import { useEffect, useState } from 'react'
import { ordenesService } from '../../services/ordenes'
import { mensajeDeError } from '../../utils/errores'

const formatearPrecio = (valor) =>
  new Intl.NumberFormat('es-AR', {
    style: 'currency',
    currency: 'ARS',
    maximumFractionDigits: 0,
  }).format(valor)

// "2026-11-12T01:07:00" -> "12/11/2026 01:07"
function formatearFechaHora(iso) {
  const d = new Date(iso)
  const p = (n) => String(n).padStart(2, '0')
  return `${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()} ${p(d.getHours())}:${p(d.getMinutes())}`
}

const ESTILO_ESTADO = {
  PENDIENTE: { texto: 'Pendiente de pago', clase: 'bg-amber-500/15 text-amber-700' },
  PAGADA: { texto: 'Pagada', clase: 'bg-celeste/15 text-azul' },
  CANCELADA: { texto: 'Cancelada', clase: 'bg-red-500/15 text-red-700' },
}

export default function MisCompras({ alumnoId }) {
  const [ordenes, setOrdenes] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  // Cada vez que cambia, se vuelven a pedir las compras.
  const [version, setVersion] = useState(0)

  useEffect(() => {
    let cancelado = false
    ordenesService
      .listarDelAlumno(alumnoId)
      .then((datos) => {
        if (cancelado) return
        setOrdenes(datos)
        setError('')
      })
      .catch((err) => {
        if (!cancelado) setError(mensajeDeError(err))
      })
      .finally(() => {
        if (!cancelado) setCargando(false)
      })
    return () => {
      cancelado = true
    }
  }, [alumnoId, version])

  if (cargando) return <p className="text-negro/70">Cargando compras...</p>
  if (error) return <p className="text-red-700">{error}</p>

  // Las más nuevas primero (el backend no garantiza un orden).
  const ordenadas = [...ordenes].sort((a, b) => b.fecha.localeCompare(a.fecha))
  const hayPendientes = ordenadas.some((o) => o.estado === 'PENDIENTE')

  return (
    <div>
      <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
        <p className="text-sm text-negro/70">
          {hayPendientes
            ? 'Si ya pagaste en Mercado Pago, la orden puede tardar un momento en confirmarse.'
            : 'Acá ves el historial de tus compras.'}
        </p>
        <button
          onClick={() => setVersion((v) => v + 1)}
          className="rounded-lg border border-negro/20 px-3 py-1.5 text-sm text-negro transition hover:bg-negro/10"
        >
          Actualizar
        </button>
      </div>

      {ordenadas.length === 0 ? (
        <p className="text-sm text-negro0">Todavía no hiciste ninguna compra.</p>
      ) : (
        <ul className="flex flex-col gap-3">
          {ordenadas.map((o) => {
            const estilo = ESTILO_ESTADO[o.estado] ?? ESTILO_ESTADO.PENDIENTE
            return (
              <li key={o.id} className="rounded-2xl border border-negro/10 bg-gris p-4">
                <div className="flex flex-wrap items-center justify-between gap-2">
                  <div>
                    <p className="font-semibold">Orden #{o.id}</p>
                    <p className="text-xs text-negro0">{formatearFechaHora(o.fecha)}</p>
                  </div>
                  <span className={`rounded-full px-2 py-0.5 text-xs ${estilo.clase}`}>{estilo.texto}</span>
                </div>
                <ul className="mt-3 divide-y divide-negro/10 text-sm">
                  {o.items.map((i) => (
                    <li key={i.id} className="flex justify-between gap-4 py-1.5">
                      <span className="text-negro">
                        {i.cantidad} × {i.productoNombre}
                      </span>
                      <span className="text-negro/70">{formatearPrecio(i.subtotal)}</span>
                    </li>
                  ))}
                </ul>
                <p className="mt-3 text-right font-semibold text-azul">
                  Total {formatearPrecio(o.total)}
                </p>
              </li>
            )
          })}
        </ul>
      )}
    </div>
  )
}