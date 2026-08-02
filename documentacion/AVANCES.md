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

## 2026-08-01 — Limpieza de datos de prueba
- Se borraron `data/` (H2) y `registro/` (journal) para que la app arranque con el historial vacío.
- El registro "RESTAURADO" que se veía en `/movimientos` era residuo del test end-to-end de la Fase 5 (se movieron y restauraron archivos de prueba), no una acción del usuario.

## 2026-08-01 — Fase 5 probada con VIDEOS, DOCUMENTOS y SONIDO + `.jar` de distribución
- Se probó el ciclo completo (detectar → mover → restaurar) con los 3 tipos restantes:
  - **VIDEOS** (`mp4`, `avi`): 3 archivos → 1 duplicado → movido → restaurado. ✓
  - **DOCUMENTOS** (`pdf`, `xlsx`): 3 archivos → 1 duplicado → movido → restaurado. ✓
  - **SONIDO** (`mp3`, `wav`): 3 archivos → 1 duplicado → movido → restaurado. ✓
- El journal `registro/movimientos.jsonl` registró cada evento `mover`/`restaurar` correctamente (con escape de `\`).
- Se generó el `.jar` ejecutable: `target/limpiamedia-0.0.1-SNAPSHOT.jar` (52 MB) con `mvnw clean package` (tests incluidos, OK).
- **Smoke test**: el `.jar` corre standalone con `java -jar` (se probó en el puerto 8099 → 200). ✓
- Se limpiaron `data/` y `registro/` otra vez para dejar la app con historial vacío.
- Los 4 tipos (FOTOS, VIDEOS, DOCUMENTOS, SONIDO) quedan cubiertos por pruebas end-to-end.

## 2026-08-01 — Verificados los triplicados (3+ copias)
- Confirmado que la detección maneja **3 o más copias**: grupo de 3 archivos idénticos → 1 ORIGINAL + 2 DUPLICADO (`i > 0` en `ScanService.detectarDuplicados`).
- Prueba end-to-end (carpeta con 3 fotos idénticas + 1 única): `total=4`, `duplicados=2`. ✓
- Mover: los 2 duplicados van a la carpeta destino y el segundo se renombra `foto_1.jpg` (no pisa al primero). ✓
- Restaurar: ambos vuelven a sus rutas originales (`b\foto.jpg`, `c\foto.jpg`). ✓

## 2026-08-01 — Botón "Abrir carpeta destino" después de mover
- En `resultado.html`, el panel "Duplicados ya movidos" (que aparece cuando `carpetaDestino != null`, o sea después de mover) ahora tiene el botón **"Abrir carpeta destino"**.
- El botón llama a `GET /escaneo/{id}/abrir-carpeta`, que lanza `explorer.exe` con la ruta destino de esa sesión (solo permite abrir carpetas guardadas en la base, no rutas arbitrarias) y vuelve al resultado con un flash.
- Como `resultado.html` es genérico, el botón funciona para **FOTOS, VIDEOS, DOCUMENTOS y SONIDO**.
- **Pruebas**: después de mover 1 duplicado, el botón aparece; al clickear, Explorer abrió la carpeta (flash "Se abrió la carpeta destino en el Explorador" ✓, procesos `explorer.exe` nuevos ✓). `mvnw clean package` OK y `.jar` regenerado.

## 2026-08-01 — Detección mejorada: copias de Windows con nombre distinto
- Antes solo se agrupaba por **nombre + tamaño** exactos. Ahora también se detectan las **copias de Windows** cuyo nombre difiere solo por el marcador de copiado:
  - Sufijo numérico: `VID_20260105_161551289_1.mp4`, `_2`, `_3`, … (ejemplo de teléfono/DJI).
  - Prefijos: `copia de foto.jpg`, `Copy of foto.jpg`, `copia_foto.jpg`.
  - Sufijos de Windows: `foto - copia.jpg`, `foto - Copy.jpg`, `foto - Copy (2).jpg`, `foto (2).jpg`.
- Implementación en `ScanService.normalizarNombre(nombre, nombresPresentes)`:
  - Los marcadores de texto inequívocos se quitan siempre.
  - El sufijo numérico `_N` solo se quita **si el nombre base existe entre los archivos escaneados** (el copiado convive con el original). Así **no se rompen timestamps** como `VID_20260105_161551289.mp4` ni nombres reales como `reporte_2025.pdf`.
  - El **hash SHA-256 sigue confirmando el contenido**: dos nombres iguales-norm con contenido distinto quedan fuera (sin falsos positivos).
- En cada grupo, el **ORIGINAL** es el archivo con nombre base (sin marcador), y las copias quedan como DUPLICADO y se **proponen para mover** (todo automático en la UI existente).
- Tests unitarios nuevos: `src/test/.../ScanServiceNombreTest.java` (7 casos). `mvnw test` OK.
- **Prueba end-to-end** (VIDEOS): `VID_...mp4` + `VID_..._1.mp4` + `VID_..._2.mp4` + un `VID_..._1.mp4` con contenido distinto (trampa) → `duplicados=2`, la trampa descartada por hash, mover mueve `_1` y `_2` y deja el original. ✓ (FOTOS): `copia de foto.jpg` + `foto.jpg` → 1 duplicado. ✓
- `.jar` regenerado con `mvnw clean package`.

## 2026-08-01 — Escaneo de disco completo: diagnóstico + arreglos de UX
- El usuario reportó que al escanear **todo `D:\`** la app decía "1.357 archivos" y en otro escaneo "no había duplicados".
- **Diagnóstico con la base real** (H2 Shell sobre `data/limpiamedia`):
  - Sesión FOTOS `D:\`: COMPLETADO con **7.879 archivos y 2.500 duplicados** (el escaneo de fotos SÍ funcionaba; coincide con un conteo externo de 7.881 imágenes).
  - La cifra "1.357/1.332" era un escaneo de **VIDEOS** de `D:\` (1.332 videos) todavía en curso, no fotos.
  - El "no había duplicados" era un **bug de UI**: la página de resultado se podía abrir con el escaneo EN_PROGRESO y mostraba "No se encontraron duplicados" en falso.
- **Arreglos**:
  - `ScanController.resultado`: si el escaneo está EN_PROGRESO redirige al progreso; si está ERROR avisa y vuelve al escaneo. Ya no muestra resultados falsos.
  - `ScanController.iniciar`: si ya hay un escaneo EN_PROGRESO, rechaza el nuevo con el mensaje "Ya hay un escaneo en curso" (antes se encolaba en silencio detrás del único hilo del executor).
  - `config/LimpiezaSesionesListener`: al iniciar la app, las sesiones EN_PROGRESO huérfanas (de un reinicio previo) se marcan ERROR para no bloquear escaneos nuevos.
  - Rendimiento: guardado en BD cada 500 archivos (antes 100) y cada 100 hashes (antes 25).
- **Pruebas** (instancia aislada en :8081 con base temporal): guard rechaza con flash ✓, resultado de sesión en curso redirige a progreso ✓, al reiniciar la sesión huérfana pasa a ERROR ✓, detección end-to-end sigue OK ✓. `mvnw clean package` OK y `.jar` regenerado.
- **Aclaración**: no hay límite de capacidad; el escaneo recorre todo el disco. Escanear `D:\` completo lleva varios minutos (caminata + hash de cada candidato), es normal. Faltan por mejorar: tiempo del escaneo en discos grandes y límites de memoria con millones de archivos.

## 2026-08-01 — Progreso con fases y tiempo restante estimado (ETA)
- **Bug corregido**: la barra llegaba a 100% antes de terminar porque durante el hash `procesados` seguía creciendo por encima de `total` (los archivos a verificar son más que los totales: 7.879 fotos → se hasheaban miles). Ahora:
  - La sesión tiene **fases** (`ScanPhase`): `CONTANDO` → `RECOLECTANDO` → `VERIFICANDO`.
  - El hash usa contadores propios (`totalVerificar` / `procesadosVerificar`): se calcula de antemano cuántos candidatos hay y la barra de verificación se basa en eso. Nunca más pasa del 100%.
- **Nuevo**: la página de progreso muestra por fase:
  - `Contando archivos…` (sin barra, hasta conocer el total).
  - Recolectando: `X / total (pct%) · quedan aprox. N min · transcurrido M`.
  - Verificando: `Comparando contenido (hash) X / Y · quedan aprox. …`.
  - El ETA se calcula en el navegador a partir de la velocidad de los últimos polls (suavizado), y el tiempo transcurrido desde el inicio real de la sesión (viene en la API como `inicioEpoch`).
- **API `/api/escaneo/{id}`** ahora devuelve además: `fase`, `totalVerificar`, `procesadosVerificar`, `inicioEpoch`.
- **Infraestructura**: los tests (`LimpiaMediaApplicationTests`) usan una **BD H2 en memoria** (nueva `src/test/resources/application.properties`) y ya no tocan la BD real del usuario (antes cada build marcaba como ERROR sus sesiones en curso por el listener de limpieza).
- **Migración BD real**: las columnas nuevas (`fase`, `total_verificar`, `procesados_verificar`) se crean **nullable** para que `ddl-auto=update` funcione sobre la BD existente con filas (un `long` primitivo genera `NOT NULL` y H2 rechaza el `alter` con filas previas). Al reiniciar la app con el código nuevo, Hibernate agrega las columnas solo.
- **Pruebas** (instancia aislada :8081): con 121 archivos → `total=121, totalVerificar=80, procesadosVerificar=80, duplicados=40` exacto; con 2.250 archivos → `totalVerificar=1500, duplicados=750` ✓. `mvnw clean package` verde (6 tests) y `.jar` regenerado.

## 2026-08-01 — Detectar un formato específico (búsqueda personalizada)
- **Nueva tarjeta** en el inicio: "Detectar un formato específico" → abre un **formulario con checkboxes** de formatos agrupados por categoría (Imágenes, Videos, Documentos, Sonido, Comprimidos, Programas, Otros). Botones "Seleccionar todas" / "Desmarcar todas" globales y un "Todas" por categoría.
- **Cómo funciona**: el escaneo solo compara archivos **de los formatos tildados** (un JPG no se compara con un PDF). Se puede elegir un solo formato (ej: solo JPG) o varios.
- **Backend**:
  - `ScanType.PERSONALIZADO` + `ScanSession.extensionesFiltradas` (string con los formatos elegidos, para mostrarlos en el resultado).
  - `ScanService.iniciarEscaneoPersonalizado(Set<String>, ruta)`: filtra por el conjunto de extensiones; el conteo/recolección ahora reciben `Set<String>` en lugar de `ScanType` (refactor de `contarArchivos`/`recolectarArchivos`).
  - `FileExtensionsConfig.categorias()` expone las categorías para el formulario y `extensionesDe(tipo)`/`tieneExtension(Set,…)` generalizan el filtro.
  - `ScanController`: `GET /scan/personalizado` (formulario), `POST /scan/personalizado/iniciar` (validación: al menos un formato; mismo guard de "ya hay un escaneo en curso"). En el resultado, el título muestra "formatos jpg, pdf…" y el botón "Escanear otra carpeta" vuelve al formulario.
- **Pruebas** (instancia aislada :8081): solo PDF+JPG → total=27 (excluye mp3/exe) ✓; solo JPG → total=14, 1 duplicado, y un PDF duplicado queda fuera ✓; sin extensiones → flash de error "Tenés que seleccionar al menos un formato." ✓; título y enlace correctos en el resultado ✓. `mvnw clean package` verde y `.jar` regenerado.
- **⚠ Migración de BD existentes**: Hibernate 7 mapea los enums Java a tipo **nativo ENUM de H2**, y `ddl-auto=update` NO amplía un ENUM ya creado. Al agregar `ScanType.PERSONALIZADO`, una BD creada con el enum viejo (4 valores) falla al insertar con error `Valor no permitido para la columna "('DOCUMENTOS','FOTOS','SONIDO','VIDEOS')"`. Fix manual en la BD (una sola vez, se ejecuta con la app levantada porque usa AUTO_SERVER):
  ```sql
  ALTER TABLE SCAN_SESSION ALTER COLUMN TIPO SET DATA TYPE ENUM('DOCUMENTOS','FOTOS','SONIDO','VIDEOS','PERSONALIZADO');
  ```
  Las instalaciones nuevas (jar) crean el ENUM ya con los 5 valores, no requieren nada. Si en el futuro se agrega otro valor a `ScanType`, `ScanStatus` o `ScanPhase`, hay que ampliar el ENUM correspondiente igual.

## 2026-08-02 — Mejoras de frontend (reporte, dashboard, filtros, vista previa)
- **Exportar reporte del escaneo**: botón "Descargar reporte (CSV)" en el resultado. Endpoint `GET /escaneo/{id}/exportar` genera un CSV con BOM UTF-8 (abre bien en Excel): columnas Grupo, Etiqueta, Nombre, Ruta, Tamaño (MB), Hash. Solo aparece si hay grupos.
- **Dashboard en el inicio**: el `index` ahora muestra un resumen (escaneos realizados, completados, archivos analizados, duplicados encontrados) y el historial de todos los escaneos en tabla (tipo, fecha `dd/MM/yyyy HH:mm`, estado con píldora de color, archivos, duplicados) con enlaces "Ver" (resultado) o "Progreso".
- **Búsqueda y filtros en resultados**: campo de texto para filtrar los grupos por nombre de archivo + select de extensión (se arma con las extensiones presentes) + contador "Mostrando X de Y grupos". Todo client-side (JS).
- **Vista previa de imágenes**: endpoint `GET /archivo/{id}` sirve el archivo si es una imagen (`jpg, jpeg, png, gif, bmp, webp, svg, ico, jfif`) con su Content-Type correcto; en los resultados, cada archivo de imagen muestra una miniatura (44 px) que al hacer clic abre un modal con la imagen grande. Sin miniaturas para videos/documentos.
- **Pruebas** (instancia aislada :8081): dashboard renderiza resumen + historial ✓; escaneo FOTOS con 2 duplicados (rojo.png/rojo_1.png, azul.jpg/azul (2).jpg) ✓; `/archivo/{id}` devuelve `200 image/png` ✓; CSV con los 2 grupos y hashes idénticos por grupo ✓; JS de filtros/miniaturas/modal servido ✓. `mvnw clean package` verde (6 tests) y `.jar` regenerado.

## 2026-08-02 — Navbar minimalista reutilizable + botón "borrar historial" de movimientos
- **Navbar reutilizable**: nuevo fragmento Thymeleaf `templates/fragments/navbar.html` (`th:fragment="navbar"`) insertado en las 6 plantillas (`index`, `scan`, `scan-personalizado`, `progreso`, `resultado`, `movimientos`) con `th:insert`. Links: Inicio y Historial de movimientos, con clase `activo` según la ruta.
- **`config/VistaAdvice`**: `@ControllerAdvice` que expone `rutaActual` (el requestURI) como atributo de modelo en todas las vistas. Es necesario porque **Thymeleaf 3.1 ya no expone `#request` por defecto** en las expresiones de plantilla (antes usaba `#request.requestURI`, lanzaba `EL1011E ... on null context object`). En el fragmento se usa `rutaActual == '/'` y `rutaActual.startsWith('/movimientos')`.
- **CSS del navbar** en `style.css`: sticky con blur, links con hover, link activo violeta. Se quitaron los "← Volver al inicio" del hero de todas las páginas (ya lo cubre el navbar).
- **Botón "Borrar historial" de movimientos** (`POST /movimientos/borrar`): solo borra los registros `MOVE_RECORD` de la BD (`MoveService.borrarHistorial()`), con `confirm` JS. No toca los archivos físicos ni el journal `registro/movimientos.jsonl` (decisión del usuario).
- **Pruebas** (instancia aislada :8081, BD temporal): navbar presente en index/scan/personalizado/movimientos con link activo correcto ✓; botón borrar historial visible solo con registros, con confirm y action correctos ✓; POST devuelve 302 y la BD queda en 0 registros, la página muestra el aviso vacío y el botón desaparece ✓. `mvnw -q compile` OK.

## 2026-08-02 — Efecto StarBorder (borde de estrellas) adaptado a CSS puro
- El usuario compartió el componente React `StarBorder` de reactbits.dev (https://www.reactbits.dev/animations/star-border). Como LimpiaMedia es **Thymeleaf (no React)**, no se importó el JSX: se replicó el efecto 1:1 con **HTML + CSS puro**, que es donde vive toda la magia.
- **Marcado**: en `index.html` cada tarjeta quedó envuelta en `<div class="star-border" style="--sb-color:#...">` con dos divs `<div class="border-gradient-bottom">` / `<div class="border-gradient-top">` antes del `<a class="tarjeta">`.
- **CSS** en `style.css`:
  - `.star-border`: contenedor relativo con `overflow:hidden`, border-radius 14px, `--sb-color` y `--sb-speed` (variables CSS), shadow y hover `translateY(-4px)` (el transform se movió del `.tarjeta` al contenedor para que `overflow:hidden` no recorte el efecto).
  - `.border-gradient-bottom/.top`: absolutos, `width:300%; height:50%`, `border-radius:50%`, `background: radial-gradient(circle, var(--sb-color), transparent 10%)`, animación infinita alternate con `@keyframes star-movement-bottom` / `star-movement-top` (idénticos a los de reactbits: translate 0%→±100% con fade de opacidad).
  - Colores por tarjeta: fotos `#0ea5e9`, videos `#f43f5e`, documentos `#f59e0b`, sonido `#10b981`, personalizado `#8b5cf6`.
- **Pruebas**: home renderiza 5 contenedores `star-border` con sus gradientes y las variables de color ✓; CSS con keyframes y clases servido ✓. `mvnw -q compile` OK.

## 2026-08-02 — Botón "Borrar historial de escaneos" en la home
- **`POST /escaneos/borrar`**: `HomeController.borrarHistorialEscaneos()` llama a `ScanService.borrarHistorialEscaneos()`, que borra en **orden de FK**: `ScannedFile` → `DuplicateGroup` → `ScanSession` (`deleteAll` de cada repositorio).
- Botón rojo "Borrar historial de escaneos" en la sección "Historial de escaneos" del dashboard (`index.html`), visible solo si hay sesiones, con `confirm` JS y flash de confirmación (`ok`). CSS: `.btn-peligro` (reutilizado) y `.historial-titulo` ahora es flex para alinear el botón a la derecha.
- **No toca**: los archivos físicos, `MOVE_RECORD` (historial de movimientos) ni el journal.
- **Pruebas** (instancia aislada :8081, BD temporal): botón visible solo con sesiones ✓; insertada sesión de prueba → POST 302, tablas `SCAN_SESSION`/`SCANNED_FILE`/`DUPLICATE_GROUP` en 0 y `MOVE_RECORD` intacta ✓; home recargada sin botón, con aviso vacío y flash "Historial de escaneos borrado (N sesiones)" ✓.

## 2026-08-02 — Logo en el navbar
- El usuario creó `static/img/navbar.png` (logo que ya incluye el nombre de la app).
- En `fragments/navbar.html` se reemplazó el `<span class="navbar-marca">` + `<span class="navbar-nombre">LimpiaMedia</span>` por `<img class="navbar-logo-img" th:src="@{/img/navbar.png}" alt="LimpiaMedia">` (sin texto, la imagen ya trae el nombre).
- CSS: `.navbar-logo-img` con `height:34px; width:auto; display:block`. Se eliminaron `.navbar-marca` y `.navbar-nombre`.
- **Pruebas** (instancia aislada :8081): navbar renderiza `/img/navbar.png` y ya no muestra "navbar-nombre" ✓; `GET /img/navbar.png` → `200 image/png` ✓.
- **Pendiente**: empaquetar con **jpackage** (Plan B) para distribuir el instalador de escritorio.
