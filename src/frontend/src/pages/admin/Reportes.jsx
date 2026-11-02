import { useEffect, useState } from 'react'
import GraficoColumnas from '../../components/GraficoColumnas'
import ListaBarras from '../../components/ListaBarras'
import TablaDatos from '../../components/TablaDatos'
import { reportesService } from '../../services/reportes'
import { mensajeDeError } from '../../utils/errores'

// Colores de los gráficos (validados para daltonismo sobre fondo oscuro).
// Cuando el grupo defina la paleta, se cambian solo estas dos líneas.
const COLOR_1 = '#3987e5' // azul
const COLOR_2 = '#d95926' // naranja

const claseSelect =
  'rounded-lg border border-neutral-700 bg-neutral-950 px-3 py-2 text-sm outline-none focus:border-emerald-500'

const MESES = ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic']
const MESES_LARGOS = [
  'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
  'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre',
]
const DIAS = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo']

const plata = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', maximumFractionDigits: 0 })
const compacto = new Intl.NumberFormat('es-AR', { notation: 'compact', maximumFractionDigits: 1 })
const formatoPlata = (valor, corto) => (corto ? compacto.format(valor) : plata.format(valor))
// En los ejes y etiquetas (corto) va solo el número; en el tooltip, con la unidad.
const formatoVisitas = (valor, corto) => (corto ? String(valor) : `${valor} ${valor === 1 ? 'ingreso' : 'ingresos'}`)

function Tarjeta({ titulo, subtitulo, children }) {
  return (
    <section className="rounded-2xl border border-neutral-800 bg-neutral-900 p-5">
      <h3 className="text-lg font-semibold">{titulo}</h3>
      {subtitulo && <p className="mb-4 mt-1 text-sm text-neutral-400">{subtitulo}</p>}
      {children}
    </section>
  )
}

function Indicador({ titulo, valor, grande = false }) {
  return (
    <div className="rounded-2xl border border-neutral-800 bg-neutral-900 p-4">
      <p className="text-xs text-neutral-400">{titulo}</p>
      <p className={`mt-1 font-semibold ${grande ? 'text-4xl text-emerald-400' : 'text-xl'}`}>{valor}</p>
    </div>
  )
}

function Leyenda({ items }) {
  return (
    <ul className="mb-3 flex flex-wrap gap-4 text-sm text-neutral-300">
      {items.map((item) => (
        <li key={item.nombre} className="flex items-center gap-2">
          <span className="inline-block h-2.5 w-2.5 rounded-sm" style={{ backgroundColor: item.color }} />
          {item.nombre}
        </li>
      ))}
    </ul>
  )
}

export default function Reportes() {
  const anioActual = new Date().getFullYear()
  const [anio, setAnio] = useState(anioActual)
  const [mes, setMes] = useState('')
  // "clave" dice a qué filtro corresponden los datos: mientras no coincide con el filtro actual, se está recargando.
  const [ingresos, setIngresos] = useState({ clave: null, datos: null, error: '' })
  const [afluencia, setAfluencia] = useState({ datos: null, error: '' })
  const [actividades, setActividades] = useState({ datos: null, error: '' })

  const claveFiltro = `${anio}-${mes}`

  useEffect(() => {
    let cancelado = false
    reportesService
      .ingresos(anio, mes === '' ? null : Number(mes))
      .then((datos) => {
        if (!cancelado) setIngresos({ clave: claveFiltro, datos, error: '' })
      })
      .catch((err) => {
        if (!cancelado) setIngresos({ clave: claveFiltro, datos: null, error: mensajeDeError(err) })
      })
    return () => {
      cancelado = true
    }
  }, [anio, mes, claveFiltro])

  useEffect(() => {
    let cancelado = false
    reportesService
      .afluencia()
      .then((datos) => {
        if (!cancelado) setAfluencia({ datos, error: '' })
      })
      .catch((err) => {
        if (!cancelado) setAfluencia({ datos: null, error: mensajeDeError(err) })
      })
    reportesService
      .actividades()
      .then((datos) => {
        if (!cancelado) setActividades({ datos, error: '' })
      })
      .catch((err) => {
        if (!cancelado) setActividades({ datos: null, error: mensajeDeError(err) })
      })
    return () => {
      cancelado = true
    }
  }, [])

  const recargando = ingresos.clave !== claveFiltro
  const reporte = ingresos.datos

  // ---- Ingresos: una columna por mes del año, con ceros en los meses sin pagos ----
  const seriesIngresos = [
    { nombre: 'Suscripciones', color: COLOR_1 },
    { nombre: 'Shop', color: COLOR_2 },
  ]
  const categoriasIngresos = reporte
    ? MESES.map((etiqueta, i) => {
        const mensual = reporte.desgloseMensual.find((m) => m.mes === i + 1)
        return {
          etiqueta,
          titulo: `${MESES_LARGOS[i]} ${reporte.anio}`,
          valores: [mensual?.ingresosSuscripciones ?? 0, mensual?.ingresosShop ?? 0],
        }
      })
    : []

  // ---- Afluencia: los siete días en orden y las horas en orden, aunque el backend los devuelva por cantidad ----
  const dias = afluencia.datos
    ? DIAS.map((nombre, i) => ({
        etiqueta: nombre.slice(0, 3),
        titulo: nombre,
        valores: [afluencia.datos.porDiaSemana.find((d) => d.numeroDia === i + 1)?.cantidadConcurrencias ?? 0],
      }))
    : []
  let horas = []
  if (afluencia.datos && afluencia.datos.porRangoHorario.length > 0) {
    const inicios = afluencia.datos.porRangoHorario.map((h) => h.horaInicio)
    const desde = Math.min(...inicios)
    const hasta = Math.max(...inicios)
    for (let h = desde; h <= hasta; h++) {
      const dato = afluencia.datos.porRangoHorario.find((r) => r.horaInicio === h)
      horas.push({
        etiqueta: String(h).padStart(2, '0'),
        titulo: dato?.rangoHorario ?? `${String(h).padStart(2, '0')}:00 - ${String((h + 1) % 24).padStart(2, '0')}:00`,
        valores: [dato?.cantidadConcurrencias ?? 0],
      })
    }
  }
  const serieAfluencia = [{ nombre: 'Ingresos', color: COLOR_1 }]

  return (
    <div className="max-w-5xl">
      <h2 className="text-2xl font-bold">Reportes</h2>
      <p className="mb-6 mt-1 text-sm text-neutral-400">
        Datos de ejemplo del prototipo. Con el backend conectado se calculan con los pagos, reservas y check-ins reales.
      </p>

      <div className="mb-6 flex flex-wrap items-center gap-3">
        <label className="flex items-center gap-2 text-sm text-neutral-400">
          Año
          <select className={claseSelect} value={anio} onChange={(e) => setAnio(Number(e.target.value))}>
            {[anioActual, anioActual - 1, anioActual - 2].map((a) => (
              <option key={a} value={a}>
                {a}
              </option>
            ))}
          </select>
        </label>
        <label className="flex items-center gap-2 text-sm text-neutral-400">
          Mes
          <select className={claseSelect} value={mes} onChange={(e) => setMes(e.target.value)}>
            <option value="">Todo el año</option>
            {MESES_LARGOS.map((nombre, i) => (
              <option key={nombre} value={i + 1}>
                {nombre}
              </option>
            ))}
          </select>
        </label>
        <span className="text-xs text-neutral-500">El filtro aplica a los ingresos.</span>
      </div>

      {ingresos.error ? (
        <p className="mb-6 text-red-400">{ingresos.error}</p>
      ) : !reporte ? (
        <p className="mb-6 text-neutral-400">Cargando ingresos...</p>
      ) : (
        <div className={`mb-6 transition-opacity ${recargando ? 'opacity-50' : ''}`}>
          <div className="mb-4 grid gap-3 sm:grid-cols-4">
            <div className="sm:col-span-2">
              <Indicador titulo="Ingresos totales" valor={plata.format(reporte.totalIngresos)} grande />
            </div>
            <Indicador titulo="Suscripciones" valor={plata.format(reporte.ingresosSuscripciones)} />
            <Indicador titulo="Shop" valor={plata.format(reporte.ingresosShop)} />
          </div>

          <Tarjeta
            titulo="Ingresos por mes"
            subtitulo={`Pagos aprobados en ${reporte.mes !== null ? `${MESES_LARGOS[reporte.mes - 1]} de ` : ''}${reporte.anio}: ${reporte.cantidadPagos} en total.`}
          >
            {reporte.cantidadPagos === 0 ? (
              <p className="text-sm text-neutral-400">No hay pagos aprobados en este período.</p>
            ) : reporte.mes !== null ? (
              <p className="text-sm text-neutral-400">
                Elegí "Todo el año" para ver la evolución mes a mes. En {MESES_LARGOS[reporte.mes - 1]} hubo{' '}
                {reporte.cantidadPagos} pagos.
              </p>
            ) : (
              <>
                <Leyenda items={seriesIngresos} />
                <GraficoColumnas
                  categorias={categoriasIngresos}
                  series={seriesIngresos}
                  formato={formatoPlata}
                  descripcion={`Ingresos mensuales de ${reporte.anio}, divididos entre suscripciones y shop`}
                />
                <TablaDatos
                  columnas={['Mes', 'Suscripciones', 'Shop', 'Total', 'Pagos']}
                  filas={reporte.desgloseMensual.map((m) => [
                    MESES_LARGOS[m.mes - 1],
                    plata.format(m.ingresosSuscripciones),
                    plata.format(m.ingresosShop),
                    plata.format(m.totalIngresos),
                    m.cantidadPagos,
                  ])}
                />
              </>
            )}
          </Tarjeta>
        </div>
      )}

      <div className="mb-6 grid gap-6 lg:grid-cols-2">
        <Tarjeta titulo="Afluencia por día" subtitulo="Reservas confirmadas y check-ins, según el día de la semana.">
          {afluencia.error ? (
            <p className="text-red-400">{afluencia.error}</p>
          ) : !afluencia.datos ? (
            <p className="text-neutral-400">Cargando...</p>
          ) : afluencia.datos.totalConcurrencias === 0 ? (
            <p className="text-sm text-neutral-400">Todavía no hay movimientos registrados.</p>
          ) : (
            <>
              <GraficoColumnas
                categorias={dias}
                series={serieAfluencia}
                formato={formatoVisitas}
                etiquetarMaximo
                ancho={440}
                descripcion="Ingresos por día de la semana"
              />
              <TablaDatos columnas={['Día', 'Ingresos']} filas={dias.map((d) => [d.titulo, d.valores[0]])} />
            </>
          )}
        </Tarjeta>

        <Tarjeta titulo="Afluencia por horario" subtitulo="Cantidad de ingresos en cada franja de una hora.">
          {afluencia.error ? (
            <p className="text-red-400">{afluencia.error}</p>
          ) : !afluencia.datos ? (
            <p className="text-neutral-400">Cargando...</p>
          ) : horas.length === 0 ? (
            <p className="text-sm text-neutral-400">Todavía no hay movimientos registrados.</p>
          ) : (
            <>
              <GraficoColumnas
                categorias={horas}
                series={serieAfluencia}
                formato={formatoVisitas}
                etiquetarMaximo
                ancho={440}
                descripcion="Ingresos por hora del día"
              />
              <TablaDatos columnas={['Franja', 'Ingresos']} filas={horas.map((h) => [h.titulo, h.valores[0]])} />
            </>
          )}
        </Tarjeta>
      </div>

      <Tarjeta titulo="Alumnos por actividad" subtitulo="Cuántos alumnos distintos participan en cada actividad (clases y check-ins libres).">
        {actividades.error ? (
          <p className="text-red-400">{actividades.error}</p>
        ) : !actividades.datos ? (
          <p className="text-neutral-400">Cargando...</p>
        ) : actividades.datos.length === 0 ? (
          <p className="text-sm text-neutral-400">Todavía no hay actividad registrada.</p>
        ) : (
          <>
            <ListaBarras
              color={COLOR_1}
              filas={actividades.datos.map((a) => ({
                nombre: a.tipoActividad,
                valor: a.cantidadAlumnos,
                detalle: `${a.cantidadAlumnos} alumnos · ${a.totalParticipaciones} participaciones`,
              }))}
            />
            <TablaDatos
              columnas={['Actividad', 'Alumnos', 'Participaciones']}
              filas={actividades.datos.map((a) => [a.tipoActividad, a.cantidadAlumnos, a.totalParticipaciones])}
            />
          </>
        )}
      </Tarjeta>
    </div>
  )
}