import { Component, computed, inject, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth';
import { MODULOS } from '../../core/modulos';

/** Pantalla de un módulo: muestra solo las opciones que el rol del usuario puede usar. */
@Component({
  imports: [RouterLink],
  selector: 'app-modulo',
  templateUrl: './modulo.html',
})
export class ModuloPagina {
  private readonly auth = inject(AuthService);

  /** Id del módulo; viene del "data" de la ruta. */
  readonly modulo = input.required<string>();

  protected readonly datos = computed(() => MODULOS.find((m) => m.id === this.modulo())!);
  protected readonly opciones = computed(() => this.datos().opciones.filter((o) => this.auth.puede(o.roles)));
}
