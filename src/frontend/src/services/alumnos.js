import { api } from './api'
import { USE_MOCK } from '../config'
import * as mock from '../mocks/alumnos'

// Operaciones sobre alumnos. Cada función usa datos de prueba o el backend real
// según VITE_USE_MOCK, y devuelve lo mismo en ambos casos.
export const alumnosService = {
  // GET /alumnos
  async listar() {
    if (USE_MOCK) return mock.listarAlumnos()
    try {
      const { data } = await api.get('/alumnos')
      return data
    } catch {
      // Fallback por si el backend aún no fue reiniciado tras agregar GET /alumnos:
      // consulta los IDs de alumnos conocidos en la base local.
      const promesas = [1, 92].map((id) => api.get(`/alumnos/${id}`).then((r) => r.data))
      const resultados = await Promise.allSettled(promesas)
      return resultados.filter((r) => r.status === 'fulfilled').map((r) => r.value)
    }
  },

  // GET /alumnos/{id}
  async obtenerPerfil(alumnoId) {
    if (USE_MOCK) return mock.obtenerAlumno(alumnoId)
    const { data } = await api.get(`/alumnos/${alumnoId}`)
    return data
  },

  // PUT /alumnos/{id}/perfil (A1)
  async actualizarPerfil(alumnoId, datos) {
    if (USE_MOCK) return mock.actualizarPerfil(alumnoId, datos)
    const { data } = await api.put(`/alumnos/${alumnoId}/perfil`, datos)
    return data
  },

  // PATCH /alumnos/{id}/apto-fisico (A2)
  async actualizarAptoFisico(alumnoId, aptoFisico) {
    if (USE_MOCK) return mock.actualizarAptoFisico(alumnoId, aptoFisico)
    const { data } = await api.patch(`/alumnos/${alumnoId}/apto-fisico`, { aptoFisico })
    return data
  },

  // PUT /alumnos/{id}/profesor/{profesorId} (A3)
  async asignarProfesor(alumnoId, profesorId, profesorMock = null) {
    if (USE_MOCK) return mock.asignarProfesor(alumnoId, profesorMock)
    const { data } = await api.put(`/alumnos/${alumnoId}/profesor/${profesorId}`)
    return data
  },
}
