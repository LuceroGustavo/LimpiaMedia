# HISTORIAL — LimpiaMedia

Registro de versiones y decisiones del proyecto.

## v0.0.0 — 2026-08-01
**Estado:** Inicio. Sin código de aplicación todavía.
- Documentación inicial creada.
- Stack definido: Java 21, Spring Boot 4.1.0, Maven, Thymeleaf, H2, Spring Data JPA.
- Decisiones registradas en `PLAN.md` y `leeme_primero.md`.

## v0.1.0 — 2026-08-02
**Estado:** Frontend pulido + limpieza de historiales.
- Navbar minimalista reutilizable (fragmento Thymeleaf) en todas las páginas, con link activo vía `VistaAdvice` (`rutaActual`; `#request` ya no está disponible en Thymeleaf 3.1).
- Botón "Borrar historial" de movimientos (`POST /movimientos/borrar`): solo registros `MOVE_RECORD`, sin tocar archivos ni el journal JSONL.
- Efecto **StarBorder** (reactbits.dev) adaptado a CSS puro en las tarjetas de categorías de la home.
- Botón "Borrar historial de escaneos" en la home (`POST /escaneos/borrar`): borra sesiones, archivos y grupos en orden de FK; no toca archivos físicos ni el historial de movimientos.
- Logo propio del usuario (`static/img/navbar.png`) en el navbar, reemplazando la marca SVG de CSS.
- Pendiente: empaquetado con **jpackage** (Plan B).
