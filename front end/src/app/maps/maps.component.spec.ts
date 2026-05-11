import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MapsComponent } from './maps.component';

describe('MapsComponent', () => {
  let component: MapsComponent;
  let fixture: ComponentFixture<MapsComponent>;
  let originalGoogle: any;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MapsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    originalGoogle = (window as any).google;
    (window as any).google = {
      maps: {
        LatLng: function(lat: number, lng: number) {
          return { lat, lng };
        },
        Map: function() {},
        Marker: function() {
          return {
            setMap: function() {}
          };
        }
      }
    };

    fixture = TestBed.createComponent(MapsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    (window as any).google = originalGoogle;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
