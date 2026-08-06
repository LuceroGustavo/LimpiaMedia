# PLAN — Módulo "Buscar comparativas"

> Archivo de desarrollo del módulo (creado el 2026-08-05). Si no se termina hoy,
> este archivo permite retomarlo. Es un buscador de archivos por **patrón de nombre**
> para encontrar/reordenar archivos "del mismo tema" que viven en varias carpetas.

## Objetivo

Permitir buscar archivos por nombre con comodines (`*` y `?`) sobre una carpeta/disco
elegido, listar **todos** los que coinciden y poder **ordenarlos** por fecha de
modificación/creación para identificar cuál es el **más actualizado**. Caso típico:
muchos archivos iguales/del mismo tema repartidos en varias carpetas del trabajo.

## Sintaxis del patrón

| Ejemplo                     | Significado                                                    |
|-----------------------------|----------------------------------------------------------------|
| `*.xlsx`                    | todos los archivos xlsx                                        |
| `*avances*`                 | cualquier archivo cuyo nombre contenga "avances"               |
| `avab*.xlsx`                | nombres que empiezan con "avab" y terminan en .xlsx            |
| `avances del sistema.xlsx`  | sin comodines → búsqueda por "contiene" (case-insensitive)     |

Reglas:
- Comodines: `*` = cualquier secuencia de caracteres, `?` = un solo carácter.
- **Case-insensitive**.
- Con comodín → el patrón debe calzar el **nombre completo**; sin comodín → "contiene".
- Se matchea contra el **NOMBRE** del archivo (no la ruta).

## Atributos que lee por archivo

| Atributo                 | Fuente Java                             | Notas                                           |
|--------------------------|-----------------------------------------|-------------------------------------------------|
| Nombre                   | `file.getFileName()`                    |                                                 |
| Ruta completa            | `file.toString()`                       |                                                 |
| Carpeta contenedora      | `file.getParent()`                      |                                                 |
| Extensión                | `FileExtensionsConfig.extension()`      |                                                 |
| Tamaño (bytes)           | `attrs.size()` (BasicFileAttributes)    | Se muestra en MB                                |
| Fecha creación           | `attrs.creationTime()`                  |                                                 |
| Fecha última modificación| `attrs.lastModifiedTime()`              | **Clave para "cuál es el más actualizado"**     |
| Fecha último acceso      | `attrs.lastAccessTime()`                | En algunos FS de red puede no estar actualizada |
| Propietario / usuario    | `Files.getOwner()`                      | Formato `DOMINIO\usuario` en Windows            |

## Backend

- `dto/FileComparativaDTO`: nombre, ruta, carpetaPadre, extension, tamanio,
  fechaCreacion/fechaModificacion/fechaAcceso (LocalDateTime) + getters `*Epoch()`
  (millis para ordenar en el cliente).
- `service/ComparativaService.buscar(ruta, patron)`: `Files.walkFileTree` recursivo,
  filtra por nombre con regex (patrón → regex), lee atributos, devuelve lista.
  Ignora carpetas sin permisos.
- `controller/ComparativaController`:
  - `GET /comparativas` → formulario de búsqueda.
  - `POST /comparativas/buscar` → valida (ruta existe + patrón no vacío) → redirect a
    `GET /comparativas/resultado?ruta&patron`.
  - `GET /comparativas/resultado` → ejecuta la búsqueda y renderiza la tabla.
  - `GET /comparativas/abrir-carpeta?ruta&volver&patron` → abre en el Explorador la
    carpeta contenedora del archivo y vuelve al resultado.

## Frontend

- Plantilla `templates/comparativas.html`: navbar + formulario (selector de carpeta
  reutilizado + campo de patrón) + resumen + tabla dinámica ordenable.
- JS `static/js/comparativas.js`:
  - Configura el modal compartido (`carpetas.js`).
  - Orden por clic en los encabezados (nombre, tamaño, fechas, propietario…).
  - Filtro de texto sobre la tabla.
  - Checkbox **"resaltar el más actualizado"**: pinta la fila con fecha de
    modificación mayor.
  - Orden inicial: modificación descendente.
- Navbar: link "Comparativas" (activo en `/comparativas*`).

## Fases / checklist

- [x] Plan (este archivo)
- [x] DTO `FileComparativaDTO`
- [x] `ComparativaService` (patrón → regex, walk, atributos)
- [x] `ComparativaController` (formulario, búsqueda, abrir carpeta)
- [x] Plantilla `comparativas.html` + JS (`comparativas.js`)
- [x] Navbar + CSS
- [x] Compilar + `node --check` + `mvnw test`
- [x] Tests unitarios de patrones (`ComparativaServicePatronTest`, 6 casos)
- [x] Probar end-to-end con `*.xlsx`, `*avances*`, `avab*.xlsx` (local y red)
- [x] Documentar en `AVANCES.md`
- [x] Mejoras de vista + filtros selectivos (ancho 1600px, fuente resumen, columna Ext., filtro por extensión y rango de fechas) — 2026-08-06

## Pendientes / ideas futuras

- Búsqueda **asíncrona** con barra de progreso (hoy es síncrona; en discos/red muy
  grandes puede tardar).
- Agrupar por "nombre base" para ver versiones del mismo documento lado a lado.
- Selección múltiple + mover/copiar los "viejos" a una carpeta de respaldo
  (reordenamiento real).
- Límite de resultados configurable (hoy sin tope).
