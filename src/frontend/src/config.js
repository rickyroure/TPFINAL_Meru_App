// Configuración general del frontend. Los valores salen del archivo .env.*
export const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

// Por defecto la app usa datos de prueba. Solo llama al backend real
// si VITE_USE_MOCK vale exactamente "false".
export const USE_MOCK = import.meta.env.VITE_USE_MOCK !== 'false'