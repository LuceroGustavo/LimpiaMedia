package com.lucero.limpiamedia.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class ComparativaServicePatronTest {

	private boolean calza(String patron, String nombre) {
		Pattern regex = ComparativaService.compilarPatron(patron);
		return regex.matcher(nombre).find();
	}

	@Test
	void asteriscoAlFinalBuscaPorExtension() {
		assertTrue(calza("*.xlsx", "avances.xlsx"));
		assertTrue(calza("*.xlsx", "informe 2026.xlsx"));
		assertFalse(calza("*.xlsx", "avances.txt"));
		assertFalse(calza("*.xlsx", "avances.xlsx.bak"));
	}

	@Test
	void asteriscosALosCostadosBuscaContiene() {
		assertTrue(calza("*avances*", "informe avances 2026.xlsx"));
		assertTrue(calza("*avances*", "AVANCES-del-sistema.pdf"));
		assertFalse(calza("*avances*", "informe.pdf"));
	}

	@Test
	void patronConPrefijoYSufijo() {
		assertTrue(calza("avab*.xlsx", "avab1.xlsx"));
		assertTrue(calza("avab*.xlsx", "avab_final.xlsx"));
		assertFalse(calza("avab*.xlsx", "xavab1.xlsx"));
	}

	@Test
	void sinComodinesBuscaContiene() {
		assertTrue(calza("avances", "AVANCES del sistema.xlsx"));
		assertTrue(calza("avances", "informe avances 2026.pdf"));
		assertFalse(calza("avances", "informe final.pdf"));
	}

	@Test
	void interrogacionRepresentaUnCaracter() {
		assertTrue(calza("foto?.jpg", "foto1.jpg"));
		assertFalse(calza("foto?.jpg", "foto12.jpg"));
	}

	@Test
	void puntoSeTomaLiteral() {
		assertTrue(calza("avances.xlsx", "avances.xlsx"));
		assertFalse(calza("avances.xlsx", "avancesXxlsx"));
	}
}
