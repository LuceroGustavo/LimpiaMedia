package com.lucero.limpiamedia.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class CarpetaExcluida {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String rutaRaiz;
	private String rutaExcluida;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRutaRaiz() {
		return rutaRaiz;
	}

	public void setRutaRaiz(String rutaRaiz) {
		this.rutaRaiz = rutaRaiz;
	}

	public String getRutaExcluida() {
		return rutaExcluida;
	}

	public void setRutaExcluida(String rutaExcluida) {
		this.rutaExcluida = rutaExcluida;
	}
}
