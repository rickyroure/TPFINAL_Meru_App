import axios from 'axios'
import { ROLES } from '../constants/roles'
import {
  API_URL,
  KEYCLOAK_CLIENT_ID,
  KEYCLOAK_REALM,
  KEYCLOAK_URL,
  USE_MOCK,
} from '../config'

// ---------------------------------------------------------------------------
// SERVICIO DE AUTENTICACIÓN (Soporta modo MOCK y modo REAL con Keycloak + Backend)
// ---------------------------------------------------------------------------

const CLAVE_SESION = 'meru_sesion'

const USUARIOS_DEMO = [
  { id: 1, nombre: 'Admin Demo', email: 'admin@meru.com', password: 'admin123', rol: ROLES.ADMIN },
  { id: 2, nombre: 'Profesor Demo', email: 'profesor@meru.com', password: 'profe123', rol: ROLES.PROFESOR, profesorId: 1 },
  { id: 3, nombre: 'Alumno Demo', email: 'alumno@meru.com', password: 'alumno123', rol: ROLES.ALUMNO, alumnoId: 1 },
]

function decodificarPayloadJwt(token) {
  const base64Url = token.split('.')[1]
  const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
  const jsonPayload = decodeURIComponent(
    atob(base64)
      .split('')
      .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
      .join(''),
  )
  return JSON.parse(jsonPayload)
}

function resolverRolDesdeClaims(payload) {
  const roles = (payload?.realm_access?.roles ?? []).map((r) => r.toLowerCase())
  if (roles.includes(ROLES.ADMIN)) return ROLES.ADMIN
  if (roles.includes(ROLES.PROFESOR)) return ROLES.PROFESOR
  return ROLES.ALUMNO
}

export const authService = {
  async login(identificador, password) {
    if (USE_MOCK) {
      await new Promise((resolve) => setTimeout(resolve, 400))

      const usuario = USUARIOS_DEMO.find(
        (u) => u.email === identificador.trim().toLowerCase() && u.password === password,
      )
      if (!usuario) {
        throw new Error('Email o contraseña incorrectos')
      }

      const sesion = {
        id: usuario.id,
        nombre: usuario.nombre,
        email: usuario.email,
        rol: usuario.rol,
        alumnoId: usuario.alumnoId ?? null,
        profesorId: usuario.profesorId ?? null,
      }
      localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion))
      return sesion
    }

    if (identificador.trim().toLowerCase() === 'admin' && password === 'admin') {
      throw new Error(
        'admin / admin es el usuario de la consola de Keycloak (:8081). Para Meru App usá admin.test / admin1234.',
      )
    }

    // Autenticación real contra Keycloak (Direct Access Grants)
    const tokenUrl = `${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token`
    const params = new URLSearchParams()
    params.append('client_id', KEYCLOAK_CLIENT_ID)
    params.append('grant_type', 'password')
    params.append('username', identificador.trim())
    params.append('password', password)

    let accessToken
    try {
      const { data } = await axios.post(tokenUrl, params, {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      })
      accessToken = data.access_token
    } catch (err) {
      if (err.response?.status === 401 || err.response?.status === 400) {
        const desc = err.response?.data?.error_description ?? ''
        if (desc.includes('Direct access grants')) {
          throw new Error(
            'El cliente meru-frontend en Keycloak no tiene habilitado Direct Access Grants.',
            { cause: err },
          )
        }
        throw new Error('Usuario o contraseña incorrectos en Keycloak', { cause: err })
      }
      throw new Error('No se pudo conectar con el servidor de autenticación (Keycloak)', {
        cause: err,
      })
    }

    const payload = decodificarPayloadJwt(accessToken)
    const rol = resolverRolDesdeClaims(payload)
    const keycloakId = payload.sub
    const email = payload.email ?? `${payload.preferred_username ?? identificador.trim()}@meru.app`
    const nombre = payload.given_name ?? payload.name ?? payload.preferred_username ?? 'Usuario'
    const apellido = payload.family_name ?? 'Meru'

    // Sincronizar identidad en el backend local (POST /auth/sync) y obtener alumnoId / profesorId
    let syncData = null
    try {
      const { data } = await axios.post(
        `${API_URL}/auth/sync`,
        { keycloakId, email, nombre, apellido },
        { headers: { Authorization: `Bearer ${accessToken}` } },
      )
      syncData = data
    } catch {
      // Si el backend aún no responde en /auth/sync, continuamos con los datos del token
    }

    const sesion = {
      id: syncData?.id ?? null,
      nombre: `${syncData?.nombre ?? nombre} ${syncData?.apellido ?? apellido}`.trim(),
      email: syncData?.email ?? email,
      rol,
      alumnoId: syncData?.alumnoId ?? (rol === ROLES.ALUMNO ? 1 : null),
      profesorId: syncData?.profesorId ?? (rol === ROLES.PROFESOR ? 1 : null),
      token: accessToken,
    }

    localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion))
    return sesion
  },

  actualizarSesion(parcial) {
    const actual = this.obtenerSesion()
    if (!actual) return null
    const nueva = { ...actual, ...parcial }
    localStorage.setItem(CLAVE_SESION, JSON.stringify(nueva))
    return nueva
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

  // Token JWT que se envía al backend. Con Keycloak devuelve el token real.
  obtenerToken() {
    return this.obtenerSesion()?.token ?? null
  },
}
