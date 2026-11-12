import { useEffect, useState } from 'react'
import Bienvenida from '../../components/Bienvenida'
import TarjetaResumen from '../../components/TarjetaResumen'
import { reportesService } from '../../services/reportes'
import { planesService } from '../../services/planes'
import { productosService } from '../../services/productos'
import { clasesService } from '../../services/clases'
import { formatearFechaHora, formatearPrecio } from '../../utils/formato'

const STOCK_BAJO = 5

export default function InicioAdmin() {
  // null mientras carga. Después queda [ingresos, planes, productos, clases]:
  // si alguna consulta falló, su lugar queda en null y solo esa tarjeta muestra "—".
  const [datos, setDatos] = useState(null)
  const [ahora] = useState(() => Date.now())
  const anio = new Date(ahora).getFullYear()

  useEffect(() => {
    let cancelado = false
    Promise.allSettled([
      reportesService.ingresos(anio),
      planesService.listarPlanes(),
      productosService.listar(),
      clasesService.listarTodas(),
    ]).then((resultados) => {
      if (cancelado) return
      setDatos(resultados.map((r) => (r.status === 'fulfilled' ? r.value : null)))
    })
    return () => {
      cancelado = true
    }
  }, [anio])

  if (datos === null) return <p className="text-neutral-400">Cargando el resumen...</p>
  const [ingresos, planes, productos, clases] = datos

  const sinStock = productos?.filter((p) => p.stock <= 0).length ?? 0
  const stockBajo = productos?.filter((p) => p.stock > 0 && p.stock <= STOCK_BAJO).length ?? 0
  const clasesProximas = clases
    ?.filter((c) => new Date(c.horario).getTime() > ahora)
    .sort((a, b) => a.horario.localeCompare(b.horario))
  const precioMinimo = planes?.length ? Math.min(...planes.map((p) => p.precio)) : null

  return (
    <Bienvenida descripcion="Este es el resumen general del gimnasio.">
      <div className="grid gap-4 sm:grid-cols-2">
        <div className="sm:col-span-2">
          <TarjetaResumen
            titulo={`Ingresos ${anio}`}
            to="/admin/reportes"
            valor={ingresos ? formatearPrecio(ingresos.totalIngresos) : null}
            detalle={
              ingresos
                ? `Suscripciones ${formatearPrecio(ingresos.ingresosSuscripciones)} · Shop ${formatearPrecio(
                    ingresos.ingresosShop,
                  )}`
                : 'No se pudo cargar'
            }
          />
        </div>
        <TarjetaResumen
          titulo="Planes"
          to="/admin/planes"
          valor={planes ? String(planes.length) : null}
          detalle={
            planes
              ? precioMinimo !== null
                ? `Desde ${formatearPrecio(precioMinimo)}`
                : 'Todavía no hay planes'
              : 'No se pudo cargar'
          }
        />
        <TarjetaResumen
          titulo="Productos del shop"
          to="/admin/productos"
          valor={productos ? String(productos.length) : null}
          detalle={
            productos
              ? sinStock + stockBajo > 0
                ? `${sinStock} sin stock · ${stockBajo} con stock bajo`
                : 'Stock en orden'
              : 'No se pudo cargar'
          }
          aviso={!productos || sinStock + stockBajo > 0}
        />
        <div className="sm:col-span-2">
          <TarjetaResumen
            titulo="Clases próximas"
            to="/admin/clases"
            valor={clasesProximas ? String(clasesProximas.length) : null}
            detalle={
              clasesProximas
                ? clasesProximas.length > 0
                  ? `La próxima: ${clasesProximas[0].tipoActividad} · ${formatearFechaHora(clasesProximas[0].horario)}`
                  : 'No hay clases programadas'
                : 'No se pudo cargar'
            }
          />
        </div>
      </div>
    </Bienvenida>
  )
}