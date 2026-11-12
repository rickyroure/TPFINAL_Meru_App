import { useEffect, useState } from 'react'
import { useAuth } from '../../hooks/useAuth'
import Bienvenida from '../../components/Bienvenida'
import TarjetaResumen from '../../components/TarjetaResumen'
import { alumnosService } from '../../services/alumnos'
import { planesService } from '../../services/planes'
import { clasesService } from '../../services/clases'
import { rutinasService } from '../../services/rutinas'
import { formatearFecha, formatearFechaHora } from '../../utils/formato'

const DIAS_AVISO_VENCIMIENTO = 3

export default function InicioAlumno() {
  const { usuario } = useAuth()
  // null mientras carga. Después queda [perfil, suscripciones, reservas, rutinas]:
  // si alguna consulta falló, su lugar queda en null y solo esa tarjeta muestra "—".
  const [datos, setDatos] = useState(null)
  const [ahora] = useState(() => Date.now())

  useEffect(() => {
    let cancelado = false
    Promise.allSettled([
      alumnosService.obtenerPerfil(usuario.alumnoId),
      planesService.listarSuscripcionesDelAlumno(usuario.alumnoId),
      clasesService.listarReservasDelAlumno(usuario.alumnoId),
      rutinasService.listarPorAlumno(usuario.alumnoId),
    ]).then((resultados) => {
      if (cancelado) return
      setDatos(resultados.map((r) => (r.status === 'fulfilled' ? r.value : null)))
    })
    return () => {
      cancelado = true
    }
  }, [usuario.alumnoId])

  if (datos === null) return <p className="text-neutral-400">Cargando tu resumen...</p>
  const [perfil, suscripciones, reservas, rutinas] = datos

  // Plan vigente: la suscripción activa que vence más tarde.
  const vigente = suscripciones
    ?.filter((s) => s.estado === 'ACTIVA' && s.diasRestantes > 0)
    .sort((a, b) => b.fechaFin.localeCompare(a.fechaFin))[0]

  // Próxima clase: la reserva confirmada más cercana que todavía no empezó.
  const proximaClase = reservas
    ?.filter((r) => r.estado === 'CONFIRMADA' && new Date(r.horarioClase).getTime() > ahora)
    .sort((a, b) => a.horarioClase.localeCompare(b.horarioClase))[0]

  // La rutina actual es la última que le asignaron.
  const rutina = rutinas?.[rutinas.length - 1]

  return (
    <Bienvenida descripcion="Este es el resumen de tu actividad en el gimnasio.">
      {perfil && !perfil.aptoFisico && (
        <p className="mb-4 rounded-lg bg-amber-500/15 px-4 py-3 text-sm text-amber-400">
          Todavía no tenés el apto físico registrado. Acercalo al gimnasio para que lo carguen.
        </p>
      )}

      <div className="grid gap-4 sm:grid-cols-2">
        <TarjetaResumen
          titulo="Mi plan"
          to="/alumno/plan"
          valor={suscripciones ? (vigente ? vigente.nombrePlan : 'Sin plan vigente') : null}
          detalle={
            suscripciones
              ? vigente
                ? `Vence el ${formatearFecha(vigente.fechaFin)} · ${vigente.diasRestantes} ${
                    vigente.diasRestantes === 1 ? 'día' : 'días'
                  }`
                : 'Contratá uno para empezar a entrenar'
              : 'No se pudo cargar'
          }
          aviso={!suscripciones || !vigente || vigente.diasRestantes <= DIAS_AVISO_VENCIMIENTO}
        />
        <TarjetaResumen
          titulo="Próxima clase"
          to="/alumno/clases"
          valor={reservas ? (proximaClase ? proximaClase.tipoActividadClase : 'Sin reservas') : null}
          detalle={
            reservas
              ? proximaClase
                ? formatearFechaHora(proximaClase.horarioClase)
                : 'Reservá tu lugar en una clase'
              : 'No se pudo cargar'
          }
        />
        <TarjetaResumen
          titulo="Mi rutina"
          to="/alumno/rutina"
          valor={rutinas ? (rutina ? rutina.nombre : 'Sin rutina asignada') : null}
          detalle={
            rutinas
              ? rutina
                ? `${rutina.duracionSemanas} ${rutina.duracionSemanas === 1 ? 'semana' : 'semanas'} · profesor ${
                    rutina.nombreProfesor ?? '—'
                  }`
                : 'Tu profesor te va a asignar una'
              : 'No se pudo cargar'
          }
        />
        <TarjetaResumen
          titulo="Shop"
          to="/alumno/shop"
          valor="Suplementos"
          detalle="Mirá los productos y comprá online"
        />
      </div>
    </Bienvenida>
  )
}