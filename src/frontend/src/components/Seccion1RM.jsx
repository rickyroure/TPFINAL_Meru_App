import { useEffect, useState } from 'react'
import { ejerciciosService } from '../services/ejercicios'
import { rmService } from '../services/rm'
import { mensajeDeError } from '../utils/errores'

const claseInput =
  'w-full rounded-lg border border-neutral-700 bg-neutral-950 px-3 py-2 text-sm outline-none focus:border-emerald-500'

// Máximos de un alumno: el valor actual de cada ejercicio, el historial y un formulario para registrar uno nuevo.
export default function Seccion1RM({ alumnoId }) {
  const [registros, setRegistros] = useState([])
  const [ejercicios, setEjercicios] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')

  const [ejercicioId, setEjercicioId] = useState('')
  const [valor, setValor] = useState('')
  const [fecha, setFecha] = useState('')
  const [guardando, setGuardando] = useState(false)
  const [errorForm, setErrorForm] = useState('')

  useEffect(() => {
    let cancelado = false
    Promise.all([rmService.listarHistorico(alumnoId), ejerciciosService.listar()])
      .then(([historico, catalogo]) => {
        if (cancelado) return
        setRegistros(historico)
        setEjercicios(catalogo)
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
  }, [alumnoId])

  const registrar = async (evento) => {
    evento.preventDefault()
    setErrorForm('')
    if (!ejercicioId) return setErrorForm('Elegí un ejercicio.')
    if (valor === '' || Number(valor) <= 0) return setErrorForm('El valor del 1RM debe ser mayor a 0.')
    setGuardando(true)
    try {
      const nuevo = await rmService.registrar(alumnoId, {
        ejercicioId: Number(ejercicioId),
        valor1rm: Number(valor),
        fechaMedicion: fecha || null,
      })
      setRegistros((anteriores) =>
        [nuevo, ...anteriores].sort((a, b) => b.fechaMedicion.localeCompare(a.fechaMedicion) || b.id - a.id),
      )
      setValor('')
      setEjercicioId('')
    } catch (err) {
      setErrorForm(mensajeDeError(err))
    } finally {
      setGuardando(false)
    }
  }

  if (cargando) return <p className="mb-8 text-sm text-neutral-500">Cargando máximos...</p>
  if (error) return <p className="mb-8 text-sm text-red-400">{error}</p>

  // La lista viene ordenada de la medición más nueva a la más vieja: la primera de cada ejercicio es la actual.
  const actuales = []
  for (const r of registros) {
    if (!actuales.some((a) => a.ejercicioId === r.ejercicioId)) actuales.push(r)
  }

  return (
    <section className="mb-8 rounded-2xl border border-neutral-800 bg-neutral-900 p-5">
      <h3 className="mb-1 text-lg font-semibold">Máximos (1RM)</h3>
      <p className="mb-4 text-sm text-neutral-400">
        Con estos valores se calcula el peso de cada ejercicio a partir del % de carga de la rutina.
      </p>

      {actuales.length === 0 ? (
        <p className="mb-4 text-sm text-neutral-500">Todavía no hay máximos registrados.</p>
      ) : (
        <div className="mb-4 grid grid-cols-2 gap-2 sm:grid-cols-4">
          {actuales.map((r) => (
            <div key={r.ejercicioId} className="rounded-lg bg-neutral-950 px-3 py-2 text-center">
              <p className="text-[10px] uppercase tracking-wide text-neutral-500">{r.nombreEjercicio}</p>
              <p className="text-sm font-medium">{r.valor1rm} kg</p>
            </div>
          ))}
        </div>
      )}

      <form onSubmit={registrar} className="grid grid-cols-2 items-end gap-3 sm:grid-cols-5">
        <label className="col-span-2 flex flex-col gap-1 text-xs text-neutral-400">
          Ejercicio
          <select className={claseInput} value={ejercicioId} onChange={(e) => setEjercicioId(e.target.value)}>
            <option value="">Elegir...</option>
            {ejercicios.map((e) => (
              <option key={e.id} value={e.id}>
                {e.nombre}
              </option>
            ))}
          </select>
        </label>
        <label className="flex flex-col gap-1 text-xs text-neutral-400">
          1RM (kg)
          <input type="number" min="0" step="0.5" className={claseInput} value={valor} onChange={(e) => setValor(e.target.value)} />
        </label>
        <label className="flex flex-col gap-1 text-xs text-neutral-400">
          Fecha (opcional)
          <input type="date" className={claseInput} value={fecha} onChange={(e) => setFecha(e.target.value)} />
        </label>
        <button
          type="submit"
          disabled={guardando}
          className="rounded-lg bg-emerald-500 px-3 py-2 text-sm font-medium text-neutral-950 transition hover:bg-emerald-400 disabled:opacity-60"
        >
          {guardando ? 'Guardando...' : 'Registrar'}
        </button>
      </form>
      {errorForm && <p className="mt-2 text-sm text-red-400">{errorForm}</p>}

      {registros.length > 0 && (
        <details className="mt-4">
          <summary className="cursor-pointer text-sm text-emerald-400">Ver historial ({registros.length})</summary>
          <ul className="mt-2 divide-y divide-neutral-800 text-sm">
            {registros.map((r) => (
              <li key={r.id} className="flex justify-between py-1.5">
                <span className="text-neutral-400">
                  {r.fechaMedicion} · {r.nombreEjercicio}
                </span>
                <span>{r.valor1rm} kg</span>
              </li>
            ))}
          </ul>
        </details>
      )}
    </section>
  )
}