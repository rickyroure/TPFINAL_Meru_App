import { useEffect, useState } from 'react'
import { planesService } from '../../services/planes'
import { mensajeDeError } from '../../utils/errores'

const claseInput =
  'w-full rounded-lg border border-neutral-700 bg-neutral-950 px-3 py-2 text-sm outline-none focus:border-emerald-500'

const formatoPrecio = (precio) =>
  new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', maximumFractionDigits: 0 }).format(precio)

// Formulario para crear un plan o editar uno existente (si recibe "plan").
function FormularioPlan({ plan, onGuardado, onCancelar }) {
  const [nombre, setNombre] = useState(plan?.nombre ?? '')
  const [precio, setPrecio] = useState(plan ? String(plan.precio) : '')
  const [duracion, setDuracion] = useState(plan ? String(plan.duracionDias) : '')
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState('')

  const guardar = async (evento) => {
    evento.preventDefault()
    setError('')
    const datos = {
      nombre: nombre.trim(),
      precio: precio === '' ? null : Number(precio),
      duracionDias: duracion === '' ? null : Number(duracion),
    }
    setGuardando(true)
    try {
      const resultado = plan
        ? await planesService.actualizarPlan(plan.id, datos)
        : await planesService.crearPlan(datos)
      onGuardado(resultado, Boolean(plan))
    } catch (err) {
      setError(mensajeDeError(err))
      setGuardando(false)
    }
  }

  return (
    <form onSubmit={guardar} className="grid gap-3 rounded-2xl border border-emerald-500/40 bg-neutral-900 p-5 sm:grid-cols-4">
      <h3 className="font-semibold sm:col-span-4">{plan ? `Editar plan "${plan.nombre}"` : 'Nuevo plan'}</h3>
      <label className="flex flex-col gap-1 text-xs text-neutral-400">
        Nombre
        <input className={claseInput} value={nombre} onChange={(e) => setNombre(e.target.value)} />
      </label>
      <label className="flex flex-col gap-1 text-xs text-neutral-400">
        Precio (ARS)
        <input type="number" min="0" className={claseInput} value={precio} onChange={(e) => setPrecio(e.target.value)} />
      </label>
      <label className="flex flex-col gap-1 text-xs text-neutral-400">
        Duración (días)
        <input type="number" min="1" className={claseInput} value={duracion} onChange={(e) => setDuracion(e.target.value)} />
      </label>
      <div className="flex items-end gap-2">
        <button
          type="submit"
          disabled={guardando}
          className="rounded-lg bg-emerald-500 px-4 py-2 text-sm font-medium text-neutral-950 transition hover:bg-emerald-400 disabled:opacity-60"
        >
          {guardando ? 'Guardando...' : 'Guardar'}
        </button>
        <button
          type="button"
          onClick={onCancelar}
          className="rounded-lg border border-neutral-700 px-4 py-2 text-sm text-neutral-300 hover:bg-neutral-800"
        >
          Cancelar
        </button>
      </div>
      {error && <p className="text-sm text-red-400 sm:col-span-4">{error}</p>}
    </form>
  )
}

export default function PlanesYTarifas() {
  const [planes, setPlanes] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [aviso, setAviso] = useState('')
  // null = formulario cerrado, 'nuevo' = alta, o el plan que se está editando.
  const [formulario, setFormulario] = useState(null)

  useEffect(() => {
    let cancelado = false
    planesService
      .listarPlanes()
      .then((datos) => {
        if (!cancelado) setPlanes(datos)
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

  const alGuardar = (plan, eraEdicion) => {
    setPlanes((anteriores) =>
      eraEdicion ? anteriores.map((p) => (p.id === plan.id ? plan : p)) : [...anteriores, plan],
    )
    setFormulario(null)
    setAviso(eraEdicion ? `Plan "${plan.nombre}" actualizado.` : `Plan "${plan.nombre}" creado.`)
  }

  if (cargando) return <p className="text-neutral-400">Cargando planes...</p>
  if (error) return <p className="text-red-400">{error}</p>

  return (
    <div className="max-w-4xl">
      <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 className="text-2xl font-bold">Planes y tarifas</h2>
          <p className="mt-1 text-sm text-neutral-400">Los cambios se ven al instante en la pantalla "Mi plan" de los alumnos.</p>
        </div>
        {formulario === null && (
          <button
            onClick={() => {
              setAviso('')
              setFormulario('nuevo')
            }}
            className="rounded-lg bg-emerald-500 px-4 py-2 text-sm font-medium text-neutral-950 transition hover:bg-emerald-400"
          >
            + Nuevo plan
          </button>
        )}
      </div>

      {aviso && <p className="mb-4 rounded-lg bg-emerald-500/10 px-4 py-2 text-sm text-emerald-400">{aviso}</p>}

      {formulario !== null && (
        <div className="mb-6">
          <FormularioPlan
            key={formulario === 'nuevo' ? 'nuevo' : formulario.id}
            plan={formulario === 'nuevo' ? null : formulario}
            onGuardado={alGuardar}
            onCancelar={() => setFormulario(null)}
          />
        </div>
      )}

      {planes.length === 0 ? (
        <p className="text-neutral-400">Todavía no hay planes creados.</p>
      ) : (
        <ul className="flex flex-col gap-3">
          {planes.map((p) => (
            <li
              key={p.id}
              className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-neutral-800 bg-neutral-900 p-4"
            >
              <div>
                <p className="font-semibold">{p.nombre}</p>
                <p className="text-sm text-neutral-400">
                  {p.duracionDias} {p.duracionDias === 1 ? 'día' : 'días'}
                </p>
              </div>
              <div className="flex items-center gap-4">
                <span className="text-lg font-semibold text-emerald-400">{formatoPrecio(p.precio)}</span>
                <button
                  onClick={() => {
                    setAviso('')
                    setFormulario(p)
                  }}
                  className="rounded-lg border border-neutral-700 px-3 py-1.5 text-sm text-neutral-300 transition hover:bg-neutral-800"
                >
                  Editar
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}