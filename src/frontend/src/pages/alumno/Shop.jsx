import { useEffect, useState } from 'react'
import { useAuth } from '../../hooks/useAuth'
import { productosService } from '../../services/productos'
import { ordenesService } from '../../services/ordenes'
import { mensajeDeError } from '../../utils/errores'
import MisCompras from './MisCompras'

const STOCK_BAJO = 5

const formatearPrecio = (valor) =>
  new Intl.NumberFormat('es-AR', {
    style: 'currency',
    currency: 'ARS',
    maximumFractionDigits: 0,
  }).format(valor)

const claseBotonPestana = (activa) =>
  `rounded-lg px-4 py-2 text-sm font-medium transition ${
    activa ? 'bg-emerald-500 text-neutral-950' : 'border border-neutral-700 text-neutral-300 hover:bg-neutral-800'
  }`

export default function Shop() {
  const { usuario } = useAuth()
  const [pestana, setPestana] = useState('tienda') // 'tienda' | 'compras'
  const [productos, setProductos] = useState([])
  // El carrito guarda { [productoId]: cantidad }.
  const [carrito, setCarrito] = useState({})
  const [cargando, setCargando] = useState(true)
  const [errorCarga, setErrorCarga] = useState('')
  const [pagando, setPagando] = useState(false)
  const [mensaje, setMensaje] = useState(null) // { tipo: 'exito' | 'error', texto }
  // Cada vez que cambia, se vuelven a pedir los productos (para ver el stock actualizado).
  const [version, setVersion] = useState(0)

  useEffect(() => {
    let cancelado = false
    productosService
      .listar()
      .then((datos) => {
        if (cancelado) return
        setProductos(datos)
        setErrorCarga('')
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
  }, [version])

  // Cambia la cantidad de un producto en el carrito, sin pasar del stock. Si llega a 0 se quita.
  const cambiarCantidad = (producto, diferencia) => {
    setMensaje(null)
    setCarrito((actual) => {
      const nueva = Math.min((actual[producto.id] ?? 0) + diferencia, producto.stock)
      if (nueva <= 0) {
        const resto = { ...actual }
        delete resto[producto.id]
        return resto
      }
      return { ...actual, [producto.id]: nueva }
    })
  }

  // La cantidad nunca pasa del stock actual: si el stock bajó (otra persona compró),
  // el carrito se ajusta solo y el producto sin stock desaparece de él.
  const lineas = productos
    .map((p) => ({ producto: p, cantidad: Math.min(carrito[p.id] ?? 0, p.stock) }))
    .filter((l) => l.cantidad > 0)
  const total = lineas.reduce((suma, l) => suma + l.producto.precio * l.cantidad, 0)
  const cantidadTotal = lineas.reduce((suma, l) => suma + l.cantidad, 0)

  const pagar = async () => {
    setMensaje(null)
    setPagando(true)
    try {
      const items = lineas.map((l) => ({ productoId: l.producto.id, cantidad: l.cantidad }))
      const pago = await ordenesService.comprar(usuario.alumnoId, items, total)
      setCarrito({})
      if (pago.checkoutUrl) {
        // Mercado Pago real: se lleva al usuario a la página de pago.
        window.location.assign(pago.checkoutUrl)
        return
      }
      if (pago.estado === 'APROBADO') {
        setMensaje({ tipo: 'exito', texto: '¡Compra realizada! Pago aprobado.' })
      } else {
        setMensaje({ tipo: 'error', texto: `El pago quedó ${pago.estado.toLowerCase()}. ${pago.mensaje ?? ''}` })
      }
      setVersion((v) => v + 1)
    } catch (err) {
      setMensaje({ tipo: 'error', texto: mensajeDeError(err) })
      // Si falló por falta de stock, se refresca el catálogo para ver lo disponible.
      setVersion((v) => v + 1)
    } finally {
      setPagando(false)
    }
  }

  return (
    <div className="max-w-5xl">
      <h2 className="text-2xl font-bold">Shop</h2>
      <p className="mb-5 mt-1 text-sm text-neutral-400">Suplementos y accesorios para tu entrenamiento.</p>

      <div className="mb-6 flex gap-2">
        <button onClick={() => setPestana('tienda')} className={claseBotonPestana(pestana === 'tienda')}>
          Tienda
        </button>
        <button onClick={() => setPestana('compras')} className={claseBotonPestana(pestana === 'compras')}>
          Mis compras
        </button>
      </div>

      {pestana === 'compras' ? (
        <MisCompras alumnoId={usuario.alumnoId} />
      ) : cargando ? (
        <p className="text-neutral-400">Cargando productos...</p>
      ) : errorCarga ? (
        <p className="text-red-400">{errorCarga}</p>
      ) : (
        <div className="grid gap-6 lg:grid-cols-3">
          <section className="lg:col-span-2">
            {mensaje && (
              <p
                className={`mb-4 rounded-lg px-3 py-2 text-sm ${
                  mensaje.tipo === 'exito' ? 'bg-emerald-500/15 text-emerald-400' : 'bg-red-500/15 text-red-400'
                }`}
              >
                {mensaje.texto}
              </p>
            )}

            {productos.length === 0 ? (
              <p className="text-neutral-400">Todavía no hay productos disponibles.</p>
            ) : (
              <div className="grid gap-4 sm:grid-cols-2">
                {productos.map((p) => {
                  const enCarrito = Math.min(carrito[p.id] ?? 0, p.stock)
                  const sinStock = p.stock <= 0
                  const alLimite = enCarrito >= p.stock
                  return (
                    <article
                      key={p.id}
                      className="flex flex-col justify-between rounded-2xl border border-neutral-800 bg-neutral-900 p-5"
                    >
                      <div>
                        <h3 className="font-semibold">{p.nombre}</h3>
                        <p className="mt-2 text-2xl font-bold">{formatearPrecio(p.precio)}</p>
                        {sinStock ? (
                          <p className="mt-1 text-sm text-red-400">Sin stock</p>
                        ) : p.stock <= STOCK_BAJO ? (
                          <p className="mt-1 text-sm text-amber-400">
                            ¡Últimas {p.stock} {p.stock === 1 ? 'unidad' : 'unidades'}!
                          </p>
                        ) : (
                          <p className="mt-1 text-sm text-neutral-400">{p.stock} disponibles</p>
                        )}
                      </div>
                      <button
                        onClick={() => cambiarCantidad(p, 1)}
                        disabled={sinStock || alLimite || pagando}
                        className="mt-4 rounded-lg bg-emerald-500 px-3 py-2 text-sm font-medium text-neutral-950 transition hover:bg-emerald-400 disabled:cursor-not-allowed disabled:opacity-40"
                      >
                        {enCarrito > 0 ? `Agregar otro (${enCarrito} en el carrito)` : 'Agregar al carrito'}
                      </button>
                    </article>
                  )
                })}
              </div>
            )}
          </section>

          <aside className="h-fit rounded-2xl border border-neutral-800 bg-neutral-900 p-5">
            <h3 className="mb-4 font-semibold">
              Carrito {cantidadTotal > 0 && <span className="text-neutral-500">({cantidadTotal})</span>}
            </h3>

            {lineas.length === 0 ? (
              <p className="text-sm text-neutral-500">Todavía no agregaste productos.</p>
            ) : (
              <>
                <ul className="divide-y divide-neutral-800">
                  {lineas.map(({ producto, cantidad }) => (
                    <li key={producto.id} className="py-3">
                      <p className="text-sm font-medium">{producto.nombre}</p>
                      <div className="mt-2 flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <button
                            onClick={() => cambiarCantidad(producto, -1)}
                            disabled={pagando}
                            aria-label={`Quitar una unidad de ${producto.nombre}`}
                            className="h-7 w-7 rounded-md border border-neutral-700 text-neutral-300 transition hover:bg-neutral-800 disabled:opacity-40"
                          >
                            −
                          </button>
                          <span className="w-6 text-center text-sm">{cantidad}</span>
                          <button
                            onClick={() => cambiarCantidad(producto, 1)}
                            disabled={pagando || cantidad >= producto.stock}
                            aria-label={`Agregar una unidad de ${producto.nombre}`}
                            className="h-7 w-7 rounded-md border border-neutral-700 text-neutral-300 transition hover:bg-neutral-800 disabled:opacity-40"
                          >
                            +
                          </button>
                        </div>
                        <span className="text-sm text-neutral-300">
                          {formatearPrecio(producto.precio * cantidad)}
                        </span>
                      </div>
                    </li>
                  ))}
                </ul>

                <div className="mt-4 flex items-center justify-between border-t border-neutral-800 pt-4">
                  <span className="text-sm text-neutral-400">Total</span>
                  <span className="text-xl font-bold text-emerald-400">{formatearPrecio(total)}</span>
                </div>
                <button
                  onClick={pagar}
                  disabled={pagando}
                  className="mt-4 w-full rounded-lg bg-emerald-500 px-3 py-2 text-sm font-medium text-neutral-950 transition hover:bg-emerald-400 disabled:opacity-60"
                >
                  {pagando ? 'Procesando...' : 'Pagar con Mercado Pago'}
                </button>
                <button
                  onClick={() => setCarrito({})}
                  disabled={pagando}
                  className="mt-2 w-full rounded-lg border border-neutral-700 px-3 py-2 text-sm text-neutral-300 transition hover:bg-neutral-800 disabled:opacity-50"
                >
                  Vaciar carrito
                </button>
              </>
            )}
          </aside>
        </div>
      )}
    </div>
  )
}