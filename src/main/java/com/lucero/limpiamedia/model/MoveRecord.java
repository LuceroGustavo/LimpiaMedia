package com.lucero.limpiamedia.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class MoveRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long sesionId;
	private Long archivoId;

	@Enumerated(EnumType.STRING)
	private ScanType tipo;

	private String archivoOriginal;
	private String archivoNuevo;
	private LocalDateTime fecha;
	private boolean restaurado;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getSesionId() {
		return sesionId;
	}

	public void setSesionId(Long sesionId) {
		this.sesionId = sesionId;
	}

	public Long getArchivoId() {
		return archivoId;
	}

	public void setArchivoId(Long archivoId) {
		this.archivoId = archivoId;
	}

	public ScanType getTipo() {
		return tipo;
	}

	public void setTipo(ScanType tipo) {
		this.tipo = tipo;
	}

	public String getArchivoOriginal() {
		return archivoOriginal;
	}

	public void setArchivoOriginal(String archivoOriginal) {
		this.archivoOriginal = archivoOriginal;
	}

	public String getArchivoNuevo() {
		return archivoNuevo;
	}

	public void setArchivoNuevo(String archivoNuevo) {
		this.archivoNuevo = archivoNuevo;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}

	public boolean isRestaurado() {
		return restaurado;
	}

	public void setRestaurado(boolean restaurado) {
		this.restaurado = restaurado;
	}
}
