import { useEffect, useState } from 'react'
import { useAuth } from '../../hooks/useAuth'
import Bienvenida from '../../components/Bienvenida'
import TarjetaResumen from '../../components/TarjetaResumen'
import { profesoresService } from '../../services/profesores'
import { rutinasService } from '../../services/rutinas'
import { clasesService } from '../../services/clases'
import { formatearFechaHora } from '../../utils/formato'

export default function InicioProfesor() {
  const { usuario } = useAuth()
  // null mientras carga. Después queda [alumnos, rutinas, clases]:
  // si alguna consulta falló, su lugar queda en null y solo esa tarjeta muestra "—".
  const [datos, setDatos] = useState(null)
  const [ahora] = useState(() => Date.now())

  useEffect(() => {
    let cancelado = false
    Promise.allSettled([
      profesoresService.listarAlumnos(usuario.profesorId),
      rutinasService.listarPorProfesor(usuario.profesorId),
      clasesService.listarDelProfesor(usuario.profesorId),
    ]).then((resultados) => {
      if (cancelado) return
      setDatos(resultados.map((r) => (r.status === 'fulfilled' ? r.value : null)))
    })
    return () => {
      cancelado = true
    }
  }, [usuario.profesorId])

  if (datos === null) return <p className="text-neutral-400">Cargando tu resumen...</p>
  const [alumnos, rutinas, clases] = datos

  const sinApto = alumnos?.filter((a) => !a.aptoFisico).length ?? 0
  // Las plantillas no tienen alumno; las rutinas asignadas sí.
  const plantillas = rutinas?.filter((r) => r.alumnoId == null).length ?? 0
  const asignadas = (rutinas?.length ?? 0) - plantillas
  const proximaClase = clases
    ?.filter((c) => new Date(c.horario).getTime() > ahora)
    .sort((a, b) => a.horario.localeCompare(b.horario))[0]

  return (
    <Bienvenida descripcion="Este es el resumen de tus alumnos, rutinas y clases.">
      <div className="grid gap-4 sm:grid-cols-2">
        <TarjetaResumen
          titulo="Mis alumnos"
          to="/profesor/alumnos"
          valor={alumnos ? String(alumnos.length) : null}
          detalle={
            alumnos
              ? sinApto > 0
                ? `${sinApto} sin apto físico registrado`
                : 'Todos con apto físico'
              : 'No se pudo cargar'
          }
          aviso={!alumnos || sinApto > 0}
        />
        <TarjetaResumen
          titulo="Rutinas"
          to="/profesor/rutinas"
          valor={rutinas ? `${plantillas} ${plantillas === 1 ? 'plantilla' : 'plantillas'}` : null}
          detalle={
            rutinas
              ? `${asignadas} ${asignadas === 1 ? 'asignada' : 'asignadas'} a alumnos`
              : 'No se pudo cargar'
          }
        />
        <div className="sm:col-span-2">
          <TarjetaResumen
            titulo="Próxima clase"
            to="/profesor/clases"
            valor={clases ? (proximaClase ? proximaClase.tipoActividad : 'Sin clases próximas') : null}
            detalle={
              clases
                ? proximaClase
                  ? `${formatearFechaHora(proximaClase.horario)} · ${
                      proximaClase.cupoMaximo - proximaClase.cupoDisponible
                    } de ${proximaClase.cupoMaximo} lugares ocupados`
                  : 'No tenés clases asignadas a futuro'
                : 'No se pudo cargar'
            }
          />
        </div>
      </div>
    </Bienvenida>
  )
}