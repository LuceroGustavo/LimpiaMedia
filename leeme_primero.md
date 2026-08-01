# LEEME PRIMERO — LimpiaMedia

> **Este archivo es el punto de entrada para cualquier IA o desarrollador que retome este proyecto.**
> Leer antes de tocar código. Actualizar cada vez que haya un avance relevante.

## Qué es

Software **100% local** (escritorio/web) para **detectar archivos duplicados** en una carpeta o disco
elegido por el usuario. Categorías soportadas: **fotos, videos, documentos y sonido**.

- **NO elimina nada.** Solo detecta y, si el usuario quiere, mueve los duplicados a una carpeta destino.
- Corre en el navegador apuntando a `http://localhost:8080` sin servidor externo (Tomcat embebido).

## Stack y herramientas

| Capa | Tecnología |
|------|------------|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.1.0 (Maven) |
| Build | Maven |
| Motor de vistas | Thymeleaf |
| Base de datos | H2 embebida (archivo local) |
| Persistencia | Spring Data JPA |
| UI | HTML + CSS + jsTree (árbol) + JS vanilla |

## Cómo correr

```bash
mvn spring-boot:run
# abrir http://localhost:8080
```

Consola H2 (solo dev): `http://localhost:8080/h2-console`
URL JDBC: `jdbc:h2:file:./data/limpiamedia` · user: `sa` · password: (vacía, ver config)

## Estructura del proyecto

```
LimpiaMedia/
├── leeme_primero.md          ← ESTE archivo (contexto para IA)
├── documentacion/
│   ├── PLAN.md               ← plan, fases, objetivos
│   ├── AVANCES.md            ← bitácora (fecha + qué se hizo)
│   └── HISTORIAL.md          ← versiones y decisiones
├── pom.xml
└── src/
    ├── main/java/com/lucero/limpiamedia/
    │   ├── LimpiaMediaApplication.java
    │   ├── config/           ← configuración (ext, H2, async)
    │   ├── controller/       ← Home, Scan, Folder, File
    │   ├── service/          ← FileSystem, Scan, DuplicateDetector, Hash, Move
    │   ├── model/            ← entidades JPA
    │   ├── repository/       ← repositorios JPA
    │   └── dto/              ← DTOs (JSON para el árbol, etc.)
    └── main/resources/
        ├── application.properties
        ├── templates/        ← index, scan, tree, move
        └── static/           ← css, js, imágenes
```

## Flujo de la aplicación

1. `/` → pantalla con 4 tarjetas: **Fotos · Videos · Documentos · Sonido**.
2. Click en una tarjeta → pantalla para elegir carpeta/disco:
   - Navegador de unidades (C:, D:…) + árbol de carpetas navegable.
   - Campo para pegar la ruta directa (ej: `D:\Fotos`).
3. Escaneo **asíncrono** con barra de progreso (no congela la pantalla en discos grandes).
4. Detección de duplicados:
   - **1er filtro**: agrupar por `nombre + tamaño` (rápido).
   - **Confirmación**: a los candidatos se les calcula hash **SHA-256** → solo duplicados reales.
5. Resultado en **árbol** (jsTree): estructura de carpetas con duplicados marcados/agrupados.
6. Botón **"Mover duplicados a carpeta"** → elegir destino (sugerido: `Duplicados/`) → mueve.
   - Nunca pisa archivos: si existe el nombre en destino, renombra con sufijo `_1`, `_2`.
   - Valida que el destino no esté dentro de la carpeta escaneada.

## Extensiones soportadas por categoría (config en clase `config/FileExtensionsConfig`)

- **FOTOS**: jpg, jpeg, png, gif, bmp, webp, tiff, heic, raw, cr2, nef
- **VIDEOS**: mp4, avi, mkv, mov, wmv, flv, webm, m4v, mpeg
- **DOCUMENTOS**: pdf, doc, docx, xls, xlsx, ppt, pptx, txt, rtf, csv, odt, ods
- **SONIDO**: mp3, wav, flac, aac, ogg, m4a, wma

## Decisiones tomadas

- Base de datos **H2 embebida** (portable, sin instalación) para guardar historial de escaneos.
- Selección de carpeta por **navegador de carpetas** (unidades + árbol) y campo de ruta.
- Duplicados: **nombre + tamaño primero, hash SHA-256 para confirmar**.
- **Nunca se elimina** un archivo; solo movimiento reversible.
- Escaneo asíncrono con progreso para soportar discos grandes.

## Estado actual

> **Fase 2 — Completada.** Esqueleto funcional: 4 tarjetas, controladores, entidades JPA y
> repositorios. La app levanta en `localhost:8080` y H2 crea las tablas.
> **Siguiente:** Fase 3 (navegación de carpetas). Ver `documentacion/AVANCES.md`.

## Reglas del proyecto

- Sin comentarios innecesarios en código.
- Escritura en español (UI y documentación).
- Todo avance se registra en `documentacion/AVANCES.md`.
- No se elimina información del usuario nunca.
