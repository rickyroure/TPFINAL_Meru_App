// Configuración general del frontend. Los valores salen del archivo .env.*
export const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'
export const KEYCLOAK_URL = import.meta.env.VITE_KEYCLOAK_URL ?? 'http://localhost:8081'
export const KEYCLOAK_REALM = import.meta.env.VITE_KEYCLOAK_REALM ?? 'meru-app'
export const KEYCLOAK_CLIENT_ID = import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? 'meru-frontend'

// Por defecto la app usa datos de prueba. Solo llama al backend real
// si VITE_USE_MOCK vale exactamente "false".
export const USE_MOCK = import.meta.env.VITE_USE_MOCK !== 'false'
