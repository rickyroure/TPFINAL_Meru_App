// Roles de la aplicación. Cuando se defina el proveedor de autenticación
// (Keycloak, Supabase, etc.) hay que alinear estos valores con los roles del backend.
export const ROLES = {
  ALUMNO: 'ALUMNO',
  PROFESOR: 'PROFESOR',
  ADMIN: 'ADMIN',
}

// A qué panel va cada rol después de iniciar sesión.
export const RUTA_POR_ROL = {
  [ROLES.ALUMNO]: '/alumno',
  [ROLES.PROFESOR]: '/profesor',
  [ROLES.ADMIN]: '/admin',
}