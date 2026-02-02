import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role } from '../models/utilisateur.model';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm: FormGroup;
  loading = false;
  error = '';
  returnUrl: string = '/tpe';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });

    // Rediriger si déjà connecté
    if (this.authService.isLoggedIn()) {
      this.router.navigate([this.getDefaultRoute()]);
    }
  }

  ngOnInit(): void {
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/tpe';
  }

  getDefaultRoute(): string {
    const user = this.authService.getCurrentUser();
    if (user) {
      // ADMIN et MONETIQUE vont au dashboard
      if (user.role === Role.ADMIN || user.role === Role.MONETIQUE) {
        return '/dashboard';
      }
      // AGENCE va aux demandes
      if (user.role === Role.AGENCE) {
        return '/demandes';
      }
    }
    // Par défaut, TPE
    return '/tpe';
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }

    this.loading = true;
    this.error = '';

    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        const user = this.authService.getCurrentUser();
        // Si l'utilisateur essaie d'accéder au dashboard mais n'a pas la permission
        if (this.returnUrl === '/dashboard' && 
            user && user.role !== Role.ADMIN && user.role !== Role.MONETIQUE) {
          this.router.navigate([this.getDefaultRoute()]);
        } else {
          this.router.navigate([this.returnUrl]);
        }
      },
      error: (err) => {
        console.error('Erreur de connexion:', err);
        this.error = 'Identifiants incorrects';
        this.loading = false;
      }
    });
  }
}
