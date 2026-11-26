import { api } from './api'
import { USE_MOCK } from '../config'
import * as mock from '../mocks/rutinas'

// Operaciones sobre rutinas y progreso. Cada función usa datos de prueba o el
// backend real según VITE_USE_MOCK, y devuelve lo mismo en ambos casos.
export const rutinasService = {
  // GET /rutinas/alumno/{alumnoId}
  async listarPorAlumno(alumnoId) {
    if (USE_MOCK) return mock.listarRutinasPorAlumno(alumnoId)
    const { data } = await api.get(`/rutinas/alumno/${alumnoId}`)
    return data
  },

  // GET /rutinas/profesor/{profesorId}: plantillas (alumnoId null) y rutinas ya asignadas.
  async listarPorProfesor(profesorId) {
    if (USE_MOCK) return mock.listarRutinasPorProfesor(profesorId)
    const { data } = await api.get(`/rutinas/profesor/${profesorId}`)
    return data
  },

  // POST /rutinas/plantillas
  async crearPlantilla(datos) {
    if (USE_MOCK) return mock.crearPlantilla(datos)
    const { data } = await api.post('/rutinas/plantillas', datos)
    return data
  },

  // POST /rutinas/{rutinaId}/asignar
  async asignar(rutinaId, datos) {
    if (USE_MOCK) return mock.asignarRutina(rutinaId, datos)
    const { data } = await api.post(`/rutinas/${rutinaId}/asignar`, datos)
    return data
  },

  // GET /progreso-sets/plan-ejercicio/{planEjercicioId}
  async listarProgreso(planEjercicioId) {
    if (USE_MOCK) return mock.listarProgresoPorPlanEjercicio(planEjercicioId)
    const { data } = await api.get(`/progreso-sets/plan-ejercicio/${planEjercicioId}`)
    return data
  },

  // POST /progreso-sets
  async registrarProgreso(datos) {
    if (USE_MOCK) return mock.registrarProgresoSet(datos)
    const { data } = await api.post('/progreso-sets', datos)
    return data
  },
}