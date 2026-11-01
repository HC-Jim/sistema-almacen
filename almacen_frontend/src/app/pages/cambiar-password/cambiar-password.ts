import { Component, inject, signal } from '@angular/core';
import { AbstractControl, NonNullableFormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { AuthService } from '../../core/auth';
import { mensajeError } from '../../core/errores';

/** La nueva contraseña y su confirmación deben coincidir. */
function coinciden(grupo: AbstractControl): ValidationErrors | null {
  const { nueva, confirmar } = grupo.value as { nueva: string; confirmar: string };
  return nueva && confirmar && nueva !== confirmar ? { noCoinciden: true } : null;
}

/** El usuario conectado cambia su propia contraseña. */
@Component({
  imports: [ReactiveFormsModule],
  selector: 'app-cambiar-password',
  templateUrl: './cambiar-password.html',
})
export class CambiarPassword {
  protected readonly auth = inject(AuthService);
  private readonly fb = inject(NonNullableFormBuilder);

  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly exito = signal<string | null>(null);

  protected readonly form = this.fb.group(
    {
      actual: ['', Validators.required],
      nueva: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
      confirmar: ['', Validators.required],
    },
    { validators: coinciden },
  );

  protected guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { actual, nueva } = this.form.getRawValue();
    this.guardando.set(true);
    this.error.set(null);
    this.exito.set(null);
    this.auth.cambiarPassword(actual, nueva).subscribe({
      next: () => {
        this.guardando.set(false);
        this.form.reset();
        this.exito.set('Contraseña actualizada. Úsela la próxima vez que ingrese.');
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(mensajeError(err));
      },
    });
  }
}
