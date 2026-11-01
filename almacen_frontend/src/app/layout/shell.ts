import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterOutlet } from '@angular/router';
import { filter, map } from 'rxjs';
import { AuthService } from '../core/auth';
import { MODULOS } from '../core/modulos';
import { ROL_NOMBRE } from '../models/usuario';

/**
 * Estructura común de las pantallas internas: barra superior y una barra de navegación
 * con la ruta actual (Inicio › Módulo › Opción) y el botón "Regresar" al nivel anterior.
 */
@Component({
  imports: [RouterOutlet, RouterLink],
  selector: 'app-shell',
  templateUrl: './shell.html',
  styleUrl: './shell.css',
})
export class Shell {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  private readonly url = toSignal(
    this.router.events.pipe(
      filter((e) => e instanceof NavigationEnd),
      map(() => this.router.url),
    ),
    { initialValue: this.router.url },
  );

  /** Ruta sin parámetros ni fragmento, por ejemplo "/logistica/compras/nueva". */
  private readonly ruta = computed(() => this.url().split(/[?#]/)[0]);

  protected readonly modulo = computed(() => MODULOS.find((m) => this.ruta().split('/')[1] === m.id) ?? null);
  protected readonly opcion = computed(() => {
    const m = this.modulo();
    return m?.opciones.find((o) => this.ruta() === `/${m.id}/${o.ruta}`) ?? null;
  });

  /** Desde una opción se vuelve a su módulo; desde un módulo, al menú principal. */
  protected readonly regresarA = computed(() => {
    const m = this.modulo();
    if (!m) {
      return null;
    }
    return this.opcion() ? `/${m.id}` : '/';
  });

  protected readonly rolNombre = computed(() => {
    const rol = this.auth.rol();
    return rol ? ROL_NOMBRE[rol] : '';
  });
}
