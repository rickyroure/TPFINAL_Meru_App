import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import { profesoresService } from '../../services/profesores'
import { rutinasService } from '../../services/rutinas'
import { mensajeDeError } from '../../utils/errores'

const claseInput =
  'w-full rounded-lg border border-neutral-700 bg-neutral-950 px-3 py-2 text-sm outline-none focus:border-emerald-500'

// Panel que aparece al tocar "Asignar" en una plantilla.
function FormularioAsignar({ plantilla, alumnos, onAsignada, onCancelar }) {
  const [alumnoId, setAlumnoId] = useState('')
  const [fechaInicio, setFechaInicio] = useState(new Date().toISOString().slice(0, 10))
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState('')

  const asignar = async (evento) => {
    evento.preventDefault()
    setError('')
    if (!alumnoId) {
      setError('Elegí un alumno.')
      return
    }
    setGuardando(true)
    try {
      const rutina = await rutinasService.asignar(plantilla.id, {
        alumnoId: Number(alumnoId),
        fechaInicio,
      })
      onAsignada(rutina)
    } catch (err) {
      setError(mensajeDeError(err))
      setGuardando(false)
    }
  }

  return (
    <form onSubmit={asignar} className="mt-4 grid gap-3 border-t border-neutral-800 pt-4 sm:grid-cols-3">
      <label className="flex flex-col gap-1 text-xs text-neutral-400">
        Alumno
        <select className={claseInput} value={alumnoId} onChange={(e) => setAlumnoId(e.target.value)}>
          <option value="">Elegir...</option>
          {alumnos.map((a) => (
            <option key={a.id} value={a.id}>
              {a.nombre} {a.apellido}
            </option>
          ))}
        </select>
      </label>
      <label className="flex flex-col gap-1 text-xs text-neutral-400">
        Fecha de inicio
        <input type="date" className={claseInput} value={fechaInicio} onChange={(e) => setFechaInicio(e.target.value)} />
      </label>
      <div className="flex items-end gap-2">
        <button
          type="submit"
          disabled={guardando}
          className="rounded-lg bg-emerald-500 px-4 py-2 text-sm font-medium text-neutral-950 transition hover:bg-emerald-400 disabled:opacity-60"
        >
          {guardando ? 'Asignando...' : 'Confirmar'}
        </button>
        <button
          type="button"
          onClick={onCancelar}
          className="rounded-lg border border-neutral-700 px-4 py-2 text-sm text-neutral-300 hover:bg-neutral-800"
        >
          Cancelar
        </button>
      </div>
      {error && <p className="text-sm text-red-400 sm:col-span-3">{error}</p>}
    </form>
  )
}

export default function Rutinas() {
  const { usuario } = useAuth()
  const [rutinas, setRutinas] = useState([])
  const [alumnos, setAlumnos] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [asignando, setAsignando] = useState(null)
  const [aviso, setAviso] = useState('')
  const [version, setVersion] = useState(0)

  useEffect(() => {
    let cancelado = false
    Promise.all([rutinasService.listarPorProfesor(usuario.profesorId), profesoresService.listarAlumnos(usuario.profesorId)])
      .then(([listaRutinas, listaAlumnos]) => {
        if (cancelado) return
        setRutinas(listaRutinas)
        setAlumnos(listaAlumnos)
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
  }, [usuario.profesorId, version])

  if (cargando) return <p className="text-neutral-400">Cargando rutinas...</p>
  if (error) return <p className="text-red-400">{error}</p>

  // Plantilla = rutina sin alumno. Las demás son copias ya asignadas.
  const plantillas = rutinas.filter((r) => r.alumnoId === null)
  const asignadas = rutinas.filter((r) => r.alumnoId !== null)

  const alAsignar = (rutina) => {
    setAsignando(null)
    setAviso(`Rutina "${rutina.nombre}" asignada a ${rutina.nombreAlumno}.`)
    setVersion((v) => v + 1)
  }

  return (
    <div className="max-w-4xl">
      <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
        <h2 className="text-2xl font-bold">Rutinas</h2>
        <Link
          to="/profesor/rutinas/nueva"
          className="rounded-lg bg-emerald-500 px-4 py-2 text-sm font-medium text-neutral-950 transition hover:bg-emerald-400"
        >
          + Nueva plantilla
        </Link>
      </div>

      {aviso && (
        <p className="mb-4 rounded-lg bg-emerald-500/10 px-4 py-2 text-sm text-emerald-400">{aviso}</p>
      )}

      <h3 className="mb-3 text-lg font-semibold text-emerald-500">Mis plantillas</h3>
      {plantillas.length === 0 ? (
        <p className="mb-8 text-neutral-400">Todavía no creaste plantillas.</p>
      ) : (
        <ul className="mb-8 flex flex-col gap-3">
          {plantillas.map((p) => (
            <li key={p.id} className="rounded-2xl border border-neutral-800 bg-neutral-900 p-4">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <p className="font-semibold">{p.nombre}</p>
                  <p className="text-sm text-neutral-400">
                    {p.duracionSemanas} {p.duracionSemanas === 1 ? 'semana' : 'semanas'} · {p.items.length} ejercicios
                  </p>
                </div>
                <button
                  onClick={() => setAsignando(asignando === p.id ? null : p.id)}
                  className="rounded-lg border border-emerald-500/60 px-4 py-2 text-sm text-emerald-400 transition hover:bg-emerald-500/10"
                >
                  Asignar a un alumno
                </button>
              </div>
              {asignando === p.id && (
                <FormularioAsignar
                  plantilla={p}
                  alumnos={alumnos}
                  onAsignada={alAsignar}
                  onCancelar={() => setAsignando(null)}
                />
              )}
            </li>
          ))}
        </ul>
      )}

      <h3 className="mb-3 text-lg font-semibold text-emerald-500">Rutinas asignadas</h3>
      {asignadas.length === 0 ? (
        <p className="text-neutral-400">Todavía no asignaste rutinas.</p>
      ) : (
        <ul className="flex flex-col gap-2">
          {asignadas.map((r) => (
            <li key={r.id}>
              <Link
                to={`/profesor/alumnos/${r.alumnoId}`}
                className="flex flex-wrap items-center justify-between gap-2 rounded-xl border border-neutral-800 bg-neutral-900 px-4 py-3 text-sm transition hover:border-emerald-500/60"
              >
                <span className="font-medium">{r.nombre}</span>
                <span className="text-neutral-400">
                  {r.nombreAlumno} · desde {r.fechaInicio}
                </span>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}