export type Rol = 'ADMIN' | 'COMERCIAL' | 'COMPRAS' | 'ALMACEN';

/** Nombre del área que corresponde a cada rol. */
export const ROL_NOMBRE: Record<Rol, string> = {
  ADMIN: 'Administrador',
  COMERCIAL: 'Comercial',
  COMPRAS: 'Logística - Compras',
  ALMACEN: 'Logística - Almacén',
};

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
