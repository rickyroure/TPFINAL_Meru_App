import { api } from './api'
import { USE_MOCK } from '../config'
import * as mock from '../mocks/alumnos'

// Operaciones sobre alumnos. Cada función usa datos de prueba o el backend real
// según VITE_USE_MOCK, y devuelve lo mismo en ambos casos.
export const alumnosService = {
  // GET /alumnos/{id}
  async obtenerPerfil(alumnoId) {
    if (USE_MOCK) return mock.obtenerAlumno(alumnoId)
    const { data } = await api.get(`/alumnos/${alumnoId}`)
    return data
  },

  // PUT /alumnos/{id}/perfil
  async actualizarPerfil(alumnoId, datos) {
    if (USE_MOCK) return mock.actualizarPerfil(alumnoId, datos)
    const { data } = await api.put(`/alumnos/${alumnoId}/perfil`, datos)
    return data
  },
}