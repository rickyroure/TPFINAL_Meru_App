import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import { ejerciciosService } from '../../services/ejercicios'
import { rutinasService } from '../../services/rutinas'
import { mensajeDeError } from '../../utils/errores'

const claseInput =
  'w-full rounded-lg border border-neutral-700 bg-neutral-950 px-3 py-2 text-sm outline-none focus:border-emerald-500'
const SESIONES = ['A', 'B', 'C', 'D', 'E']

// Convierte un campo de texto opcional en número (o null si está vacío).
const numeroOpcional = (valor) => (valor === '' ? null : Number(valor))

export default function NuevaRutina() {
  const { usuario } = useAuth()
  const navigate = useNavigate()

  const [ejercicios, setEjercicios] = useState([])
  const [errorCarga, setErrorCarga] = useState('')

  const [nombre, setNombre] = useState('')
  const [duracion, setDuracion] = useState('4')
  const [items, setItems] = useState([])

  // Formulario del ejercicio que se está agregando.
  const [semana, setSemana] = useState('1')
  const [sesion, setSesion] = useState('A')
  const [ejercicioId, setEjercicioId] = useState('')
  const [series, setSeries] = useState('3')
  const [reps, setReps] = useState('10')
  const [pausa, setPausa] = useState('')
  const [carga, setCarga] = useState('')
  const [rpe, setRpe] = useState('')

  const [errorItem, setErrorItem] = useState('')
  const [error, setError] = useState('')
  const [guardando, setGuardando] = useState(false)

  useEffect(() => {
    let cancelado = false
    ejerciciosService
      .listar()
      .then((datos) => {
        if (!cancelado) setEjercicios(datos)
      })
      .catch((err) => {
        if (!cancelado) setErrorCarga(mensajeDeError(err))
      })
    return () => {
      cancelado = true
    }
  }, [])

  const semanasTotales = Math.max(1, Number(duracion) || 1)
  const nombreDe = (id) => ejercicios.find((e) => e.id === id)?.nombre ?? `Ejercicio ${id}`

  const agregarEjercicio = (evento) => {
    evento.preventDefault()
    setErrorItem('')
    // Mismas reglas que PlanEjercicioItemDTO del backend.
    if (!ejercicioId) return setErrorItem('Elegí un ejercicio.')
    if (Number(semana) < 1 || Number(semana) > semanasTotales) {
      return setErrorItem(`La semana tiene que estar entre 1 y ${semanasTotales}.`)
    }
    if (Number(series) < 1) return setErrorItem('Debe haber al menos 1 serie.')
    if (Number(reps) < 1) return setErrorItem('Debe haber al menos 1 repetición.')
    if (carga !== '' && (Number(carga) < 0 || Number(carga) > 100)) {
      return setErrorItem('La carga debe estar entre 0 y 100.')
    }
    if (rpe !== '' && (Number(rpe) < 1 || Number(rpe) > 10)) return setErrorItem('El RPE debe estar entre 1 y 10.')

    const mismoGrupo = items.filter((i) => i.numeroSemana === Number(semana) && i.sesion === sesion)
    setItems((anteriores) => [
      ...anteriores,
      {
        ejercicioId: Number(ejercicioId),
        sesion,
        numeroSemana: Number(semana),
        orden: mismoGrupo.length + 1,
        series: Number(series),
        reps: Number(reps),
        pausaSegundos: numeroOpcional(pausa),
        cargaPct: numeroOpcional(carga),
        rpe: numeroOpcional(rpe),
      },
    ])
    setEjercicioId('')
  }

  const quitar = (indice) => setItems((anteriores) => anteriores.filter((_, i) => i !== indice))

  // Copia lo cargado en la semana 1 a las semanas 2..N. Reemplaza lo que hubiera en esas semanas.
  const repetirSemanaUno = () => {
    const base = items.filter((i) => i.numeroSemana === 1)
    const copias = []
    for (let s = 2; s <= semanasTotales; s++) {
      base.forEach((i) => copias.push({ ...i, numeroSemana: s }))
    }
    setItems([...base, ...copias])
  }

  const guardar = async (evento) => {
    evento.preventDefault()
    setError('')
    if (!nombre.trim()) return setError('El nombre de la rutina es obligatorio.')
    if (Number(duracion) < 1) return setError('La duración mínima es de 1 semana.')
    if (items.length === 0) return setError('Agregá al menos un ejercicio.')
    if (items.some((i) => i.numeroSemana > Number(duracion))) {
      return setError('Hay ejercicios en semanas posteriores a la duración. Quitalos o aumentá la duración.')
    }
    setGuardando(true)
    try {
      await rutinasService.crearPlantilla({
        profesorId: usuario.profesorId,
        nombre: nombre.trim(),
        duracionSemanas: Number(duracion),
        items,
      })
      navigate('/profesor/rutinas')
    } catch (err) {
      setError(mensajeDeError(err))
      setGuardando(false)
    }
  }

  const semanasConItems = [...new Set(items.map((i) => i.numeroSemana))].sort((a, b) => a - b)

  return (
    <div className="max-w-4xl">
      <Link to="/profesor/rutinas" className="text-sm text-emerald-400 hover:underline">
        ← Volver a rutinas
      </Link>
      <h2 className="mb-6 mt-3 text-2xl font-bold">Nueva plantilla de rutina</h2>
      {errorCarga && <p className="mb-4 text-red-400">{errorCarga}</p>}

      <div className="mb-6 grid gap-3 sm:grid-cols-3">
        <label className="flex flex-col gap-1 text-xs text-neutral-400 sm:col-span-2">
          Nombre de la rutina
          <input className={claseInput} value={nombre} onChange={(e) => setNombre(e.target.value)} />
        </label>
        <label className="flex flex-col gap-1 text-xs text-neutral-400">
          Duración (semanas)
          <input
            type="number"
            min="1"
            className={claseInput}
            value={duracion}
            onChange={(e) => setDuracion(e.target.value)}
          />
        </label>
      </div>

      <form onSubmit={agregarEjercicio} className="mb-6 rounded-2xl border border-neutral-800 bg-neutral-900 p-5">
        <h3 className="mb-3 font-semibold">Agregar ejercicio</h3>
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
          <label className="flex flex-col gap-1 text-xs text-neutral-400">
            Semana
            <input type="number" min="1" className={claseInput} value={semana} onChange={(e) => setSemana(e.target.value)} />
          </label>
          <label className="flex flex-col gap-1 text-xs text-neutral-400">
            Sesión
            <select className={claseInput} value={sesion} onChange={(e) => setSesion(e.target.value)}>
              {SESIONES.map((s) => (
                <option key={s} value={s}>
                  Sesión {s}
                </option>
              ))}
            </select>
          </label>
          <label className="col-span-2 flex flex-col gap-1 text-xs text-neutral-400">
            Ejercicio
            <select className={claseInput} value={ejercicioId} onChange={(e) => setEjercicioId(e.target.value)}>
              <option value="">Elegir...</option>
              {ejercicios.map((e) => (
                <option key={e.id} value={e.id}>
                  {e.nombre} ({e.grupoMuscular})
                </option>
              ))}
            </select>
          </label>
          <label className="flex flex-col gap-1 text-xs text-neutral-400">
            Series
            <input type="number" min="1" className={claseInput} value={series} onChange={(e) => setSeries(e.target.value)} />
          </label>
          <label className="flex flex-col gap-1 text-xs text-neutral-400">
            Reps
            <input type="number" min="1" className={claseInput} value={reps} onChange={(e) => setReps(e.target.value)} />
          </label>
          <label className="flex flex-col gap-1 text-xs text-neutral-400">
            Pausa (seg)
            <input type="number" min="0" className={claseInput} value={pausa} onChange={(e) => setPausa(e.target.value)} />
          </label>
          <label className="flex flex-col gap-1 text-xs text-neutral-400">
            Carga (% 1RM)
            <input type="number" min="0" max="100" step="0.5" className={claseInput} value={carga} onChange={(e) => setCarga(e.target.value)} />
          </label>
          <label className="flex flex-col gap-1 text-xs text-neutral-400">
            RPE (1-10)
            <input type="number" min="1" max="10" step="0.5" className={claseInput} value={rpe} onChange={(e) => setRpe(e.target.value)} />
          </label>
        </div>
        <button
          type="submit"
          className="mt-4 rounded-lg border border-emerald-500/60 px-4 py-2 text-sm text-emerald-400 transition hover:bg-emerald-500/10"
        >
          + Agregar a la rutina
        </button>
        {errorItem && <p className="mt-2 text-sm text-red-400">{errorItem}</p>}
      </form>

      <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
        <h3 className="font-semibold">Ejercicios de la rutina ({items.length})</h3>
        {semanasTotales > 1 && items.some((i) => i.numeroSemana === 1) && (
          <button
            type="button"
            onClick={repetirSemanaUno}
            className="text-sm text-emerald-400 hover:underline"
          >
            Repetir semana 1 en las demás semanas
          </button>
        )}
      </div>

      {items.length === 0 ? (
        <p className="mb-6 text-sm text-neutral-500">Todavía no agregaste ejercicios.</p>
      ) : (
        <div className="mb-6 flex flex-col gap-4">
          {semanasConItems.map((n) => (
            <section key={n} className="rounded-2xl border border-neutral-800 bg-neutral-900 p-4">
              <h4 className="mb-2 text-sm font-semibold text-emerald-500">Semana {n}</h4>
              <ul className="divide-y divide-neutral-800 text-sm">
                {items
                  .map((item, indice) => ({ item, indice }))
                  .filter(({ item }) => item.numeroSemana === n)
                  .sort((a, b) => a.item.sesion.localeCompare(b.item.sesion) || a.item.orden - b.item.orden)
                  .map(({ item, indice }) => (
                    <li key={indice} className="flex items-center justify-between gap-3 py-2">
                      <span>
                        <span className="mr-2 rounded bg-neutral-800 px-2 py-0.5 text-xs">Sesión {item.sesion}</span>
                        {nombreDe(item.ejercicioId)} · {item.series}×{item.reps}
                        {item.cargaPct != null && ` · ${item.cargaPct}%`}
                        {item.rpe != null && ` · RPE ${item.rpe}`}
                      </span>
                      <button
                        type="button"
                        onClick={() => quitar(indice)}
                        className="text-xs text-red-400 hover:underline"
                      >
                        Quitar
                      </button>
                    </li>
                  ))}
              </ul>
            </section>
          ))}
        </div>
      )}

      <form onSubmit={guardar}>
        <button
          type="submit"
          disabled={guardando}
          className="rounded-lg bg-emerald-500 px-5 py-2.5 text-sm font-medium text-neutral-950 transition hover:bg-emerald-400 disabled:opacity-60"
        >
          {guardando ? 'Guardando...' : 'Guardar plantilla'}
        </button>
        {error && <p className="mt-3 text-sm text-red-400">{error}</p>}
      </form>
    </div>
  )
}