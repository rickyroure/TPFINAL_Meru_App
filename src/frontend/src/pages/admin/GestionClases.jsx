import { useEffect, useState } from 'react'
import { clasesService } from '../../services/clases'
import { mensajeDeError } from '../../utils/errores'

const claseInput =
  'w-full rounded-lg border border-negro/20 bg-fondo px-3 py-2 text-sm outline-none focus:border-celeste'

const formatoHorario = (horario) =>
  new Date(horario).toLocaleString('es-AR', { dateStyle: 'medium', timeStyle: 'short' })

export default function GestionClases() {
  const [clases, setClases] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [aviso, setAviso] = useState('')
  // Momento de la carga: sirve para marcar qué clases ya finalizaron.
  const [ahora, setAhora] = useState(0)

  const [tipoActividad, setTipoActividad] = useState('')
  const [horario, setHorario] = useState('')
  const [cupo, setCupo] = useState('10')
  const [profesorId, setProfesorId] = useState('1')
  const [guardando, setGuardando] = useState(false)
  const [errorForm, setErrorForm] = useState('')

  useEffect(() => {
    let cancelado = false
    clasesService
      .listarTodas()
      .then((datos) => {
        if (cancelado) return
        setClases(datos)
        setAhora(Date.now())
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
  }, [])

  const crear = async (evento) => {
    evento.preventDefault()
    setErrorForm('')
    setAviso('')
    setGuardando(true)
    try {
      const nueva = await clasesService.crear({
        profesorId: profesorId === '' ? null : Number(profesorId),
        tipoActividad: tipoActividad.trim(),
        horario,
        cupoMaximo: cupo === '' ? null : Number(cupo),
      })
      setClases((anteriores) => [...anteriores, nueva].sort((a, b) => a.horario.localeCompare(b.horario)))
      setAviso(`Clase de ${nueva.tipoActividad} creada.`)
      setTipoActividad('')
      setHorario('')
    } catch (err) {
      setErrorForm(mensajeDeError(err))
    } finally {
      setGuardando(false)
    }
  }

  if (cargando) return <p className="text-negro/70">Cargando clases...</p>
  if (error) return <p className="text-red-700">{error}</p>

  return (
    <div className="max-w-4xl">
      <h2 className="text-2xl font-bold">Clases</h2>
      <p className="mb-6 mt-1 text-sm text-negro/70">Alta de clases y vista de ocupación.</p>

      <form onSubmit={crear} className="mb-6 grid gap-3 rounded-2xl border border-negro/10 bg-gris p-5 sm:grid-cols-4">
        <h3 className="font-semibold sm:col-span-4">Nueva clase</h3>
        <label className="flex flex-col gap-1 text-xs text-negro/70">
          Actividad
          <input
            className={claseInput}
            placeholder="Yoga, Spinning..."
            value={tipoActividad}
            onChange={(e) => setTipoActividad(e.target.value)}
          />
        </label>
        <label className="flex flex-col gap-1 text-xs text-negro/70">
          Día y hora
          <input type="datetime-local" className={claseInput} value={horario} onChange={(e) => setHorario(e.target.value)} />
        </label>
        <label className="flex flex-col gap-1 text-xs text-negro/70">
          Cupo máximo
          <input type="number" min="1" className={claseInput} value={cupo} onChange={(e) => setCupo(e.target.value)} />
        </label>
        <label className="flex flex-col gap-1 text-xs text-negro/70">
          ID del profesor
          <input type="number" min="1" className={claseInput} value={profesorId} onChange={(e) => setProfesorId(e.target.value)} />
        </label>
        <button
          type="submit"
          disabled={guardando}
          className="rounded-lg bg-celeste px-4 py-2 text-sm font-medium text-negro transition hover:bg-celeste/80 disabled:opacity-60 sm:col-span-4 sm:w-fit"
        >
          {guardando ? 'Creando...' : 'Crear clase'}
        </button>
        {errorForm && <p className="text-sm text-red-700 sm:col-span-4">{errorForm}</p>}
      </form>

      {aviso && <p className="mb-4 rounded-lg bg-celeste/10 px-4 py-2 text-sm text-azul">{aviso}</p>}

      {clases.length === 0 ? (
        <p className="text-negro/70">Todavía no hay clases cargadas.</p>
      ) : (
        <ul className="flex flex-col gap-3">
          {clases.map((c) => {
            const pasada = new Date(c.horario).getTime() <= ahora
            const ocupados = c.cupoMaximo - c.cupoDisponible
            return (
              <li
                key={c.id}
                className={`flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-negro/10 bg-gris p-4 ${
                  pasada ? 'opacity-60' : ''
                }`}
              >
                <div>
                  <p className="font-semibold">{c.tipoActividad}</p>
                  <p className="text-sm text-negro/70">
                    {formatoHorario(c.horario)} · {c.nombreProfesor}
                  </p>
                </div>
                <div className="flex items-center gap-3 text-sm">
                  <span className="text-negro">
                    {ocupados}/{c.cupoMaximo} lugares
                  </span>
                  <span
                    className={`rounded-full px-3 py-1 text-xs font-medium ${
                      pasada
                        ? 'bg-negro/10 text-negro/70'
                        : c.cupoDisponible === 0
                          ? 'bg-amber-500/15 text-amber-700'
                          : 'bg-celeste/15 text-azul'
                    }`}
                  >
                    {pasada ? 'Finalizada' : c.cupoDisponible === 0 ? 'Completa' : 'Con cupo'}
                  </span>
                </div>
              </li>
            )
          })}
        </ul>
      )}
    </div>
  )
}