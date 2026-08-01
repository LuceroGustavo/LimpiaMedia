package com.lucero.limpiamedia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

class ScanServiceNombreTest {

	@Test
	void quitaSufijoDeNumeroDeCopiaSoloSiExisteElOriginal() {
		Set<String> presentes = Set.of("VID_20260105_161551289.mp4", "VID_20260105_161551289_1.mp4",
				"VID_20260105_161551289_2.mp4");
		assertEquals("VID_20260105_161551289.mp4",
				ScanService.normalizarNombre("VID_20260105_161551289.mp4", presentes));
		assertEquals("VID_20260105_161551289.mp4",
				ScanService.normalizarNombre("VID_20260105_161551289_1.mp4", presentes));
		assertEquals("VID_20260105_161551289.mp4",
				ScanService.normalizarNombre("VID_20260105_161551289_2.mp4", presentes));

		Set<String> fotos = Set.of("foto.jpg", "foto_1.jpg", "foto_2.jpg");
		assertEquals("foto.jpg", ScanService.normalizarNombre("foto.jpg", fotos));
		assertEquals("foto.jpg", ScanService.normalizarNombre("foto_1.jpg", fotos));
		assertEquals("foto.jpg", ScanService.normalizarNombre("foto_2.jpg", fotos));
	}

	@Test
	void quitaPrefijoDeCopia() {
		Set<String> presentes = Set.of("foto.jpg", "copia de foto.jpg", "Copy of foto.jpg", "copia_foto.jpg");
		assertEquals("foto.jpg", ScanService.normalizarNombre("copia de foto.jpg", presentes));
		assertEquals("foto.jpg", ScanService.normalizarNombre("Copy of foto.jpg", presentes));
		assertEquals("foto.jpg", ScanService.normalizarNombre("copia_foto.jpg", presentes));
	}

	@Test
	void quitaSufijoDeCopiaDeWindows() {
		Set<String> presentes = Set.of("foto.jpg", "foto - copia.jpg", "foto - Copy.jpg", "foto - Copy (2).jpg",
				"foto (2).jpg");
		assertEquals("foto.jpg", ScanService.normalizarNombre("foto - copia.jpg", presentes));
		assertEquals("foto.jpg", ScanService.normalizarNombre("foto - Copy.jpg", presentes));
		assertEquals("foto.jpg", ScanService.normalizarNombre("foto - Copy (2).jpg", presentes));
		assertEquals("foto.jpg", ScanService.normalizarNombre("foto (2).jpg", presentes));
	}

	@Test
	void quitaMarcadoresRepetidos() {
		Set<String> presentes = Set.of("foto.jpg", "foto_1.jpg", "foto_1_1.jpg");
		assertEquals("foto.jpg", ScanService.normalizarNombre("foto_1_1.jpg", presentes));

		Set<String> dobles = Set.of("foto.jpg", "copia de copia de foto.jpg");
		assertEquals("foto.jpg", ScanService.normalizarNombre("copia de copia de foto.jpg", dobles));
	}

	@Test
	void noTocaTimestampsNiNombresSinMarcador() {
		Set<String> solitario = Set.of("VID_20260105_161551289.mp4");
		assertEquals("VID_20260105_161551289.mp4",
				ScanService.normalizarNombre("VID_20260105_161551289.mp4", solitario));

		Set<String> solitarios = Set.of("reporte_2025.pdf");
		assertEquals("reporte_2025.pdf", ScanService.normalizarNombre("reporte_2025.pdf", solitarios));

		Set<String> distintos = Set.of("reporte_2025.pdf", "reporte_2026.pdf");
		assertEquals("reporte_2025.pdf", ScanService.normalizarNombre("reporte_2025.pdf", distintos));
		assertEquals("reporte_2026.pdf", ScanService.normalizarNombre("reporte_2026.pdf", distintos));

		Set<String> unicos = Set.of("foto.jpg", "a.b.jpg");
		assertEquals("foto.jpg", ScanService.normalizarNombre("foto.jpg", unicos));
		assertEquals("a.b.jpg", ScanService.normalizarNombre("a.b.jpg", unicos));
	}
}
