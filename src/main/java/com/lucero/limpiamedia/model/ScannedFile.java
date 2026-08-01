package com.lucero.limpiamedia.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class ScannedFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	private ScanSession sesion;

	@ManyToOne
	private DuplicateGroup grupo;

	private String nombre;
	private String ruta;
	private String carpetaPadre;
	private String extension;
	private long tamanio;
	private String hash;
	private boolean esDuplicado;

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

	public DuplicateGroup getGrupo() {
		return grupo;
	}

	public void setGrupo(DuplicateGroup grupo) {
		this.grupo = grupo;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getRuta() {
		return ruta;
	}

	public void setRuta(String ruta) {
		this.ruta = ruta;
	}

	public String getCarpetaPadre() {
		return carpetaPadre;
	}

	public void setCarpetaPadre(String carpetaPadre) {
		this.carpetaPadre = carpetaPadre;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public long getTamanio() {
		return tamanio;
	}

	public void setTamanio(long tamanio) {
		this.tamanio = tamanio;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public boolean isEsDuplicado() {
		return esDuplicado;
	}

	public void setEsDuplicado(boolean esDuplicado) {
		this.esDuplicado = esDuplicado;
	}
}
