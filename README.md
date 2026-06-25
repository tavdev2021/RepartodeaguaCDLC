# Reparto de agua CDLC: Sistema Multi-Módulo de Distribución y Reparto de Agua Purificada 💧📦

**Reparto de agua CDLC** es un ecosistema de aplicaciones nativas para Android diseñado para optimizar y resolver los problemas críticos en la logística de reparto de agua purificada en garrafones. El proyecto está estructurado bajo una arquitectura moderna **multi-módulo**, dividiéndose en dos aplicaciones principales que interactúan a través de un núcleo compartido de datos.

---

## 📱 Componentes del Ecosistema

El proyecto se compone de dos aplicaciones robustas e independientes:

### 1. App Repartidor (Módulo `:app-delivery`)
Diseñada específicamente para el trabajo en campo, donde la conectividad suele ser inestable o nula.
* **Enfoque Offline-First:** Permite al repartidor gestionar rutas, registrar ventas, devoluciones y visitas sin conexión a internet.
* **Persistencia Local:** Sincronización inmediata con base de datos local.
* **Sincronización Inteligente:** Al detectar conexión a red, los cambios locales se respaldan y unifican de forma asíncrona en la nube.

### 2. App Administrador (Módulo `:app-admin`)
El panel de control en tiempo real para la gestión del negocio.
* **Fuente de Verdad Única:** Conectada directamente a los servicios en la nube para monitorear las operaciones del día.
* **Monitoreo en Tiempo Real:** Visualización instantánea de las ventas, inventarios por repartidor y estatus de las rutas conforme se sube la información desde el campo.

---

## 🛠️ Stack Tecnológico & Arquitectura

Ambas aplicaciones comparten un estándar de ingeniería de software moderno y de alto rendimiento:

* **Lenguaje:** Kotlin (100%)
* **Interfaz de Usuario:** Jetpack Compose (Diseño declarativo y moderno)
* **Patrón de Arquitectura:** MVVM (Model-View-ViewModel) con una estricta separación de capas (UI, Domain, Data).
* **Concurrencia:** Kotlin Coroutines para operaciones en hilos secundarios, evitando por completo el bloqueo de la Main UI Thread.
* **Reactividad:** `StateFlow` y `SharedFlow` para la observación reactiva de estados de la UI y eventos del sistema.
* **Base de Datos Local (Repartidor):** Room Database con soporte para migraciones y relaciones complejas.
* **Backend & Sincronización:** Firebase (Firestore / Realtime Database) como base de datos en la nube y autenticación.

---

## 📐 Estructura de Módulos del Proyecto

El proyecto implementa modularización para mejorar los tiempos de compilación, el aislamiento de código y la reutilización de componentes:

```text
├── :core (Clases base, utilerías de red, extensiones comunes)
├── :data (Modelos de datos, repositorios compartidos, entidades de Firebase)
├── :app-delivery (Lógica de negocio, ViewModels y pantallas del Repartidor + Room)
└── :app-admin (Lógica de negocio, ViewModels y pantallas del Administrador)
