package com.lucero.limpiamedia.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class DuplicateGroup {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	private ScanSession sesion;

	private String clave;
	private String hash;
	private long tamanio;

	@OneToMany(mappedBy = "grupo")
	private List<ScannedFile> archivos = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ScanSession getSesion() {
		return sesion;
	}

	public void setSesion(ScanSession sesion) {
		this.sesion = sesion;
	}

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public long getTamanio() {
		return tamanio;
	}

	public void setTamanio(long tamanio) {
		this.tamanio = tamanio;
	}

	public List<ScannedFile> getArchivos() {
		return archivos;
	}

	public void setArchivos(List<ScannedFile> archivos) {
		this.archivos = archivos;
	}
}
