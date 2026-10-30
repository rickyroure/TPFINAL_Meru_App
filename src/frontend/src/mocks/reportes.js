// Datos de prueba que imitan los reportes del backend (solo admin):
//  - GET /administracion/reportes/ingresos?anio=&mes=   (ReporteIngresosResponseDTO)
//  - GET /administracion/reportes/afluencia              (ReporteAfluenciaResponseDTO)
//  - GET /administracion/dashboard/actividades           (lista de ActividadComposicionDTO)
// Son cifras fijas de ejemplo: no se calculan a partir de las compras ni reservas del prototipo.

const demora = (ms = 450) => new Promise((resolve) => setTimeout(resolve, ms))

// Pagos aprobados por mes: [suscripciones, shop, cantidad de pagos].
const INGRESOS_POR_ANIO = {
  2026: {
    1: [310000, 45000, 14],
    2: [355000, 52000, 16],
    3: [380000, 48000, 17],
    4: [420000, 61000, 19],
    5: [450000, 70000, 21],
    6: [480000, 66000, 22],
    7: [520000, 84000, 24],
    8: [560000, 91000, 26],
    9: [610000, 105000, 29],
    10: [640000, 118000, 31],
  },
  2025: {
    9: [190000, 21000, 9],
    10: [225000, 26000, 11],
    11: [250000, 32000, 12],
    12: [285000, 41000, 14],
  },
}

export async function obtenerReporteIngresos(anio, mes) {
  await demora()
  // Mismas validaciones que ReporteIngresosService.
  const anioFiltro = anio ?? new Date().getFullYear()
  if (anioFiltro < 2000 || anioFiltro > 2100) throw new Error('El año indicado para el reporte es inválido')
  if (mes != null && (mes < 1 || mes > 12)) throw new Error('El mes debe estar comprendido entre 1 y 12')

  const delAnio = INGRESOS_POR_ANIO[anioFiltro] ?? {}
  const desgloseMensual = Object.entries(delAnio)
    .map(([numeroMes, [suscripciones, shop, pagos]]) => ({
      anio: anioFiltro,
      mes: Number(numeroMes),
      totalIngresos: suscripciones + shop,
      ingresosSuscripciones: suscripciones,
      ingresosShop: shop,
      cantidadPagos: pagos,
    }))
    .filter((m) => mes == null || m.mes === mes)
    .sort((a, b) => a.mes - b.mes)

  const suma = (campo) => desgloseMensual.reduce((total, m) => total + m[campo], 0)
  return {
    anio: anioFiltro,
    mes: mes ?? null,
    totalIngresos: suma('totalIngresos'),
    ingresosSuscripciones: suma('ingresosSuscripciones'),
    ingresosShop: suma('ingresosShop'),
    cantidadPagos: suma('cantidadPagos'),
    desgloseMensual,
  }
}

// El backend devuelve solo los días y horas con movimiento, ordenados de mayor a menor.
export async function obtenerReporteAfluencia() {
  await demora()
  const dias = [
    ['LUNES', 1, 84],
    ['MARTES', 2, 71],
    ['MIERCOLES', 3, 90],
    ['JUEVES', 4, 66],
    ['VIERNES', 5, 58],
    ['SABADO', 6, 32],
  ]
  const horas = [
    [7, 38], [8, 44], [9, 29], [10, 17], [11, 12], [12, 15], [13, 11],
    [17, 46], [18, 71], [19, 79], [20, 52], [21, 23],
  ]
  const porDiaSemana = dias
    .map(([diaSemana, numeroDia, cantidadConcurrencias]) => ({ diaSemana, numeroDia, cantidadConcurrencias }))
    .sort((a, b) => b.cantidadConcurrencias - a.cantidadConcurrencias)
  const porRangoHorario = horas
    .map(([horaInicio, cantidadConcurrencias]) => ({
      rangoHorario: `${String(horaInicio).padStart(2, '0')}:00 - ${String((horaInicio + 1) % 24).padStart(2, '0')}:00`,
      horaInicio,
      cantidadConcurrencias,
    }))
    .sort((a, b) => b.cantidadConcurrencias - a.cantidadConcurrencias)
  return {
    totalConcurrencias: dias.reduce((t, d) => t + d[2], 0),
    porDiaSemana,
    porRangoHorario,
  }
}

export async function obtenerComposicionPorActividad() {
  await demora()
  return [
    { tipoActividad: 'Musculación guiada', cantidadAlumnos: 31, totalParticipaciones: 88 },
    { tipoActividad: 'Funcional', cantidadAlumnos: 25, totalParticipaciones: 61 },
    { tipoActividad: 'Yoga', cantidadAlumnos: 18, totalParticipaciones: 42 },
    { tipoActividad: 'Spinning', cantidadAlumnos: 14, totalParticipaciones: 37 },
    { tipoActividad: 'Pilates', cantidadAlumnos: 11, totalParticipaciones: 24 },
  ]
}