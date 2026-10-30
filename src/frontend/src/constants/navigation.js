import { ROLES } from './roles'

// Menú lateral de cada rol.
// "disponible: false" muestra la opción apagada con la etiqueta "Pronto".
// A medida que desarrollemos una pantalla, le agregamos "to" y la pasamos a true.
export const MENU_POR_ROL = {
  [ROLES.ALUMNO]: [
    { label: 'Inicio', to: '/alumno', disponible: true },
    { label: 'Mi perfil', to: '/alumno/perfil', disponible: true },
    { label: 'Mi rutina', to: '/alumno/rutina', disponible: true },
    { label: 'Clases', to: '/alumno/clases', disponible: true },
    { label: 'Mi plan', to: '/alumno/plan', disponible: true },
    { label: 'Shop', disponible: false },
  ],
  [ROLES.PROFESOR]: [
    { label: 'Inicio', to: '/profesor', disponible: true },
    { label: 'Mis alumnos', to: '/profesor/alumnos', disponible: true },
    { label: 'Rutinas', to: '/profesor/rutinas', disponible: true },
    { label: 'Clases', disponible: false },
  ],
  [ROLES.ADMIN]: [
    { label: 'Inicio', to: '/admin', disponible: true },
    { label: 'Usuarios', disponible: false },
    { label: 'Clases', to: '/admin/clases', disponible: true },
    { label: 'Planes y tarifas', to: '/admin/planes', disponible: true },
    { label: 'Reportes', to: '/admin/reportes', disponible: true },
  ],
}