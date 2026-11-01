export type Rol = 'ADMIN' | 'VENDEDOR';

/** Usuario tal como lo devuelve el backend (UsuarioResponse). */
export interface Usuario {
  id: number;
  username: string;
  nombre: string;
  rol: Rol;
  activo: boolean;
}

/** Datos para crear un usuario. */
export interface UsuarioRequest {
  username: string;
  nombre: string;
  rol: Rol;
  password: string;
}

/** Datos editables de un usuario existente. */
export interface UsuarioUpdateRequest {
  nombre: string;
  rol: Rol;
  activo: boolean;
}
