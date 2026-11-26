import { useEffect, useState } from 'react'
import { alumnosService } from '../../services/alumnos'
import { profesoresService } from '../../services/profesores'
import { planesService } from '../../services/planes'
import { mensajeDeError } from '../../utils/errores'
import { formatearPrecio } from '../../utils/formato'

const claseInput =
  'w-full rounded-lg border border-negro/20 bg-fondo px-3 py-2 text-sm outline-none focus:border-celeste'

const claseBotonPestana = (activa) =>
  `rounded-lg px-4 py-2 text-sm font-medium transition ${
    activa ? 'bg-celeste text-negro' : 'border border-negro/20 text-negro hover:bg-negro/10'
  }`

// Panel desplegable para cobrarle un plan en efectivo a un alumno (PG3).
function FormularioPagoEfectivo({ alumno, planes, onPagado, onCancelar }) {
  const [planId, setPlanId] = useState(planes[0]?.id ? String(planes[0].id) : '')
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState('')

  const confirmar = async (evento) => {
    evento.preventDefault()
    setError('')
    const plan = planes.find((p) => p.id === Number(planId))
    if (!plan) {
      setError('Elegí un plan válido.')
      return
    }
    setGuardando(true)
    try {
      const pago = await planesService.cobrarEnEfectivo(plan, alumno.id)
      onPagado(plan, pago)
    } catch (err) {
      setError(mensajeDeError(err))
      setGuardando(false)
    }
  }

  return (
    <form onSubmit={confirmar} className="mt-4 grid gap-3 border-t border-negro/10 pt-4 sm:grid-cols-3">
      <label className="flex flex-col gap-1 text-xs text-negro/70 sm:col-span-2">
        Plan a cobrar en efectivo
        <select className={claseInput} value={planId} onChange={(e) => setPlanId(e.target.value)}>
          {planes.map((p) => (
            <option key={p.id} value={p.id}>
              {p.nombre} — {formatearPrecio(p.precio)} ({p.duracionDias} días)
            </option>
          ))}
        </select>
      </label>
      <div className="flex items-end gap-2">
        <button
          type="submit"
          disabled={guardando || planes.length === 0}
          className="rounded-lg bg-celeste px-4 py-2 text-sm font-medium text-negro transition hover:bg-celeste/80 disabled:opacity-60"
        >
          {guardando ? 'Registrando...' : 'Confirmar cobro'}
        </button>
        <button
          type="button"
          onClick={onCancelar}
          className="rounded-lg border border-negro/20 px-3 py-2 text-sm text-negro hover:bg-negro/10"
        >
          Cancelar
        </button>
      </div>
      {error && <p className="text-sm text-red-700 sm:col-span-3">{error}</p>}
    </form>
  )
}

// Formulario para dar de alta un nuevo profesor a partir de su ID de usuario (P3).
function FormularioNuevoProfesor({ onCreado, onCancelar }) {
  const [usuarioId, setUsuarioId] = useState('')
  const [especialidad, setEspecialidad] = useState('')
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState('')

  const guardar = async (evento) => {
    evento.preventDefault()
    setError('')
    if (!usuarioId || Number(usuarioId) < 1) {
      setError('Ingresá un ID de usuario válido.')
      return
    }
    if (!especialidad.trim()) {
      setError('La especialidad es obligatoria.')
      return
    }
    setGuardando(true)
    try {
      const creado = await profesoresService.registrar({
        usuarioId: Number(usuarioId),
        especialidad: especialidad.trim(),
      })
      onCreado(creado)
    } catch (err) {
      setError(mensajeDeError(err))
      setGuardando(false)
    }
  }

  return (
    <form
      onSubmit={guardar}
      className="mb-6 grid gap-3 rounded-2xl border border-celeste/40 bg-gris p-5 sm:grid-cols-3"
    >
      <h3 className="font-semibold sm:col-span-3">Dar de alta nuevo profesor</h3>
      <label className="flex flex-col gap-1 text-xs text-negro/70">
        ID del usuario sincronizado
        <input
          type="number"
          min="1"
          placeholder="Ej: 68"
          className={claseInput}
          value={usuarioId}
          onChange={(e) => setUsuarioId(e.target.value)}
        />
      </label>
      <label className="flex flex-col gap-1 text-xs text-negro/70">
        Especialidad
        <input
          placeholder="Musculación, CrossFit, Yoga..."
          className={claseInput}
          value={especialidad}
          onChange={(e) => setEspecialidad(e.target.value)}
        />
      </label>
      <div className="flex items-end gap-2">
        <button
          type="submit"
          disabled={guardando}
          className="rounded-lg bg-celeste px-4 py-2 text-sm font-medium text-negro transition hover:bg-celeste/80 disabled:opacity-60"
        >
          {guardando ? 'Guardando...' : 'Registrar'}
        </button>
        <button
          type="button"
          onClick={onCancelar}
          className="rounded-lg border border-negro/20 px-4 py-2 text-sm text-negro hover:bg-negro/10"
        >
          Cancelar
        </button>
      </div>
      {error && <p className="text-sm text-red-700 sm:col-span-3">{error}</p>}
    </form>
  )
}

export default function Usuarios() {
  const [pestana, setPestana] = useState('alumnos') // 'alumnos' | 'profesores'
  const [alumnos, setAlumnos] = useState([])
  const [profesores, setProfesores] = useState([])
  const [planes, setPlanes] = useState([])
  const [cargando, setCargando] = useState(true)
  const [errorCarga, setErrorCarga] = useState('')
  const [busqueda, setBusqueda] = useState('')
  const [accionEnCurso, setAccionEnCurso] = useState(null)
  const [alumnoCobrando, setAlumnoCobrando] = useState(null)
  const [creandoProfesor, setCreandoProfesor] = useState(false)
  const [mensaje, setMensaje] = useState(null) // { tipo: 'exito' | 'error', texto }

  useEffect(() => {
    let cancelado = false
    Promise.all([alumnosService.listar(), profesoresService.listar(), planesService.listarPlanes()])
      .then(([listaAlumnos, listaProfesores, listaPlanes]) => {
        if (cancelado) return
        setAlumnos(listaAlumnos)
        setProfesores(listaProfesores)
        setPlanes(listaPlanes)
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
  }, [])

  const cambiarAptoFisico = async (alumno) => {
    const clave = `apto-${alumno.id}`
    setMensaje(null)
    setAccionEnCurso(clave)
    try {
      const actualizado = await alumnosService.actualizarAptoFisico(alumno.id, !alumno.aptoFisico)
      setAlumnos((anteriores) => anteriores.map((a) => (a.id === alumno.id ? actualizado : a)))
      setMensaje({
        tipo: 'exito',
        texto: `Apto físico de ${alumno.nombre} ${alumno.apellido}: ${
          actualizado.aptoFisico ? 'Vigente' : 'Pendiente'
        }.`,
      })
    } catch (err) {
      setMensaje({ tipo: 'error', texto: mensajeDeError(err) })
    } finally {
      setAccionEnCurso(null)
    }
  }

  const cambiarProfesor = async (alumno, nuevoProfesorId) => {
    if (!nuevoProfesorId) return
    const clave = `prof-${alumno.id}`
    setMensaje(null)
    setAccionEnCurso(clave)
    try {
      const profObj = profesores.find((p) => p.id === Number(nuevoProfesorId)) ?? null
      const actualizado = await alumnosService.asignarProfesor(
        alumno.id,
        Number(nuevoProfesorId),
        profObj,
      )
      setAlumnos((anteriores) => anteriores.map((a) => (a.id === alumno.id ? actualizado : a)))
      setMensaje({
        tipo: 'exito',
        texto: `Profesor asignado a ${alumno.nombre} ${alumno.apellido}.`,
      })
    } catch (err) {
      setMensaje({ tipo: 'error', texto: mensajeDeError(err) })
    } finally {
      setAccionEnCurso(null)
    }
  }

  const alCobrarEnEfectivo = (alumno, plan, pago) => {
    setAlumnoCobrando(null)
    setMensaje({
      tipo: 'exito',
      texto: `Pago en efectivo aprobado para ${alumno.nombre} ${alumno.apellido} (${plan.nombre} · Comprobante: ${
        pago.referenciaExterna ?? `#${pago.id}`
      }).`,
    })
  }

  const alCrearProfesor = (nuevo) => {
    setProfesores((anteriores) => [...anteriores, nuevo])
    setCreandoProfesor(false)
    setMensaje({
      tipo: 'exito',
      texto: `Profesor ${nuevo.nombre} ${nuevo.apellido} (${nuevo.especialidad}) dado de alta con ID #${nuevo.id}.`,
    })
  }

  if (cargando) return <p className="text-negro/70">Cargando usuarios...</p>
  if (errorCarga) return <p className="text-red-700">{errorCarga}</p>

  const texto = busqueda.trim().toLowerCase()
  const alumnosVisibles = alumnos.filter((a) =>
    `${a.nombre} ${a.apellido} ${a.email}`.toLowerCase().includes(texto),
  )

  return (
    <div className="max-w-5xl">
      <div className="mb-5 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 className="text-2xl font-bold">Usuarios</h2>
          <p className="mt-1 text-sm text-negro/70">
            Gestioná el apto físico, profesor asignado y cobros en efectivo de los alumnos, o da de alta profesores.
          </p>
        </div>
        {pestana === 'profesores' && !creandoProfesor && (
          <button
            onClick={() => {
              setMensaje(null)
              setCreandoProfesor(true)
            }}
            className="rounded-lg bg-celeste px-4 py-2 text-sm font-medium text-negro transition hover:bg-celeste/80"
          >
            + Nuevo profesor
          </button>
        )}
      </div>

      <div className="mb-6 flex gap-2">
        <button onClick={() => setPestana('alumnos')} className={claseBotonPestana(pestana === 'alumnos')}>
          Alumnos ({alumnos.length})
        </button>
        <button onClick={() => setPestana('profesores')} className={claseBotonPestana(pestana === 'profesores')}>
          Profesores ({profesores.length})
        </button>
      </div>

      {mensaje && (
        <p
          className={`mb-4 rounded-lg px-4 py-2.5 text-sm ${
            mensaje.tipo === 'exito' ? 'bg-celeste/15 text-azul' : 'bg-red-500/15 text-red-700'
          }`}
        >
          {mensaje.texto}
        </p>
      )}

      {pestana === 'alumnos' ? (
        <>
          <input
            type="search"
            placeholder="Buscar alumno por nombre o email..."
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
            className="mb-4 w-full rounded-lg border border-negro/20 bg-fondo px-3 py-2 text-sm outline-none focus:border-celeste"
          />

          {alumnosVisibles.length === 0 ? (
            <p className="text-sm text-negro/65">No se encontraron alumnos.</p>
          ) : (
            <ul className="flex flex-col gap-3">
              {alumnosVisibles.map((a) => (
                <li key={a.id} className="rounded-2xl border border-negro/10 bg-gris p-5">
                  <div className="flex flex-wrap items-start justify-between gap-4">
                    <div>
                      <div className="flex items-center gap-2.5">
                        <p className="font-semibold">
                          {a.nombre} {a.apellido}
                        </p>
                        <span className="rounded bg-negro/10 px-2 py-0.5 text-xs text-negro/70">
                          ID #{a.id}
                        </span>
                        <span
                          className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${
                            a.aptoFisico
                              ? 'bg-celeste/15 text-azul'
                              : 'bg-amber-500/15 text-amber-700'
                          }`}
                        >
                          {a.aptoFisico ? 'Apto físico vigente' : 'Sin apto físico'}
                        </span>
                      </div>
                      <p className="mt-1 text-sm text-negro/70">
                        {a.email} · Ingreso: {a.fechaIngreso}
                      </p>
                    </div>

                    <div className="flex flex-wrap items-center gap-2">
                      <button
                        type="button"
                        onClick={() => cambiarAptoFisico(a)}
                        disabled={accionEnCurso !== null}
                        className="rounded-lg border border-negro/20 px-3 py-1.5 text-xs font-medium text-negro transition hover:bg-negro/10 disabled:opacity-50"
                      >
                        {accionEnCurso === `apto-${a.id}`
                          ? 'Guardando...'
                          : a.aptoFisico
                            ? 'Marcar sin apto'
                            : 'Aprobar apto físico'}
                      </button>

                      <button
                        type="button"
                        onClick={() => setAlumnoCobrando(alumnoCobrando === a.id ? null : a.id)}
                        className="rounded-lg border border-celeste/60 px-3 py-1.5 text-xs font-medium text-azul transition hover:bg-celeste/10"
                      >
                        Cobrar plan (Efectivo)
                      </button>
                    </div>
                  </div>

                  <div className="mt-4 flex flex-wrap items-center gap-3 border-t border-negro/10 pt-3 text-sm">
                    <span className="text-xs uppercase tracking-wide text-negro/65">Profesor a cargo:</span>
                    <select
                      value={a.profesor?.id ?? ''}
                      disabled={accionEnCurso === `prof-${a.id}`}
                      onChange={(e) => cambiarProfesor(a, e.target.value)}
                      className="rounded-lg border border-negro/20 bg-fondo px-3 py-1.5 text-sm outline-none focus:border-celeste"
                    >
                      <option value="">Sin profesor asignado</option>
                      {profesores.map((p) => (
                        <option key={p.id} value={p.id}>
                          {p.nombre} {p.apellido} ({p.especialidad})
                        </option>
                      ))}
                    </select>
                  </div>

                  {alumnoCobrando === a.id && (
                    <FormularioPagoEfectivo
                      alumno={a}
                      planes={planes}
                      onPagado={(plan, pago) => alCobrarEnEfectivo(a, plan, pago)}
                      onCancelar={() => setAlumnoCobrando(null)}
                    />
                  )}
                </li>
              ))}
            </ul>
          )}
        </>
      ) : (
        <>
          {creandoProfesor && (
            <FormularioNuevoProfesor
              onCreado={alCrearProfesor}
              onCancelar={() => setCreandoProfesor(false)}
            />
          )}

          {profesores.length === 0 ? (
            <p className="text-sm text-negro/65">Todavía no hay profesores registrados.</p>
          ) : (
            <ul className="flex flex-col gap-3">
              {profesores.map((p) => (
                <li
                  key={p.id}
                  className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-negro/10 bg-gris p-4"
                >
                  <div>
                    <div className="flex items-center gap-2">
                      <p className="font-semibold">
                        {p.nombre} {p.apellido}
                      </p>
                      <span className="rounded bg-negro/10 px-2 py-0.5 text-xs text-negro/70">
                        ID #{p.id}
                      </span>
                    </div>
                    <p className="mt-0.5 text-sm text-negro/70">{p.email}</p>
                  </div>
                  <span className="rounded-full bg-celeste/15 px-3 py-1 text-xs font-medium text-azul">
                    {p.especialidad}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </>
      )}
    </div>
  )
}
