import { useEffect, useState } from 'react'
import { useAuth } from '../../hooks/useAuth'
import { clasesService } from '../../services/clases'
import { mensajeDeError } from '../../utils/errores'

// "2026-10-29T18:00:00" -> "jue 29 oct, 18:00"
function formatearHorario(iso) {
  return new Date(iso).toLocaleString('es-AR', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const ESTILO_ESTADO = {
  CONFIRMADA: { texto: 'Confirmada', clase: 'bg-emerald-500/15 text-emerald-400' },
  CANCELADA: { texto: 'Cancelada', clase: 'bg-neutral-700/40 text-neutral-400' },
  ASISTIO: { texto: 'Asististe', clase: 'bg-sky-500/15 text-sky-400' },
  NO_ASISTIO: { texto: 'No asististe', clase: 'bg-red-500/15 text-red-400' },
}

export default function Clases() {
  const { usuario } = useAuth()
  const [clases, setClases] = useState([])
  const [reservas, setReservas] = useState([])
  const [cargando, setCargando] = useState(true)
  const [errorCarga, setErrorCarga] = useState('')
  const [accionEnCurso, setAccionEnCurso] = useState(null)
  const [mensaje, setMensaje] = useState(null) // { tipo: 'exito' | 'error', texto }
  // Cada vez que cambia, se vuelven a pedir las clases y las reservas.
  const [version, setVersion] = useState(0)

  useEffect(() => {
    let cancelado = false
    Promise.all([clasesService.listar(), clasesService.listarReservasDelAlumno(usuario.alumnoId)])
      .then(([datosClases, datosReservas]) => {
        if (cancelado) return
        setClases(datosClases)
        setReservas(datosReservas)
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

  // Ejecuta una acción (reservar o cancelar), muestra el resultado y refresca los datos.
  const ejecutar = async (clave, accion, textoExito) => {
    setMensaje(null)
    setAccionEnCurso(clave)
    try {
      await accion()
      setMensaje({ tipo: 'exito', texto: textoExito })
      setVersion((v) => v + 1)
    } catch (err) {
      setMensaje({ tipo: 'error', texto: mensajeDeError(err) })
    } finally {
      setAccionEnCurso(null)
    }
  }

  const reservar = (clase) =>
    ejecutar(
      `reservar-${clase.id}`,
      () => clasesService.reservar(clase.id, usuario.alumnoId),
      `Reservaste ${clase.tipoActividad}.`,
    )

  const cancelar = (reserva) =>
    ejecutar(
      `cancelar-${reserva.id}`,
      () => clasesService.cancelarReserva(reserva.id),
      'Reserva cancelada.',
    )

  if (cargando) return <p className="text-neutral-400">Cargando clases...</p>
  if (errorCarga) return <p className="text-red-400">{errorCarga}</p>

  const idsReservados = new Set(
    reservas.filter((r) => r.estado === 'CONFIRMADA').map((r) => r.claseId),
  )
  const misReservas = [...reservas].sort((a, b) => b.horarioClase.localeCompare(a.horarioClase))

  return (
    <div className="max-w-4xl">
      <h2 className="text-2xl font-bold">Clases</h2>
      <p className="mb-6 mt-1 text-sm text-neutral-400">
        Reservá tu lugar. Podés cancelar hasta 30 minutos antes del inicio.
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

      <h3 className="mb-3 text-lg font-semibold text-emerald-500">Clases disponibles</h3>
      {clases.length === 0 ? (
        <p className="mb-8 text-sm text-neutral-500">No hay clases próximas.</p>
      ) : (
        <div className="mb-8 grid gap-4 sm:grid-cols-2">
          {clases.map((clase) => {
            const reservada = idsReservados.has(clase.id)
            const sinCupo = clase.cupoDisponible <= 0
            return (
              <article
                key={clase.id}
                className="flex flex-col justify-between rounded-2xl border border-neutral-800 bg-neutral-900 p-5"
              >
                <div>
                  <h4 className="font-semibold">{clase.tipoActividad}</h4>
                  <p className="mt-1 text-sm capitalize text-neutral-400">
                    {formatearHorario(clase.horario)}
                  </p>
                  <p className="text-sm text-neutral-500">Profesor: {clase.nombreProfesor}</p>
                  <p className={`mt-2 text-sm ${sinCupo ? 'text-red-400' : 'text-neutral-300'}`}>
                    {sinCupo
                      ? 'Sin cupo'
                      : `${clase.cupoDisponible} de ${clase.cupoMaximo} lugares libres`}
                  </p>
                </div>
                <button
                  onClick={() => reservar(clase)}
                  disabled={reservada || sinCupo || accionEnCurso !== null}
                  className="mt-4 rounded-lg bg-emerald-500 px-3 py-2 text-sm font-medium text-neutral-950 transition hover:bg-emerald-400 disabled:cursor-not-allowed disabled:bg-neutral-800 disabled:text-neutral-500"
                >
                  {accionEnCurso === `reservar-${clase.id}`
                    ? 'Reservando...'
                    : reservada
                      ? 'Ya reservada'
                      : sinCupo
                        ? 'Sin cupo'
                        : 'Reservar'}
                </button>
              </article>
            )
          })}
        </div>
      )}

      <h3 className="mb-3 text-lg font-semibold text-emerald-500">Mis reservas</h3>
      {misReservas.length === 0 ? (
        <p className="text-sm text-neutral-500">Todavía no tenés reservas.</p>
      ) : (
        <ul className="divide-y divide-neutral-800 rounded-2xl border border-neutral-800 bg-neutral-900">
          {misReservas.map((reserva) => {
            const estilo = ESTILO_ESTADO[reserva.estado]
            return (
              <li key={reserva.id} className="flex items-center justify-between gap-4 p-4">
                <div>
                  <p className="font-medium">{reserva.tipoActividadClase}</p>
                  <p className="text-sm capitalize text-neutral-400">
                    {formatearHorario(reserva.horarioClase)}
                  </p>
                </div>
                <div className="flex items-center gap-3">
                  <span className={`rounded-full px-2 py-0.5 text-xs ${estilo.clase}`}>
                    {estilo.texto}
                  </span>
                  {reserva.estado === 'CONFIRMADA' && (
                    <button
                      onClick={() => cancelar(reserva)}
                      disabled={accionEnCurso !== null}
                      className="rounded-lg border border-neutral-700 px-3 py-1.5 text-sm transition hover:bg-neutral-800 disabled:opacity-50"
                    >
                      {accionEnCurso === `cancelar-${reserva.id}` ? 'Cancelando...' : 'Cancelar'}
                    </button>
                  )}
                </div>
              </li>
            )
          })}
        </ul>
      )}
    </div>
  )
}