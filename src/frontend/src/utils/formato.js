// Funciones para mostrar precios y fechas con el formato argentino.

const DIAS_CORTOS = ['dom', 'lun', 'mar', 'mié', 'jue', 'vie', 'sáb']

// 38000 -> "$ 38.000"
export const formatearPrecio = (valor) =>
  new Intl.NumberFormat('es-AR', {
    style: 'currency',
    currency: 'ARS',
    maximumFractionDigits: 0,
  }).format(valor)

// "2026-11-12" -> "12/11/2026"
export const formatearFecha = (iso) => iso.split('-').reverse().join('/')

// "2026-11-12T18:00:00" -> "jue 12/11 · 18:00"
export function formatearFechaHora(iso) {
  const d = new Date(iso)
  const p = (n) => String(n).padStart(2, '0')
  return `${DIAS_CORTOS[d.getDay()]} ${p(d.getDate())}/${p(d.getMonth() + 1)} · ${p(d.getHours())}:${p(d.getMinutes())}`
}