import { api } from './api'
import { USE_MOCK } from '../config'
import * as mock from '../mocks/reportes'

// Reportes del administrador. Usa datos de prueba o el backend real según VITE_USE_MOCK.
export const reportesService = {
  // GET /administracion/reportes/ingresos?anio=2026&mes=10 (el mes es opcional)
  async ingresos(anio, mes) {
    if (USE_MOCK) return mock.obtenerReporteIngresos(anio, mes)
    const { data } = await api.get('/administracion/reportes/ingresos', {
      params: { anio, ...(mes ? { mes } : {}) },
    })
    return data
  },

  // GET /administracion/reportes/afluencia
  async afluencia() {
    if (USE_MOCK) return mock.obtenerReporteAfluencia()
    const { data } = await api.get('/administracion/reportes/afluencia')
    return data
  },

  // GET /administracion/dashboard/actividades
  async actividades() {
    if (USE_MOCK) return mock.obtenerComposicionPorActividad()
    const { data } = await api.get('/administracion/dashboard/actividades')
    return data
  },
}