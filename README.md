## Arquitectura del Proyecto

### Módulos Core (`:core:*`)
- **`:core:domain`**: Modelos de dominio puros, interfaces de repositorios y casos de uso.
- **`:core:data`**: Implementaciones de repositorios, fuentes de datos y mappers de entidades.
- **`:core:network`**: Configuración de red y servicios externos (Cloudinary).
- **`:core:ui`**: Componentes de interfaz reutilizables, temas, colores y tipografía basados en Material 3.

### Módulos de Características (`:feature:*`)
- **`:feature:auth`**: Autenticación de usuarios (Google Sign-In).
- **`:feature:explore`**: Exploración de lugares públicos compartidos por otros usuarios, likes y comentarios.
- **`:feature:home`**: Gestión de lugares guardados personales ("Mis lugares"), mapa y navegación principal.
- **`:feature:notifications`**: Sistema de notificaciones en tiempo real (comentarios, me gusta).
- **`:feature:places`**: Creación y registro de nuevos lugares con geolocalización y fotos.
- **`:feature:profile`**: Perfil de usuario, estadísticas (lugares, me gusta), edición de perfil y preferencias de tema.

---

## Tecnologías y Librerías

- **Jetpack Compose & Material 3**: UI declarativa moderna y adaptable.
- **Kotlin Coroutines & Flow**: Manejo de asincronía y flujos de datos reactivos.
- **Hilt (Dagger)**: Inyección de dependencias.
- **Firebase (Auth & Firestore)**: Autenticación y base de datos en la nube.
- **Coil**: Carga eficiente de imágenes asíncronas.
- **osmdroid**: Mapas OpenStreetMap interactivos.

---
