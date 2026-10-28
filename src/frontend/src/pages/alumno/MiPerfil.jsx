import { useEffect, useState } from 'react'
import { useAuth } from '../../hooks/useAuth'
import { alumnosService } from '../../services/alumnos'
import { mensajeDeError } from '../../utils/errores'

const claseInput =
  'rounded-lg border border-neutral-700 bg-neutral-950 px-3 py-2 outline-none focus:border-emerald-500'

// Dato que se muestra pero no se puede editar.
function DatoFijo({ titulo, children }) {
  return (
    <div>
      <p className="text-xs uppercase tracking-wide text-neutral-500">{titulo}</p>
      <p className="mt-1 text-sm">{children}</p>
    </div>
  )
}

function FormularioPerfil({ perfil, alumnoId, alGuardar }) {
  const [nombre, setNombre] = useState(perfil.nombre)
  const [apellido, setApellido] = useState(perfil.apellido)
  const [telefono, setTelefono] = useState(perfil.telefono ?? '')
  const [fechaNacimiento, setFechaNacimiento] = useState(perfil.fechaNacimiento ?? '')
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState('')
  const [exito, setExito] = useState(false)

  const guardar = async (evento) => {
    evento.preventDefault()
    setError('')
    setExito(false)
    setGuardando(true)
    try {
      const actualizado = await alumnosService.actualizarPerfil(alumnoId, {
        nombre,
        apellido,
        telefono,
        fechaNacimiento: fechaNacimiento || null,
      })
      alGuardar(actualizado)
      setExito(true)
    } catch (err) {
      setError(mensajeDeError(err))
    } finally {
      setGuardando(false)
    }
  }

  return (
    <form onSubmit={guardar} className="grid gap-4 sm:grid-cols-2">
      <label className="flex flex-col gap-1 text-sm">
        Nombre
        <input className={claseInput} value={nombre} onChange={(e) => setNombre(e.target.value)} />
      </label>
      <label className="flex flex-col gap-1 text-sm">
        Apellido
        <input className={claseInput} value={apellido} onChange={(e) => setApellido(e.target.value)} />
      </label>
      <label className="flex flex-col gap-1 text-sm">
        Teléfono
        <input
          type="tel"
          className={claseInput}
          value={telefono}
          onChange={(e) => setTelefono(e.target.value)}
        />
      </label>
      <label className="flex flex-col gap-1 text-sm">
        Fecha de nacimiento
        <input
          type="date"
          className={claseInput}
          value={fechaNacimiento}
          onChange={(e) => setFechaNacimiento(e.target.value)}
        />
      </label>

      <div className="flex items-center gap-4 sm:col-span-2">
        <button
          type="submit"
          disabled={guardando}
          className="rounded-lg bg-emerald-500 px-4 py-2 text-sm font-medium text-neutral-950 transition hover:bg-emerald-400 disabled:opacity-60"
        >
          {guardando ? 'Guardando...' : 'Guardar cambios'}
        </button>
        {exito && <p className="text-sm text-emerald-400">Perfil actualizado correctamente.</p>}
        {error && <p className="text-sm text-red-400">{error}</p>}
      </div>
    </form>
  )
}

export default function MiPerfil() {
  const { usuario } = useAuth()
  const [perfil, setPerfil] = useState(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelado = false
    alumnosService
      .obtenerPerfil(usuario.alumnoId)
      .then((datos) => {
        if (!cancelado) setPerfil(datos)
      })
      .catch((err) => {
        if (!cancelado) setError(mensajeDeError(err))
      })
      .finally(() => {
        if (!cancelado) setCargando(false)
      })
    // Si el usuario sale de la pantalla antes de que responda, se ignora la respuesta.
    return () => {
      cancelado = true
    }
  }, [usuario.alumnoId])

  if (cargando) return <p className="text-neutral-400">Cargando perfil...</p>
  if (error) return <p className="text-red-400">{error}</p>

  return (
    <div className="max-w-3xl">
      <h2 className="text-2xl font-bold">Mi perfil</h2>
      <p className="mb-6 mt-1 text-sm text-neutral-400">
        Mantené tus datos personales actualizados.
      </p>

      <section className="mb-6 rounded-2xl border border-neutral-800 bg-neutral-900 p-6">
        <h3 className="mb-4 font-semibold">Datos personales</h3>
        <FormularioPerfil perfil={perfil} alumnoId={usuario.alumnoId} alGuardar={setPerfil} />
      </section>

      <section className="rounded-2xl border border-neutral-800 bg-neutral-900 p-6">
        <h3 className="mb-1 font-semibold">Información del gimnasio</h3>
        <p className="mb-4 text-xs text-neutral-500">
          Estos datos los gestiona la administración y no se pueden editar.
        </p>
        <div className="grid gap-4 sm:grid-cols-2">
          <DatoFijo titulo="Email">{perfil.email}</DatoFijo>
          <DatoFijo titulo="Fecha de ingreso">{perfil.fechaIngreso}</DatoFijo>
          <DatoFijo titulo="Apto físico">
            {perfil.aptoFisico ? (
              <span className="rounded-full bg-emerald-500/15 px-2 py-0.5 text-emerald-400">
                Vigente
              </span>
            ) : (
              <span className="rounded-full bg-red-500/15 px-2 py-0.5 text-red-400">
                Pendiente
              </span>
            )}
          </DatoFijo>
          <DatoFijo titulo="Profesor asignado">
            {perfil.profesor
              ? `${perfil.profesor.nombre} ${perfil.profesor.apellido} · ${perfil.profesor.especialidad}`
              : 'Todavía no tenés un profesor asignado'}
          </DatoFijo>
        </div>
      </section>
    </div>
  )
}