import { Component, inject, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/auth';
import { mensajeError } from '../../core/errores';
import { Rol, ROL_NOMBRE, Usuario } from '../../models/usuario';
import { UsuarioService } from '../../services/usuario';

/** Administración de usuarios: alta, edición de nombre/rol, activar/desactivar y restablecer contraseña. */
@Component({
  imports: [ReactiveFormsModule],
  selector: 'app-usuarios',
  templateUrl: './usuarios.html',
})
export class Usuarios implements OnInit {
  private readonly usuarioService = inject(UsuarioService);
  private readonly fb = inject(NonNullableFormBuilder);
  protected readonly auth = inject(AuthService);

  protected readonly usuarios = signal<Usuario[]>([]);
  protected readonly cargando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly exito = signal<string | null>(null);
  protected readonly editandoId = signal<number | null>(null);

  protected readonly roles: Rol[] = ['COMERCIAL', 'COMPRAS', 'ALMACEN', 'ADMIN'];
  protected readonly rolNombre = ROL_NOMBRE;

  /** Al editar, el usuario no cambia y la contraseña se restablece aparte. */
  protected readonly form = this.fb.group({
    username: ['', [Validators.required, Validators.maxLength(50), Validators.pattern(/^[A-Za-z0-9._-]+$/)]],
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    rol: this.fb.control<Rol>('COMERCIAL', Validators.required),
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
  });

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.usuarioService.listar().subscribe({
      next: (data) => {
        this.usuarios.set(data);
        this.cargando.set(false);
      },
      error: (err) => this.mostrarError(err),
    });
  }

  protected guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const valor = this.form.getRawValue();
    const id = this.editandoId();
    const actual = this.usuarios().find((u) => u.id === id);
    const peticion =
      id === null
        ? this.usuarioService.crear(valor)
        : this.usuarioService.actualizar(id, { nombre: valor.nombre, rol: valor.rol, activo: actual?.activo ?? true });

    this.error.set(null);
    peticion.subscribe({
      next: (u) => {
        this.exito.set(`Usuario ${u.username} ${id === null ? 'creado' : 'actualizado'}.`);
        this.cancelar();
        this.cargar();
      },
      error: (err) => this.mostrarError(err),
    });
  }

  protected editar(usuario: Usuario): void {
    this.exito.set(null);
    this.editandoId.set(usuario.id);
    this.form.setValue({ username: usuario.username, nombre: usuario.nombre, rol: usuario.rol, password: '' });
    this.form.controls.username.disable();
    this.form.controls.password.disable();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  protected cambiarEstado(usuario: Usuario): void {
    const accion = usuario.activo ? 'Desactivar' : 'Activar';
    if (!confirm(`¿${accion} al usuario "${usuario.username}"?`)) {
      return;
    }
    this.error.set(null);
    this.usuarioService
      .actualizar(usuario.id, { nombre: usuario.nombre, rol: usuario.rol, activo: !usuario.activo })
      .subscribe({
        next: (u) => {
          this.exito.set(`Usuario ${u.username} ${u.activo ? 'activado' : 'desactivado'}.`);
          this.cargar();
        },
        error: (err) => this.mostrarError(err),
      });
  }

  protected restablecer(usuario: Usuario): void {
    const nueva = prompt(`Nueva contraseña para "${usuario.username}" (mínimo 8 caracteres):`);
    if (nueva === null) {
      return;
    }
    if (nueva.length < 8) {
      this.mostrarError(null, 'La contraseña debe tener al menos 8 caracteres.');
      return;
    }
    this.error.set(null);
    this.usuarioService.restablecerPassword(usuario.id, nueva).subscribe({
      next: () => this.exito.set(`Contraseña de ${usuario.username} restablecida.`),
      error: (err) => this.mostrarError(err),
    });
  }

  protected cancelar(): void {
    this.editandoId.set(null);
    this.form.reset();
    this.form.controls.username.enable();
    this.form.controls.password.enable();
  }

  protected esYo(usuario: Usuario): boolean {
    return usuario.username === this.auth.sesion()?.username;
  }

  private mostrarError(err: unknown, mensaje?: string): void {
    this.cargando.set(false);
    this.exito.set(null);
    this.error.set(mensaje ?? mensajeError(err));
  }
}
