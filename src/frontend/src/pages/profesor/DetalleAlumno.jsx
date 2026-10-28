import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import { profesoresService } from '../../services/profesores'
import { rutinasService } from '../../services/rutinas'
import { mensajeDeError } from '../../utils/errores'

function Dato({ titulo, valor }) {
  return (
    <div className="rounded-lg bg-neutral-950 px-3 py-2 text-center">
      <p className="text-[10px] uppercase tracking-wide text-neutral-500">{titulo}</p>
      <p className="text-sm font-medium">{valor}</p>
    </div>
  )
}

// Un ejercicio del plan con lo que el alumno realmente registró (solo lectura).
function TarjetaEjercicio({ plan }) {
  const [progreso, setProgreso] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelado = false
    rutinasService
      .listarProgreso(plan.id)
      .then((datos) => {
        if (!cancelado) setProgreso(datos)
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
  }, [plan.id])

  return (
    <article className="rounded-2xl border border-neutral-800 bg-neutral-900 p-5">
      <h4 className="mb-3 font-semibold">{plan.nombreEjercicio}</h4>
      <div className="mb-4 grid grid-cols-2 gap-2 sm:grid-cols-5">
        <Dato titulo="Series" valor={plan.series} />
        <Dato titulo="Reps" valor={plan.reps} />
        <Dato titulo="Pausa" valor={plan.pausaSegundos ? `${plan.pausaSegundos} s` : '-'} />
        <Dato titulo="Carga" valor={plan.cargaPct ? `${plan.cargaPct}% 1RM` : '-'} />
        <Dato titulo="RPE" valor={plan.rpe ?? '-'} />
      </div>

      <h5 className="mb-2 text-xs uppercase tracking-wide text-neutral-500">Lo que hizo el alumno</h5>
      {cargando ? (
        <p className="text-sm text-neutral-500">Cargando...</p>
      ) : error ? (
        <p className="text-sm text-red-400">{error}</p>
      ) : progreso.length === 0 ? (
        <p className="text-sm text-neutral-500">Todavía no registró ninguna serie.</p>
      ) : (
        <ul className="divide-y divide-neutral-800 text-sm">
          {progreso.map((p) => (
            <li key={p.id} className="flex justify-between py-1.5">
              <span className="text-neutral-400">
                Serie {p.numeroSerie} · {p.fecha}
              </span>
              <span>
                {p.pesoRealizado} kg × {p.repeticionesRealizadas ?? '-'}
              </span>
            </li>
          ))}
        </ul>
      )}
    </article>
  )
}

export default function DetalleAlumno() {
  const { alumnoId } = useParams()
  const { usuario } = useAuth()
  const id = Number(alumnoId)

  const [alumno, setAlumno] = useState(null)
  const [rutinas, setRutinas] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [semana, setSemana] = useState(1)

  useEffect(() => {
    let cancelado = false
    // El alumno se busca entre los asignados al profesor: así no puede abrir alumnos ajenos.
    Promise.all([profesoresService.listarAlumnos(usuario.profesorId), rutinasService.listarPorAlumno(id)])
      .then(([asignados, rutinasDelAlumno]) => {
        if (cancelado) return
        setAlumno(asignados.find((a) => a.id === id) ?? null)
        setRutinas(rutinasDelAlumno)
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
  }, [id, usuario.profesorId])

  const volver = (
    <Link to="/profesor/alumnos" className="text-sm text-emerald-400 hover:underline">
      ← Volver a mis alumnos
    </Link>
  )

  if (cargando) return <p className="text-neutral-400">Cargando alumno...</p>
  if (error) return <p className="text-red-400">{error}</p>
  if (!alumno) {
    return (
      <div>
        {volver}
        <p className="mt-4 text-neutral-400">Ese alumno no está entre tus alumnos asignados.</p>
      </div>
    )
  }

  // Por ahora se muestra la primera rutina asignada.
  const rutina = rutinas[rutinas.length - 1]
  const semanas = rutina ? [...new Set(rutina.items.map((i) => i.numeroSemana))].sort((a, b) => a - b) : []
  const itemsDeLaSemana = rutina ? rutina.items.filter((i) => i.numeroSemana === semana) : []
  const sesiones = [...new Set(itemsDeLaSemana.map((i) => i.sesion))].sort()

  return (
    <div className="max-w-4xl">
      {volver}
      <div className="mb-6 mt-3 flex flex-wrap items-center gap-3">
        <h2 className="text-2xl font-bold">
          {alumno.nombre} {alumno.apellido}
        </h2>
        <span
          className={`rounded-full px-3 py-1 text-xs font-medium ${
            alumno.aptoFisico ? 'bg-emerald-500/15 text-emerald-400' : 'bg-amber-500/15 text-amber-400'
          }`}
        >
          {alumno.aptoFisico ? 'Apto físico' : 'Sin apto físico'}
        </span>
      </div>
      <p className="mb-6 text-sm text-neutral-400">
        {alumno.email} · Ingresó el {alumno.fechaIngreso}
      </p>

      {!rutina ? (
        <div className="rounded-2xl border border-neutral-800 bg-neutral-900 p-5 text-neutral-400">
          Este alumno todavía no tiene una rutina asignada.
        </div>
      ) : (
        <>
          <h3 className="text-xl font-semibold">{rutina.nombre}</h3>
          <p className="mb-4 mt-1 text-sm text-neutral-400">
            Inicio: {rutina.fechaInicio} · {rutina.duracionSemanas} semanas
          </p>

          <div className="mb-6 flex flex-wrap gap-2">
            {semanas.map((n) => (
              <button
                key={n}
                onClick={() => setSemana(n)}
                className={`rounded-lg px-3 py-1.5 text-sm font-medium transition ${
                  n === semana
                    ? 'bg-emerald-500/15 text-emerald-400'
                    : 'border border-neutral-700 text-neutral-300 hover:bg-neutral-800'
                }`}
              >
                Semana {n}
              </button>
            ))}
          </div>

          {sesiones.map((sesion) => (
            <section key={sesion} className="mb-8">
              <h4 className="mb-3 text-lg font-semibold text-emerald-500">Sesión {sesion}</h4>
              <div className="flex flex-col gap-4">
                {itemsDeLaSemana
                  .filter((i) => i.sesion === sesion)
                  .sort((a, b) => a.orden - b.orden)
                  .map((plan) => (
                    <TarjetaEjercicio key={plan.id} plan={plan} />
                  ))}
              </div>
            </section>
          ))}
        </>
      )}
    </div>
  )
}