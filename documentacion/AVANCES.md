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

## 2026-08-01 — Fase 3 completada: navegación de carpetas
- `FileSystemService`: `listarUnidades()` (discos C:, D:, E:…) y `listarSubcarpetas(ruta)` con manejo de errores (carpetas sin permisos devuelven lista vacía, no rompen).
- `FolderController` (REST):
  - `GET /api/unidades` → unidades de disco.
  - `GET /api/carpetas?ruta=...` → subcarpetas de una ruta (JSON).
- `dto/CarpetaDTO` (ruta + nombre).
- `scan.html` rediseñado: panel "Paso 1: elegí la carpeta o disco" con:
  - Chips de unidades.
  - Campo de ruta editable + botón "Ir".
  - Breadcrumb navegable (clic en cada nivel).
  - Grilla de subcarpetas (clic para entrar).
  - Botón "Escanear esta carpeta" (form POST).
- `static/js/scan.js`: lógica de navegación (fetch, breadcrumb, grid).
- `progreso.html`: placeholder de escaneo (la detección real es Fase 4).
- `ScanController` + `POST /scan/{tipo}/iniciar` (recibe ruta, muestra progreso).
- **Pruebas**: `mvnw compile` OK. App levantada:
  - `/api/unidades` → C:\, D:\, E:\.
  - `/api/carpetas` funciona con rutas con espacios (`C:\Archivos de programa`) y navega `C:\ejercicios`, `C:\Users`.
  - `/scan/VIDEOS` → 200 · `POST /scan/VIDEOS/iniciar` → 200.
  - Carpeta sin permisos → lista vacía sin errores.
- Pendiente: Fase 4 (escaneo asíncrono con progreso + detección de duplicados).

## 2026-08-01 — Ajuste UX: selección de carpeta con botón + modal
- Cambio según feedback del usuario: el navegador a la vista ya no muestra todas las carpetas del disco de una.
- Ahora la pantalla de escaneo tiene un botón **"Seleccionar carpeta"** que abre un **modal** con el buscador (unidades, campo de ruta, breadcrumb y grilla de subcarpetas).
- El modal tiene botón **"Seleccionar esta carpeta"** y **"Cancelar"**.
- Al seleccionar: el modal se cierra y queda visible **solo la carpeta elegida** (resaltada), con el botón "Escanear esta carpeta" habilitado.
- Extras: cierre con `Escape`, clic fuera del modal y `Enter` en el campo de ruta.
- **Pruebas**: `mvnw compile` OK. `/scan/FOTOS` → 200 con botón y modal. `POST /scan/FOTOS/iniciar` → 200. JS validado con `node --check`.

## 2026-08-01 — DevTools configurado + filtro de carpetas ocultas
- `application.properties`: agregados `spring.devtools.restart.enabled=true`, `spring.devtools.livereload.enabled=true` y `spring.thymeleaf.cache=false` (así el proyecto se reinicia solo al guardar cambios y las plantillas se ven al refrescar).
- `FileSystemService`: nuevo filtro `esCarpetaOculta(Path)` en `listarSubcarpetas` que oculta:
  - Carpetas que empiezan con `$` (`$Recycle.Bin`, `$WINDOWS.~BT`, etc.).
  - Carpetas que empiezan con `.` (`.m2`, `.gradle`, `.vscode`, etc.).
  - Carpetas con atributo DOS oculto o de sistema (`Files.readAttributes` → `DosFileAttributes`).
- Corrección de import: `DosFileAttributes` vive en `java.nio.file.attribute`, no en `java.nio.file` (causaba "cannot find symbol").
- **Pruebas**: `mvnw compile` OK. `C:\` pasó de 28 a 19 carpetas visibles; `C:\Users\LUCERO-PC` de 64 a 29, sin `.` ni `$` ni ocultas de sistema.

## 2026-08-01 — Fase 4 (FOTOS): escaneo recursivo + detección de duplicados
- Enfocado en **fotos** primero (el motor es genérico para los 4 tipos; se testea con FOTOS).
- `config/FileExtensionsConfig`: extensiones por tipo (FOTOS: jpg, jpeg, png, gif, bmp, webp, tiff, heic, raw, cr2, nef, jfif, svg).
- `config/AsyncConfig`: `ThreadPoolTaskExecutor` (bean `scanExecutor`) para escaneo en segundo plano.
- `service/HashService`: hash SHA-256 por streaming (buffer 8 KB); devuelve `null` si no se puede leer.
- `service/ScanService`:
  - `iniciarEscaneo(tipo, ruta)` valida la ruta, crea `ScanSession` EN_PROGRESO y ejecuta el escaneo en el executor.
  - Escaneo **recursivo** con `Files.walkFileTree` (incluye subcarpetas; carpetas sin permiso se omiten).
  - Fase 1: conteo total. Fase 2: recolección con progreso (guarda cada 100). Fase 3: detección.
  - Detección: agrupa por **nombre+tamaño** → candidatos → **hash SHA-256** → grupos reales (primer archivo = ORIGINAL, resto = DUPLICADO).
- `ScanController`: `POST /scan/{tipo}/iniciar` (valida y redirige), `GET /escaneo/{id}` (progreso), `GET /escaneo/{id}/resultado`.
- `ScanApiController`: `GET /api/escaneo/{id}` → JSON `{estado, total, procesados, duplicados}` para el polling.
- `DuplicateGroup.archivos` ahora `cascade=ALL` para persistir archivos junto al grupo. `DuplicateGroupRepository.findBySesion_IdOrderById`.
- Plantillas: `progreso.html` (barra de progreso con polling cada 1s, redirige al resultado al completar) y `resultado.html` (resumen + grupos con etiquetas ORIGINAL/DUPLICADO y ruta completa).
- Fix: `#lists.first(g.archivos)` no acepta colecciones de Hibernate → se usa `g.archivos[0].nombre`.
- Fix importante: H2 URL ahora `jdbc:h2:file:./data/limpiamedia;AUTO_SERVER=TRUE` para que DevTools (reinicio automático) no falle por bloqueo del archivo H2.
- **Pruebas end-to-end** (carpeta con subcarpetas):
  - 7 fotos (2 duplicadas + 1 "trampa" con mismo nombre y tamaño pero contenido distinto + 1 única + 1 .txt ignorado) → `total=7`, `duplicados=2`, la "trampa" fue **descartada por el hash**. ✓
  - Carpeta sin duplicados → `duplicados=0` y mensaje "No se encontraron". ✓
  - Resultado muestra ORIGINAL/DUPLICADO y rutas. ✓
  - `mvnw test` OK.
- Pendiente: mover duplicados a carpeta (Fase 5) y probar con videos/documentos/sonido.

## 2026-08-01 — Fase 5 completada: mover duplicados + exclusión + restauración
- El motor sigue siendo genérico; la Fase 5 se probó con FOTOS.
- Entidades nuevas:
  - `MoveRecord`: registro de cada archivo movido (sesión, archivo, tipo, ruta original, ruta nueva, fecha, restaurado).
  - `CarpetaExcluida`: carpeta destino de duplicados que debe saltearse en futuros escaneos.
  - Campos nuevos: `ScanSession.carpetaDestino`, `ScannedFile.movido`.
- Repositorios: `MoveRecordRepository`, `CarpetaExcluidaRepository`, `ScannedFileRepository.findBySesionIdAndEsDuplicadoTrue`.
- `service/MoveService`:
  - `moverDuplicados(sesionId, carpetaDestino)`: crea la carpeta destino, registra la exclusión, mueve los duplicados (si el nombre ya existe usa `_1`, `_2`…), marca `movido=true`, guarda `MoveRecord` y escribe en el journal.
  - `restaurar(moveId)`: mueve el archivo de vuelta a su ubicación original (si el original quedó ocupado, crea `_1`), marca `restaurado=true` y lo registra en el journal.
  - **Journal de cambios**: archivo `registro/movimientos.jsonl` (una línea JSON por evento `mover`/`restaurar`, con escape manual de `\` y `"`, sin dependencia de Jackson).
- `service/ScanService`: `cargarExcluidas` + `estaExcluida` con `SKIP_SUBTREE`. La exclusión es **global** (no depende de la raíz escaneada), así no se re-detectan los movidos aunque el escaneo arranque en otra raíz.
- `ScanController`: `POST /escaneo/{id}/mover` (con flash `movidos`/`error`) y sugerencia de destino `Desktop\LimpiaMedia_Duplicados_{TIPO}`.
- `MoveController`: `GET /movimientos` (historial) y `POST /movimientos/{id}/restaurar`.
- Frontend:
  - `static/js/carpetas.js`: buscador de carpetas (modal) extraído y compartido entre escaneo y resultado (`buscarCarpetaConfig.campoDestino` / `alSeleccionar`).
  - `resultado.html`: panel "Mover duplicados" con campo precargado con la sugerencia, botón "Cambiar carpeta" que abre el modal, mensaje flash al mover y panel "Duplicados ya movidos" con link al historial.
  - `movimientos.html`: tabla con fecha, tipo, original → nuevo, estado (MOVIDO/RESTAURADO) y botón Restaurar por fila.
  - `scan.js`/`resultado.js`: solo configuran el modal compartido.
  - `.gitignore`: agregada la carpeta `registro/`.
- **Pruebas end-to-end**:
  - Raíz con 2 pares duplicados + 1 único → `duplicados=2`. ✓
  - Mover → los 2 duplicados aparecen en `destino\LimpiaMedia_Duplicados_FOTOS`, los originales quedan. ✓
  - Journal con 2 líneas `mover` escapadas correctamente. ✓
  - Re-escaneo de una raíz **que contiene** la carpeta destino → `duplicados=0` (exclusión global). ✓
  - Restaurar → el archivo vuelve a su carpeta original y el historial marca RESTAURADO. ✓
  - `mvnw compile` OK.
- Pendiente: probar Fase 5 con VIDEOS/DOCUMENTOS/SONIDO y generar el `.jar` de distribución.
