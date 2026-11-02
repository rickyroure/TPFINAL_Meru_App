import { useEffect, useState } from 'react'
import { useAuth } from '../../hooks/useAuth'
import { planesService } from '../../services/planes'
import { mensajeDeError } from '../../utils/errores'

const DIAS_AVISO_VENCIMIENTO = 3

const formatearPrecio = (valor) =>
  new Intl.NumberFormat('es-AR', {
    style: 'currency',
    currency: 'ARS',
    maximumFractionDigits: 0,
  }).format(valor)

// "2026-10-28" -> "28/10/2026"
const formatearFecha = (iso) => iso.split('-').reverse().join('/')

// Una suscripción ACTIVA cuya fecha de fin ya pasó (0 días) se muestra como vencida.
function estadoVisible(suscripcion) {
  if (suscripcion.estado === 'ACTIVA' && suscripcion.diasRestantes === 0) return 'VENCIDA'
  return suscripcion.estado
}

const ESTILO_ESTADO = {
  ACTIVA: { texto: 'Activa', clase: 'bg-emerald-500/15 text-emerald-400' },
  VENCIDA: { texto: 'Vencida', clase: 'bg-neutral-700/40 text-neutral-400' },
  CANCELADA: { texto: 'Cancelada', clase: 'bg-red-500/15 text-red-400' },
}

export default function MiPlan() {
  const { usuario } = useAuth()
  const [planes, setPlanes] = useState([])
  const [suscripciones, setSuscripciones] = useState([])
  const [cargando, setCargando] = useState(true)
  const [errorCarga, setErrorCarga] = useState('')
  const [planAConfirmar, setPlanAConfirmar] = useState(null)
  const [pagando, setPagando] = useState(false)
  const [mensaje, setMensaje] = useState(null) // { tipo: 'exito' | 'error', texto }
  // Cada vez que cambia, se vuelven a pedir los datos.
  const [version, setVersion] = useState(0)

  useEffect(() => {
    let cancelado = false
    Promise.all([
      planesService.listarPlanes(),
      planesService.listarSuscripcionesDelAlumno(usuario.alumnoId),
    ])
      .then(([datosPlanes, datosSuscripciones]) => {
        if (cancelado) return
        setPlanes(datosPlanes)
        setSuscripciones(datosSuscripciones)
      })
      .catch((err) => {
        if (!cancelado) setErrorCarga(mensajeDeError(err))
      })
      .finally(() => {
        if (!cancelado) setCargando(false)
      })
    return () => {
      cancelado = true
    }
  }, [usuario.alumnoId, version])

  const confirmarContratacion = async () => {
    const plan = planAConfirmar
    setMensaje(null)
    setPagando(true)
    try {
      const pago = await planesService.contratar(plan, usuario.alumnoId)
      setPlanAConfirmar(null)
      if (pago.checkoutUrl) {
        // Mercado Pago real: se lleva al usuario a la página de pago.
        window.location.assign(pago.checkoutUrl)
        return
      }
      if (pago.estado === 'APROBADO') {
        setMensaje({ tipo: 'exito', texto: `Contrataste el plan ${plan.nombre}. ¡Pago aprobado!` })
      } else {
        setMensaje({ tipo: 'error', texto: `El pago quedó ${pago.estado.toLowerCase()}. ${pago.mensaje ?? ''}` })
      }
      setVersion((v) => v + 1)
    } catch (err) {
      setMensaje({ tipo: 'error', texto: mensajeDeError(err) })
    } finally {
      setPagando(false)
    }
  }

  if (cargando) return <p className="text-neutral-400">Cargando plan...</p>
  if (errorCarga) return <p className="text-red-400">{errorCarga}</p>

  // El plan vigente es la suscripción activa que vence más tarde.
  const vigente = suscripciones
    .filter((s) => estadoVisible(s) === 'ACTIVA')
    .sort((a, b) => b.fechaFin.localeCompare(a.fechaFin))[0]
  const historial = [...suscripciones].sort((a, b) => b.fechaInicio.localeCompare(a.fechaInicio))

  return (
    <div className="max-w-4xl">
      <h2 className="text-2xl font-bold">Mi plan</h2>
      <p className="mb-6 mt-1 text-sm text-neutral-400">
        Consultá tu suscripción y contratá o renová tu plan.
      </p>

      {mensaje && (
        <p
          className={`mb-4 rounded-lg px-3 py-2 text-sm ${
            mensaje.tipo === 'exito' ? 'bg-emerald-500/15 text-emerald-400' : 'bg-red-500/15 text-red-400'
          }`}
        >
          {mensaje.texto}
        </p>
      )}

      <section className="mb-8 rounded-2xl border border-neutral-800 bg-neutral-900 p-6">
        <h3 className="mb-4 font-semibold">Plan vigente</h3>
        {vigente ? (
          <>
            <div className="grid gap-4 sm:grid-cols-4">
              <div>
                <p className="text-xs uppercase tracking-wide text-neutral-500">Plan</p>
                <p className="mt-1 font-medium">{vigente.nombrePlan}</p>
              </div>
              <div>
                <p className="text-xs uppercase tracking-wide text-neutral-500">Desde</p>
                <p className="mt-1 font-medium">{formatearFecha(vigente.fechaInicio)}</p>
              </div>
              <div>
                <p className="text-xs uppercase tracking-wide text-neutral-500">Vence</p>
                <p className="mt-1 font-medium">{formatearFecha(vigente.fechaFin)}</p>
              </div>
              <div>
                <p className="text-xs uppercase tracking-wide text-neutral-500">Días restantes</p>
                <p className="mt-1 text-2xl font-bold text-emerald-400">{vigente.diasRestantes}</p>
              </div>
            </div>
            {vigente.diasRestantes <= DIAS_AVISO_VENCIMIENTO && (
              <p className="mt-4 rounded-lg bg-amber-500/15 px-3 py-2 text-sm text-amber-400">
                Tu plan vence pronto. Renovalo para no perder el acceso.
              </p>
            )}
          </>
        ) : (
          <p className="text-sm text-neutral-400">
            No tenés un plan vigente. Elegí uno de la lista para empezar a entrenar.
          </p>
        )}
      </section>

      <h3 className="mb-3 text-lg font-semibold text-emerald-500">Planes disponibles</h3>
      <div className="mb-8 grid gap-4 sm:grid-cols-3">
        {planes.map((plan) => (
          <article
            key={plan.id}
            className="flex flex-col justify-between rounded-2xl border border-neutral-800 bg-neutral-900 p-5"
          >
            <div>
              <h4 className="font-semibold">{plan.nombre}</h4>
              <p className="mt-2 text-2xl font-bold">{formatearPrecio(plan.precio)}</p>
              <p className="text-sm text-neutral-400">{plan.duracionDias} días de acceso</p>
            </div>

            {planAConfirmar?.id === plan.id ? (
              <div className="mt-4">
                <p className="mb-2 text-xs text-neutral-400">
                  Vas a pagar {formatearPrecio(plan.precio)} con Mercado Pago.
                </p>
                <div className="flex gap-2">
                  <button
                    onClick={confirmarContratacion}
                    disabled={pagando}
                    className="flex-1 rounded-lg bg-emerald-500 px-3 py-2 text-sm font-medium text-neutral-950 transition hover:bg-emerald-400 disabled:opacity-60"
                  >
                    {pagando ? 'Procesando...' : 'Confirmar'}
                  </button>
                  <button
                    onClick={() => setPlanAConfirmar(null)}
                    disabled={pagando}
                    className="rounded-lg border border-neutral-700 px-3 py-2 text-sm transition hover:bg-neutral-800 disabled:opacity-50"
                  >
                    Volver
                  </button>
                </div>
              </div>
            ) : (
              <button
                onClick={() => setPlanAConfirmar(plan)}
                disabled={pagando}
                className="mt-4 rounded-lg bg-emerald-500 px-3 py-2 text-sm font-medium text-neutral-950 transition hover:bg-emerald-400 disabled:opacity-60"
              >
                {vigente ? 'Renovar' : 'Contratar'}
              </button>
            )}
          </article>
        ))}
      </div>

      <h3 className="mb-3 text-lg font-semibold text-emerald-500">Mis suscripciones</h3>
      {historial.length === 0 ? (
        <p className="text-sm text-neutral-500">Todavía no tenés suscripciones.</p>
      ) : (
        <ul className="divide-y divide-neutral-800 rounded-2xl border border-neutral-800 bg-neutral-900">
          {historial.map((s) => {
            const estilo = ESTILO_ESTADO[estadoVisible(s)]
            return (
              <li key={s.id} className="flex items-center justify-between gap-4 p-4">
                <div>
                  <p className="font-medium">{s.nombrePlan}</p>
                  <p className="text-sm text-neutral-400">
                    {formatearFecha(s.fechaInicio)} al {formatearFecha(s.fechaFin)}
                  </p>
                </div>
                <span className={`rounded-full px-2 py-0.5 text-xs ${estilo.clase}`}>
                  {estilo.texto}
                </span>
              </li>
            )
          })}
        </ul>
      )}
    </div>
  )
}