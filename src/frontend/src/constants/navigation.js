import { ROLES } from './roles'

// Menú lateral de cada rol.
// "disponible: false" muestra la opción apagada con la etiqueta "Pronto".
// A medida que desarrollemos una pantalla, le agregamos "to" y la pasamos a true.
export const MENU_POR_ROL = {
  [ROLES.ALUMNO]: [
    { label: 'Inicio', to: '/alumno', disponible: true },
    { label: 'Mi rutina', disponible: false },
    { label: 'Clases', disponible: false },
    { label: 'Mi plan', disponible: false },
    { label: 'Shop', disponible: false },
  ],
  [ROLES.PROFESOR]: [
    { label: 'Inicio', to: '/profesor', disponible: true },
    { label: 'Mis alumnos', disponible: false },
    { label: 'Rutinas', disponible: false },
    { label: 'Clases', disponible: false },
  ],
  [ROLES.ADMIN]: [
    { label: 'Inicio', to: '/admin', disponible: true },
    { label: 'Usuarios', disponible: false },
    { label: 'Clases', disponible: false },
    { label: 'Planes y tarifas', disponible: false },
    { label: 'Reportes', disponible: false },
  ],
}