import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { CambiarPassword } from './cambiar-password';

describe('CambiarPassword', () => {
  let fixture: ComponentFixture<CambiarPassword>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CambiarPassword],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(CambiarPassword);
    http = TestBed.inject(HttpTestingController);
    await fixture.whenStable();
  });

  function llenar(actual: string, nueva: string, confirmar: string): void {
    const el: HTMLElement = fixture.nativeElement;
    const inputs = el.querySelectorAll<HTMLInputElement>('input[type=password]');
    [actual, nueva, confirmar].forEach((valor, i) => {
      inputs[i].value = valor;
      inputs[i].dispatchEvent(new Event('input'));
    });
    el.querySelector('form')!.dispatchEvent(new Event('submit'));
  }

  it('no envía si la confirmación no coincide', () => {
    llenar('admin123', 'nueva-clave-1', 'otra-clave-2');
    http.expectNone(`${environment.apiUrl}/auth/password`);
  });

  it('envía la contraseña actual y la nueva', () => {
    llenar('admin123', 'nueva-clave-1', 'nueva-clave-1');
    const req = http.expectOne(`${environment.apiUrl}/auth/password`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ actual: 'admin123', nueva: 'nueva-clave-1' });
  });
});
