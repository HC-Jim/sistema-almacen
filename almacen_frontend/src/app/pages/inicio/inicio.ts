import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth';
import { MODULOS } from '../../core/modulos';

/** Menú principal: acceso a los módulos del rol del usuario. */
@Component({
  imports: [RouterLink],
  selector: 'app-inicio',
  templateUrl: './inicio.html',
})
export class Inicio {
  protected readonly auth = inject(AuthService);

  /** Solo los módulos donde el rol del usuario tiene alguna opción. */
  protected readonly modulos = computed(() =>
    MODULOS.filter((m) => m.opciones.some((o) => this.auth.puede(o.roles))),
  );
}
