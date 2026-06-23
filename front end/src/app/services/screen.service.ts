import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { distinctUntilChanged, finalize, map, shareReplay, tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { Screen, ScreenPermissions, ScreenRole, UserScreens } from '../models/screen.model';

@Injectable({ providedIn: 'root' })
export class ScreenService {
  private apiUrl = `${environment.apiUrl}/screens`;
  private userScreensSubject = new BehaviorSubject<Screen[]>([]);
  public userScreens$ = this.userScreensSubject.asObservable();

  private permissionsCache = new Map<string, ScreenPermissions>();
  private permissionsVersionSubject = new BehaviorSubject<number>(0);
  private permissionsLoaded = false;
  private permissionsLoading$?: Observable<UserScreens>;
  private lastUserScreens?: UserScreens;

  constructor(private http: HttpClient) { }

  getAllScreens(): Observable<Screen[]> {
    return this.http.get<Screen[]>(this.apiUrl).pipe(
      map(screens => screens.map(screen => this.normalizeScreen(screen)))
    );
  }

  getActiveScreens(): Observable<Screen[]> {
    return this.http.get<Screen[]>(`${this.apiUrl}/active`).pipe(
      map(screens => screens.map(screen => this.normalizeScreen(screen)))
    );
  }

  getScreenById(id: number): Observable<Screen> {
    return this.http.get<Screen>(`${this.apiUrl}/${id}`).pipe(map(screen => this.normalizeScreen(screen)));
  }

  getScreenByCode(code: string): Observable<Screen> {
    return this.http.get<Screen>(`${this.apiUrl}/code/${code}`).pipe(map(screen => this.normalizeScreen(screen)));
  }

  getScreensForUser(username: string): Observable<UserScreens> {
    return this.http.get<UserScreens>(`${this.apiUrl}/user/${username}`).pipe(
      map(userScreens => this.normalizeUserScreens(userScreens))
    );
  }

  getMyScreens(): Observable<UserScreens> {
    return this.ensurePermissionsLoaded();
  }

  /** Charge tout le cache une seule fois pour les guards, menus et directives. */
  ensurePermissionsLoaded(force = false): Observable<UserScreens> {
    if (!force && this.permissionsLoaded && this.lastUserScreens) {
      return of(this.lastUserScreens);
    }
    if (!force && this.permissionsLoading$) {
      return this.permissionsLoading$;
    }

    this.permissionsLoading$ = this.http.get<UserScreens>(`${this.apiUrl}/me`).pipe(
      map(userScreens => this.normalizeUserScreens(userScreens)),
      tap(userScreens => this.populateCache(userScreens)),
      finalize(() => this.permissionsLoading$ = undefined),
      shareReplay(1)
    );
    return this.permissionsLoading$;
  }

  getMyPermissions(screenCode: string): Observable<ScreenPermissions> {
    const cached = this.permissionsCache.get(screenCode);
    if (cached) {
      return of(cached);
    }
    return this.http.get<ScreenPermissions>(`${this.apiUrl}/permissions/${screenCode}`).pipe(
      tap(permissions => {
        this.permissionsCache.set(screenCode, permissions);
        this.permissionsVersionSubject.next(this.permissionsVersionSubject.value + 1);
      })
    );
  }

  canAccessScreen(screenCode: string): Observable<boolean> {
    return this.ensurePermissionsLoaded().pipe(
      map(() => this.hasPermissionCached(screenCode, 'canView'))
    );
  }

  /** Observable local: aucun appel HTTP par bouton. */
  hasPermission(screenCode: string, permissionType: keyof ScreenPermissions): Observable<boolean> {
    return this.permissionsVersionSubject.pipe(
      map(() => this.hasPermissionCached(screenCode, permissionType)),
      distinctUntilChanged()
    );
  }

  hasPermissionCached(screenCode: string, permissionType: keyof ScreenPermissions): boolean {
    return this.permissionsCache.get(screenCode)?.[permissionType] === true;
  }

  createScreen(screen: Screen): Observable<Screen> {
    return this.http.post<Screen>(this.apiUrl, screen);
  }

  updateScreen(id: number, screen: Screen): Observable<Screen> {
    return this.http.put<Screen>(`${this.apiUrl}/${id}`, screen);
  }

  deleteScreen(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  assignRoleToScreen(screenId: number, roleId: number, permissions: ScreenPermissions): Observable<ScreenRole> {
    return this.http.post<ScreenRole>(`${this.apiUrl}/${screenId}/roles/${roleId}`, permissions);
  }

  removeRoleFromScreen(screenId: number, roleId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${screenId}/roles/${roleId}`);
  }

  getScreenRoles(screenId: number): Observable<ScreenRole[]> {
    return this.http.get<ScreenRole[]>(`${this.apiUrl}/${screenId}/roles`);
  }

  getPermissionMatrix(): Observable<ScreenRole[]> {
    return this.http.get<ScreenRole[]>(`${this.apiUrl}/matrix`);
  }

  copyRoleProfile(sourceRoleId: number, targetRoleId: number): Observable<ScreenRole[]> {
    return this.http.post<ScreenRole[]>(`${this.apiUrl}/roles/copy`, { sourceRoleId, targetRoleId });
  }

  getPermissionHistory(page = 0, size = 20): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/history?page=${page}&size=${size}&sort=dateAction,desc`);
  }

  clearCache(): void {
    this.permissionsCache.clear();
    this.permissionsLoaded = false;
    this.lastUserScreens = undefined;
    this.userScreensSubject.next([]);
    this.permissionsVersionSubject.next(this.permissionsVersionSubject.value + 1);
  }

  refreshUserScreens(): void {
    this.ensurePermissionsLoaded(true).subscribe();
  }

  private normalizeUserScreens(userScreens: UserScreens): UserScreens {
    return {
      ...userScreens,
      screens: userScreens.screens.map(screen => this.normalizeScreen(screen))
    };
  }

  private normalizeScreen(screen: Screen): Screen {
    return {
      ...screen,
      libelle: this.fixFrenchEncoding(screen.libelle),
      description: screen.description ? this.fixFrenchEncoding(screen.description) : screen.description
    };
  }

  /** Filet de sécurité pour les anciennes valeurs UTF-8 enregistrées en Windows-1252. */
  private fixFrenchEncoding(value: string): string {
    const replacements: Array<[string, string]> = [
      ['\u00C3\u0192', '\u00C3'], ['\u00C3\u201A', '\u00C2'],
      ['\u00C2\u00A9', '\u00A9'], ['\u00C2\u00A0', ' '], ['\u00C2\u00B0', '\u00B0'],
      ['\u00C3\u00A0', '\u00E0'], ['\u00C3\u00A2', '\u00E2'], ['\u00C3\u00A4', '\u00E4'],
      ['\u00C3\u00A7', '\u00E7'], ['\u00C3\u00A8', '\u00E8'], ['\u00C3\u00A9', '\u00E9'],
      ['\u00C3\u00AA', '\u00EA'], ['\u00C3\u00AB', '\u00EB'], ['\u00C3\u00AE', '\u00EE'],
      ['\u00C3\u00AF', '\u00EF'], ['\u00C3\u00B4', '\u00F4'], ['\u00C3\u00B6', '\u00F6'],
      ['\u00C3\u00B9', '\u00F9'], ['\u00C3\u00BB', '\u00FB'], ['\u00C3\u00BC', '\u00FC'],
      ['\u00C3\u0080', '\u00C0'], ['\u00C3\u0087', '\u00C7'], ['\u00C3\u0088', '\u00C8'],
      ['\u00C3\u0089', '\u00C9'], ['\u00C3\u008A', '\u00CA'], ['\u00C3\u0094', '\u00D4'],
      ['\u00E2\u20AC\u2122', '\u2019'], ['\u00E2\u20AC\u201C', '\u2013'],
      ['\u00E2\u20AC\u201D', '\u2014'], ['\u00E2\u20AC\u00A6', '\u2026']
    ];

    let repaired = value;
    for (let pass = 0; pass < 3; pass++) {
      const before = repaired;
      replacements.forEach(([broken, correct]) => repaired = repaired.split(broken).join(correct));
      if (repaired === before) {
        break;
      }
    }
    return repaired;
  }

  private populateCache(userScreens: UserScreens): void {
    this.permissionsCache.clear();
    userScreens.screens.forEach(screen => {
      if (screen.permissions) {
        this.permissionsCache.set(screen.code, screen.permissions);
      }
    });
    this.lastUserScreens = userScreens;
    this.permissionsLoaded = true;
    this.userScreensSubject.next(userScreens.screens);
    this.permissionsVersionSubject.next(this.permissionsVersionSubject.value + 1);
  }
}
