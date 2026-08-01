# AVANCES — LimpiaMedia

Bitácora del proyecto. Formato: fecha · qué se hizo · estado.

## 2026-08-01 — Inicio del proyecto
- Se creó `leeme_primero.md` en la raíz con el contexto completo para IA.
- Se creó la carpeta `documentacion/` con `PLAN.md`, `AVANCES.md` y `HISTORIAL.md`.
- Se definieron decisiones clave:
  - Base de datos: **H2 embebida**.
  - Selección de carpeta: **navegador de carpetas** (unidades + árbol) + campo de ruta.
  - Criterio de duplicados: **nombre + tamaño** → luego **hash SHA-256**.
  - **Nunca se elimina**; solo se mueve.
- Pendiente: generar proyecto Spring Boot (Initializr) e importarlo al proyecto.

## 2026-08-01 — Proyecto Spring Boot importado y corregido
- Se generó el proyecto con **Spring Boot 4.1.0 · Java 21 · Maven · JAR** en start.spring.io.
- Correcciones en `pom.xml`:
  - Agregada dependencia faltante **`spring-boot-starter-web`** (sin ella no hay Tomcat/MVC y no serviría páginas).
  - Agregado **`spring-boot-devtools`** (reinicio automático en dev).
  - `groupId` corregido a `com.lucero`, `artifactId` a `limpiamedia`.
- Paquete Java renombrado de `LimpiaMedia` a **`com.lucero.limpiamedia`** (convención).
- Configurado `application.properties`: H2 embebida `jdbc:h2:file:./data/limpiamedia`, JPA `ddl-auto=update`, consola H2 habilitada.
- **`mvnw compile` OK**: el proyecto compila sin errores.
- Pendiente: Fase 2 (esqueleto de la aplicación: HomeController + index con 4 tarjetas + entidades).

## 2026-08-01 — Fase 2 completada: esqueleto de la aplicación
- Enums `ScanType` (FOTOS, VIDEOS, DOCUMENTOS, SONIDO) y `ScanStatus` (EN_PROGRESO, COMPLETADO, ERROR).
- Entidades JPA (convención de nombres en español, tablas en mayúsculas):
  - `ScanSession`: sesión de escaneo (tipo, ruta raíz, estado, fechas, contadores, archivos).
  - `ScannedFile`: archivo escaneado (nombre, ruta, carpeta padre, extensión, tamaño, hash, esDuplicado).
  - `DuplicateGroup`: grupo de duplicados (clave nombre+tamaño, hash, tamaño, archivos).
- Repositorios: `ScanSessionRepository`, `ScannedFileRepository`, `DuplicateGroupRepository`.
- `HomeController` (`GET /`) → `index.html` con las **4 tarjetas**.
- `ScanController` (`GET /scan/{tipo}`) → `scan.html` (placeholder de selección de carpeta).
- `static/css/style.css` con estilos de tarjetas y layout.
- **Pruebas**: `mvnw compile` OK. App levantada en `localhost:8080`:
  - `/` → 200 con 4 tarjetas · `/scan/FOTOS` → 200 · `/h2-console` → 200.
  - H2 creó las tablas `SCAN_SESSION`, `SCANNED_FILE`, `DUPLICATE_GROUP` (verificado por SQL).
- Pendiente: Fase 3 (navegación de carpetas: unidades + árbol + campo de ruta).
