import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { Usuarios } from './usuarios';

describe('Usuarios', () => {
  let fixture: ComponentFixture<Usuarios>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Usuarios],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(Usuarios);
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  it('lista los usuarios del backend', async () => {
    http.expectOne(`${environment.apiUrl}/usuarios`).flush([
      { id: 1, username: 'admin', nombre: 'Administrador', rol: 'ADMIN', activo: true },
      { id: 2, username: 'juan', nombre: 'Juan', rol: 'COMERCIAL', activo: false },
    ]);
    await fixture.whenStable();

    const filas = (fixture.nativeElement as HTMLElement).querySelectorAll('tbody tr');
    expect(filas.length).toBe(2);
    expect(filas[1].textContent).toContain('Inactivo');
  });
});
