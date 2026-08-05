package com.lucero.limpiamedia.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class RutaReciente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String ruta;

	@Enumerated(EnumType.STRING)
	private ScanType tipo;

	private LocalDateTime ultimaVez;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRuta() {
		return ruta;
	}

	public void setRuta(String ruta) {
		this.ruta = ruta;
	}

	public ScanType getTipo() {
		return tipo;
	}

	public void setTipo(ScanType tipo) {
		this.tipo = tipo;
	}

	public LocalDateTime getUltimaVez() {
		return ultimaVez;
	}

	public void setUltimaVez(LocalDateTime ultimaVez) {
		this.ultimaVez = ultimaVez;
	}
}
