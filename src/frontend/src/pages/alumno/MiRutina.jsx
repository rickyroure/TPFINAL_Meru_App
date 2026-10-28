import { useEffect, useState } from 'react'
import { useAuth } from '../../hooks/useAuth'
import { rutinasService } from '../../services/rutinas'
import { mensajeDeError } from '../../utils/errores'

const claseInput =
  'w-full rounded-lg border border-neutral-700 bg-neutral-950 px-3 py-2 text-sm outline-none focus:border-emerald-500'

// Etiqueta chica con un dato de la prescripción (series, pausa, RPE...).
function Dato({ titulo, valor }) {
  return (
    <div className="rounded-lg bg-neutral-950 px-3 py-2 text-center">
      <p className="text-[10px] uppercase tracking-wide text-neutral-500">{titulo}</p>
      <p className="text-sm font-medium">{valor}</p>
    </div>
  )
}

// Un ejercicio del plan, con el registro de lo que el alumno realmente hizo en cada serie.
function TarjetaEjercicio({ plan }) {
  const [progreso, setProgreso] = useState([])
  const [cargandoProgreso, setCargandoProgreso] = useState(true)
  const [numeroSerie, setNumeroSerie] = useState('1')
  const [peso, setPeso] = useState('')
  const [reps, setReps] = useState(String(plan.reps ?? ''))
  const [guardando, setGuardando] = useState(false)
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
        if (!cancelado) setCargandoProgreso(false)
      })
    return () => {
      cancelado = true
    }
  }, [plan.id])

  const registrar = async (evento) => {
    evento.preventDefault()
    setError('')
    setGuardando(true)
    try {
      const nuevo = await rutinasService.registrarProgreso({
        planEjercicioId: plan.id,
        numeroSerie: Number(numeroSerie),
        pesoRealizado: peso === '' ? null : Number(peso),
        repeticionesRealizadas: reps === '' ? null : Number(reps),
      })
      setProgreso((anterior) => [...anterior, nuevo])
      // Deja lista la serie siguiente.
      setNumeroSerie(String(Number(numeroSerie) + 1))
    } catch (err) {
      setError(mensajeDeError(err))
    } finally {
      setGuardando(false)
    }
  }

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

      <h5 className="mb-2 text-xs uppercase tracking-wide text-neutral-500">Lo que hice</h5>
      {cargandoProgreso ? (
        <p className="text-sm text-neutral-500">Cargando...</p>
      ) : progreso.length === 0 ? (
        <p className="text-sm text-neutral-500">Todavía no registraste ninguna serie.</p>
      ) : (
        <ul className="mb-3 divide-y divide-neutral-800 text-sm">
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

      <form onSubmit={registrar} className="mt-3 grid grid-cols-3 items-end gap-2 sm:grid-cols-4">
        <label className="flex flex-col gap-1 text-xs text-neutral-400">
          Serie
          <input
            type="number"
            min="1"
            className={claseInput}
            value={numeroSerie}
            onChange={(e) => setNumeroSerie(e.target.value)}
          />
        </label>
        <label className="flex flex-col gap-1 text-xs text-neutral-400">
          Peso (kg)
          <input
            type="number"
            min="0"
            step="0.5"
            className={claseInput}
            value={peso}
            onChange={(e) => setPeso(e.target.value)}
          />
        </label>
        <label className="flex flex-col gap-1 text-xs text-neutral-400">
          Reps
          <input
            type="number"
            min="0"
            className={claseInput}
            value={reps}
            onChange={(e) => setReps(e.target.value)}
          />
        </label>
        <button
          type="submit"
          disabled={guardando}
          className="col-span-3 rounded-lg bg-emerald-500 px-3 py-2 text-sm font-medium text-neutral-950 transition hover:bg-emerald-400 disabled:opacity-60 sm:col-span-1"
        >
          {guardando ? 'Guardando...' : 'Registrar'}
        </button>
      </form>
      {error && <p className="mt-2 text-sm text-red-400">{error}</p>}
    </article>
  )
}

export default function MiRutina() {
  const { usuario } = useAuth()
  const [rutinas, setRutinas] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [semana, setSemana] = useState(1)

  useEffect(() => {
    let cancelado = false
    rutinasService
      .listarPorAlumno(usuario.alumnoId)
      .then((datos) => {
        if (!cancelado) setRutinas(datos)
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
  }, [usuario.alumnoId])

  if (cargando) return <p className="text-neutral-400">Cargando rutina...</p>
  if (error) return <p className="text-red-400">{error}</p>
  if (rutinas.length === 0) {
    return (
      <div>
        <h2 className="text-2xl font-bold">Mi rutina</h2>
        <p className="mt-2 text-neutral-400">Todavía no tenés una rutina asignada. Hablá con tu profesor.</p>
      </div>
    )
  }

  // Por ahora se muestra la primera rutina asignada.
  const rutina = rutinas[0]
  const semanas = [...new Set(rutina.items.map((i) => i.numeroSemana))].sort((a, b) => a - b)
  const itemsDeLaSemana = rutina.items.filter((i) => i.numeroSemana === semana)
  const sesiones = [...new Set(itemsDeLaSemana.map((i) => i.sesion))]

  return (
    <div className="max-w-4xl">
      <h2 className="text-2xl font-bold">{rutina.nombre}</h2>
      <p className="mb-6 mt-1 text-sm text-neutral-400">
        Profesor: {rutina.nombreProfesor} · Inicio: {rutina.fechaInicio} · {rutina.duracionSemanas} semanas
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
          <h3 className="mb-3 text-lg font-semibold text-emerald-500">{sesion}</h3>
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
    </div>
  )
}