import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import { profesoresService } from '../../services/profesores'
import { mensajeDeError } from '../../utils/errores'

export default function MisAlumnos() {
  const { usuario } = useAuth()
  const [alumnos, setAlumnos] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [busqueda, setBusqueda] = useState('')

  useEffect(() => {
    let cancelado = false
    profesoresService
      .listarAlumnos(usuario.profesorId)
      .then((datos) => {
        if (!cancelado) setAlumnos(datos)
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

  if (cargando) return <p className="text-neutral-400">Cargando alumnos...</p>
  if (error) return <p className="text-red-400">{error}</p>

  // El filtro se hace en el navegador: el backend devuelve la lista completa.
  const texto = busqueda.trim().toLowerCase()
  const visibles = alumnos.filter((a) =>
    `${a.nombre} ${a.apellido} ${a.email}`.toLowerCase().includes(texto),
  )

  return (
    <div className="max-w-4xl">
      <h2 className="text-2xl font-bold">Mis alumnos</h2>
      <p className="mb-6 mt-1 text-sm text-neutral-400">
        {alumnos.length} {alumnos.length === 1 ? 'alumno asignado' : 'alumnos asignados'}
      </p>

      <input
        type="search"
        placeholder="Buscar por nombre o email"
        value={busqueda}
        onChange={(e) => setBusqueda(e.target.value)}
        className="mb-4 w-full rounded-lg border border-neutral-700 bg-neutral-950 px-3 py-2 text-sm outline-none focus:border-emerald-500"
      />

      {alumnos.length === 0 ? (
        <p className="text-neutral-400">Todavía no tenés alumnos asignados.</p>
      ) : visibles.length === 0 ? (
        <p className="text-neutral-400">Ningún alumno coincide con la búsqueda.</p>
      ) : (
        <ul className="flex flex-col gap-3">
          {visibles.map((a) => (
            <li key={a.id}>
              <Link
                to={`/profesor/alumnos/${a.id}`}
                className="flex items-center justify-between gap-4 rounded-2xl border border-neutral-800 bg-neutral-900 p-4 transition hover:border-emerald-500/60"
              >
                <div>
                  <p className="font-semibold">
                    {a.nombre} {a.apellido}
                  </p>
                  <p className="text-sm text-neutral-400">
                    {a.email} · Ingresó el {a.fechaIngreso}
                  </p>
                </div>
                <span
                  className={`shrink-0 rounded-full px-3 py-1 text-xs font-medium ${
                    a.aptoFisico
                      ? 'bg-emerald-500/15 text-emerald-400'
                      : 'bg-amber-500/15 text-amber-400'
                  }`}
                >
                  {a.aptoFisico ? 'Apto físico' : 'Sin apto físico'}
                </span>
              </Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}