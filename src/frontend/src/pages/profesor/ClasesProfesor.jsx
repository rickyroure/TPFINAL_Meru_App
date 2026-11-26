import { useEffect, useState } from 'react'
import { useAuth } from '../../hooks/useAuth'
import { clasesService } from '../../services/clases'
import { mensajeDeError } from '../../utils/errores'
import Ilustracion from '../../components/Ilustracion'
import { claveActividad } from '../../utils/imagenes'

const DIAS = ['dom', 'lun', 'mar', 'mié', 'jue', 'vie', 'sáb']

// "2026-11-12T18:00:00" -> "jue 12/11 · 18:00"
function formatearHorario(iso) {
  const d = new Date(iso)
  const p = (n) => String(n).padStart(2, '0')
  return `${DIAS[d.getDay()]} ${p(d.getDate())}/${p(d.getMonth() + 1)} · ${p(d.getHours())}:${p(d.getMinutes())}`
}

const ESTILO_ESTADO = {
  CONFIRMADA: { texto: 'Inscripto', clase: 'bg-celeste/15 text-azul' },
  ASISTIO: { texto: 'Asistió', clase: 'bg-sky-500/15 text-sky-700' },
  NO_ASISTIO: { texto: 'No asistió', clase: 'bg-red-500/15 text-red-700' },
  CANCELADA: { texto: 'Canceló', clase: 'bg-negro/10 text-negro/70' },
}

const claseBotonPestana = (activa) =>
  `rounded-lg px-4 py-2 text-sm font-medium transition ${
    activa ? 'bg-celeste text-negro' : 'border border-negro/20 text-negro hover:bg-negro/10'
  }`

// Lista de inscriptos de una clase, con los botones para marcar la asistencia.
function Inscriptos({ claseId }) {
  const [reservas, setReservas] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [marcando, setMarcando] = useState(null) // id de la reserva que se está guardando

  useEffect(() => {
    let cancelado = false
    clasesService
      .listarInscriptos(claseId)
      .then((datos) => {
        if (!cancelado) setReservas(datos)
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
  }, [claseId])

  const marcar = async (reserva, asistio) => {
    setError('')
    setMarcando(reserva.id)
    try {
      const actualizada = await clasesService.marcarAsistencia(reserva.id, asistio)
      setReservas((anteriores) => anteriores.map((r) => (r.id === actualizada.id ? actualizada : r)))
    } catch (err) {
      setError(mensajeDeError(err))
    } finally {
      setMarcando(null)
    }
  }

  if (cargando) return <p className="mt-4 text-sm text-negro/70">Cargando inscriptos...</p>

  const activas = reservas.filter((r) => r.estado !== 'CANCELADA')
  const asistieron = activas.filter((r) => r.estado === 'ASISTIO').length
  const ausentes = activas.filter((r) => r.estado === 'NO_ASISTIO').length
  const pendientes = activas.length - asistieron - ausentes

  return (
    <div className="mt-4 border-t border-negro/10 pt-4">
      {error && <p className="mb-3 rounded-lg bg-red-500/15 px-3 py-2 text-sm text-red-700">{error}</p>}

      {reservas.length === 0 ? (
        <p className="text-sm text-negro/65">Todavía no hay alumnos inscriptos en esta clase.</p>
      ) : (
        <>
          <p className="mb-3 text-sm text-negro/70">
            {activas.length} {activas.length === 1 ? 'inscripto' : 'inscriptos'} · {asistieron}{' '}
            {asistieron === 1 ? 'asistió' : 'asistieron'} · {ausentes} {ausentes === 1 ? 'ausente' : 'ausentes'} ·{' '}
            {pendientes} sin marcar
          </p>
          <ul className="divide-y divide-negro/10">
            {reservas.map((r) => {
              const estilo = ESTILO_ESTADO[r.estado] ?? ESTILO_ESTADO.CONFIRMADA
              const cancelada = r.estado === 'CANCELADA'
              return (
                <li key={r.id} className="flex flex-wrap items-center justify-between gap-3 py-2.5">
                  <div className="flex items-center gap-3">
                    <p className={`text-sm font-medium ${cancelada ? 'text-negro/65' : ''}`}>
                      {r.nombreAlumno ?? `Alumno #${r.alumnoId}`}
                    </p>
                    <span className={`rounded-full px-2 py-0.5 text-xs ${estilo.clase}`}>{estilo.texto}</span>
                  </div>
                  {!cancelada && (
                    <div className="flex gap-2">
                      <button
                        onClick={() => marcar(r, true)}
                        disabled={marcando !== null || r.estado === 'ASISTIO'}
                        className="rounded-lg border border-negro/20 px-3 py-1 text-xs text-negro transition hover:bg-negro/10 disabled:opacity-40"
                      >
                        {marcando === r.id ? 'Guardando...' : 'Asistió'}
                      </button>
                      <button
                        onClick={() => marcar(r, false)}
                        disabled={marcando !== null || r.estado === 'NO_ASISTIO'}
                        className="rounded-lg border border-negro/20 px-3 py-1 text-xs text-negro transition hover:bg-negro/10 disabled:opacity-40"
                      >
                        No asistió
                      </button>
                    </div>
                  )}
                </li>
              )
            })}
          </ul>
        </>
      )}
    </div>
  )
}

export default function ClasesProfesor() {
  const { usuario } = useAuth()
  const [clases, setClases] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [pestana, setPestana] = useState('proximas') // 'proximas' | 'pasadas'
  const [abierta, setAbierta] = useState(null) // id de la clase con los inscriptos desplegados
  // Momento en que se abrió la pantalla: separa las clases próximas de las pasadas.
  const [ahora] = useState(() => Date.now())

  useEffect(() => {
    let cancelado = false
    clasesService
      .listarDelProfesor(usuario.profesorId)
      .then((datos) => {
        if (!cancelado) setClases(datos)
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
  }, [usuario.profesorId])

  if (cargando) return <p className="text-negro/70">Cargando clases...</p>
  if (error) return <p className="text-red-700">{error}</p>

  const proximas = clases
    .filter((c) => new Date(c.horario).getTime() > ahora)
    .sort((a, b) => a.horario.localeCompare(b.horario))
  const pasadas = clases
    .filter((c) => new Date(c.horario).getTime() <= ahora)
    .sort((a, b) => b.horario.localeCompare(a.horario))
  const visibles = pestana === 'proximas' ? proximas : pasadas

  return (
    <div className="max-w-4xl">
      <h2 className="text-2xl font-bold">Mis clases</h2>
      <p className="mb-5 mt-1 text-sm text-negro/70">
        Mirá quiénes se inscribieron a tus clases y registrá la asistencia.
      </p>

      <div className="mb-6 flex gap-2">
        <button
          onClick={() => {
            setPestana('proximas')
            setAbierta(null)
          }}
          className={claseBotonPestana(pestana === 'proximas')}
        >
          Próximas ({proximas.length})
        </button>
        <button
          onClick={() => {
            setPestana('pasadas')
            setAbierta(null)
          }}
          className={claseBotonPestana(pestana === 'pasadas')}
        >
          Pasadas ({pasadas.length})
        </button>
      </div>

      {visibles.length === 0 ? (
        <p className="text-sm text-negro/65">
          {pestana === 'proximas' ? 'No tenés clases próximas.' : 'Todavía no hay clases pasadas.'}
        </p>
      ) : (
        <ul className="flex flex-col gap-3">
          {visibles.map((c) => {
            const ocupados = c.cupoMaximo - c.cupoDisponible
            const desplegada = abierta === c.id
            return (
              <li key={c.id} className="rounded-2xl border border-negro/10 bg-gris p-5">
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <div className="flex items-center gap-3">
                    <Ilustracion clave={claveActividad(c.tipoActividad)} variante="miniatura" />
                    <div>
                      <p className="font-semibold">{c.tipoActividad}</p>
                      <p className="text-sm text-negro/70">{formatearHorario(c.horario)}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-4">
                    <span className="text-sm text-negro/70">
                      {ocupados} de {c.cupoMaximo} lugares
                    </span>
                    <button
                      onClick={() => setAbierta(desplegada ? null : c.id)}
                      className="rounded-lg border border-negro/20 px-3 py-1.5 text-sm text-negro transition hover:bg-negro/10"
                    >
                      {desplegada ? 'Ocultar inscriptos' : 'Ver inscriptos'}
                    </button>
                  </div>
                </div>
                {desplegada && <Inscriptos claseId={c.id} />}
              </li>
            )
          })}
        </ul>
      )}
    </div>
  )
}