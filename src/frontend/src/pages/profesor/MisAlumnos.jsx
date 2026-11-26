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

  if (cargando) return <p className="text-negro/70">Cargando alumnos...</p>
  if (error) return <p className="text-red-700">{error}</p>

  // El filtro se hace en el navegador: el backend devuelve la lista completa.
  const texto = busqueda.trim().toLowerCase()
  const visibles = alumnos.filter((a) =>
    `${a.nombre} ${a.apellido} ${a.email}`.toLowerCase().includes(texto),
  )

  return (
    <div className="max-w-4xl">
      <h2 className="text-2xl font-bold">Mis alumnos</h2>
      <p className="mb-6 mt-1 text-sm text-negro/70">
        {alumnos.length} {alumnos.length === 1 ? 'alumno asignado' : 'alumnos asignados'}
      </p>

      <input
        type="search"
        placeholder="Buscar por nombre o email"
        value={busqueda}
        onChange={(e) => setBusqueda(e.target.value)}
        className="mb-4 w-full rounded-lg border border-negro/20 bg-fondo px-3 py-2 text-sm outline-none focus:border-celeste"
      />

      {alumnos.length === 0 ? (
        <p className="text-negro/70">Todavía no tenés alumnos asignados.</p>
      ) : visibles.length === 0 ? (
        <p className="text-negro/70">Ningún alumno coincide con la búsqueda.</p>
      ) : (
        <ul className="flex flex-col gap-3">
          {visibles.map((a) => (
            <li key={a.id}>
              <Link
                to={`/profesor/alumnos/${a.id}`}
                className="flex items-center justify-between gap-4 rounded-2xl border border-negro/10 bg-gris p-4 transition hover:border-celeste/60"
              >
                <div>
                  <p className="font-semibold">
                    {a.nombre} {a.apellido}
                  </p>
                  <p className="text-sm text-negro/70">
                    {a.email} · Ingresó el {a.fechaIngreso}
                  </p>
                </div>
                <span
                  className={`shrink-0 rounded-full px-3 py-1 text-xs font-medium ${
                    a.aptoFisico
                      ? 'bg-celeste/15 text-azul'
                      : 'bg-amber-500/15 text-amber-700'
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