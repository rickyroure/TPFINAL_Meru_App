import { ROLES } from '../constants/roles'

// ---------------------------------------------------------------------------
// SERVICIO DE AUTENTICACIÓN (versión de DESARROLLO)
//
// Toda la app usa solamente estas funciones: login, logout, obtenerSesion y
// obtenerToken. Hoy son falsas (usuarios de prueba). Cuando se conecte Keycloak
// se reemplaza el contenido de ESTE archivo y el resto del frontend no se toca.
//
// IMPORTANTE: borrar USUARIOS_DEMO cuando se conecte la autenticación real.
// ---------------------------------------------------------------------------

const CLAVE_SESION = 'meru_sesion'

const USUARIOS_DEMO = [
  { id: 1, nombre: 'Admin Demo', email: 'admin@meru.com', password: 'admin123', rol: ROLES.ADMIN },
  { id: 2, nombre: 'Profesor Demo', email: 'profesor@meru.com', password: 'profe123', rol: ROLES.PROFESOR },
  // alumnoId es el ID de la tabla "alumno": es el que piden los endpoints (/alumnos/{id}).
  { id: 3, nombre: 'Alumno Demo', email: 'alumno@meru.com', password: 'alumno123', rol: ROLES.ALUMNO, alumnoId: 1 },
]

export const authService = {
  async login(email, password) {
    // Simula la demora de una llamada a la red.
    await new Promise((resolve) => setTimeout(resolve, 400))

    const usuario = USUARIOS_DEMO.find(
      (u) => u.email === email.trim().toLowerCase() && u.password === password,
    )
    if (!usuario) {
      throw new Error('Email o contraseña incorrectos')
    }

    // La sesión nunca guarda la contraseña.
    const sesion = {
      id: usuario.id,
      nombre: usuario.nombre,
      email: usuario.email,
      rol: usuario.rol,
      alumnoId: usuario.alumnoId ?? null,
    }
    localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion))
    return sesion
  },

  logout() {
    localStorage.removeItem(CLAVE_SESION)
  },

  obtenerSesion() {
    try {
      return JSON.parse(localStorage.getItem(CLAVE_SESION))
    } catch {
      return null
    }
  },

  // Token JWT que se envía al backend. Con Keycloak va a devolver el token real.
  obtenerToken() {
    return this.obtenerSesion()?.token ?? null
  },
}