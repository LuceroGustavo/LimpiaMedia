# PLAN — LimpiaMedia

Detector local de archivos duplicados (fotos, videos, documentos, sonido).
Software 100% local: Spring Boot + Thymeleaf + H2, corre en el navegador sin servidor externo.

## Objetivos

1. Detectar archivos duplicados en una carpeta o disco elegido por el usuario.
2. Clasificar por categoría: fotos, videos, documentos, sonido.
3. Mostrar resultados en forma de árbol.
4. Mover duplicados a una carpeta destino (nunca eliminar).
5. Guardar historial de escaneos en H2.

## Fases

### Fase 1 — Estructura y documentación ✅ (en curso)
- [x] Crear `leeme_primero.md` (contexto para IA).
- [x] Crear `documentacion/` (PLAN, AVANCES, HISTORIAL).
- [ ] Generar proyecto Spring Boot en Initializr e importar al proyecto.

### Fase 2 — Esqueleto de la aplicación ✅
- [x] Configurar `application.properties` (H2, thymeleaf, puerto).
- [x] `HomeController` + `index.html` con las 4 tarjetas.
- [x] Entidades JPA: `ScanSession`, `ScannedFile`, `DuplicateGroup`.
- [x] Repositorios JPA y controladores base.
- [x] Verificado: app levanta en `localhost:8080`, H2 crea las tablas.

### Fase 3 — Navegación de carpetas ✅
- [x] `FileSystemService`: listar unidades y carpetas.
- [x] `FolderController` (JSON) para el árbol navegable.
- [x] Pantalla `scan.html`: navegador de carpetas + campo de ruta.
- [x] Pruebas: unidades (C:, D:, E:), rutas con espacios, breadcrumb y POST iniciar.

### Fase 4 — Escaneo y detección (FOTOS primero)
- [x] `ScanService`: escaneo asíncrono con progreso.
- [x] `DuplicateDetector`: filtro nombre+tamaño → hash SHA-256.
- [x] `HashService`: cálculo de hash.
- [x] Escaneo recursivo (subcarpetas incluidas).
- [x] Pantalla de progreso (polling) y resultado con grupos ORIGINAL/DUPLICADO.
- [x] Probar con FOTOS end-to-end (incluye caso "trampa" descartado por hash).
- [ ] Probar con VIDEOS, DOCUMENTOS y SONIDO.
- [ ] Mover duplicados a carpeta destino (Fase 5).

### Fase 5 — Movimiento seguro
- [ ] `MoveService`: mover duplicados a carpeta destino.
- [ ] Renombrado `_1`, `_2` para no pisar archivos.
- [ ] Validación de destino fuera de la carpeta escaneada.

### Fase 6 — Pulido
- [ ] Pruebas de punta a punta con archivos reales.
- [ ] Revisión visual de la UI.
- [ ] Actualizar documentación.

## Criterio de duplicados (confirmado)

1. **Nombre + tamaño** (filtro rápido, agrupa candidatos).
2. **Hash SHA-256** (confirma que el contenido es idéntico → sin falsos positivos).

## Reglas de seguridad

- **Nunca eliminar** archivos.
- Movimiento siempre reversible (carpeta destino propia).
- No pisar archivos existentes en destino.
