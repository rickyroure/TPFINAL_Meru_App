// Roles de la aplicación. Coinciden con los roles del realm de Keycloak
// (alumno, profesor, admin), que el backend lee del token.
export const ROLES = {
  ALUMNO: 'alumno',
  PROFESOR: 'profesor',
  ADMIN: 'admin',
}

// A qué panel va cada rol después de iniciar sesión.
export const RUTA_POR_ROL = {
  [ROLES.ALUMNO]: '/alumno',
  [ROLES.PROFESOR]: '/profesor',
  [ROLES.ADMIN]: '/admin',
}