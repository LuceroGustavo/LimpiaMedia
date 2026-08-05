package com.lucero.limpiamedia.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class FileComparativaDTO {

	private String nombre;
	private String ruta;
	private String carpetaPadre;
	private String extension;
	private long tamanio;
	private LocalDateTime fechaCreacion;
	private LocalDateTime fechaModificacion;
	private LocalDateTime fechaAcceso;
	private String propietario;

	public long getFechaCreacionEpoch() {
		return fechaCreacion == null ? 0 : fechaCreacion.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}

	public long getFechaModificacionEpoch() {
		return fechaModificacion == null ? 0
				: fechaModificacion.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}

	public long getFechaAccesoEpoch() {
		return fechaAcceso == null ? 0 : fechaAcceso.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
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

	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(LocalDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public LocalDateTime getFechaModificacion() {
		return fechaModificacion;
	}

	public void setFechaModificacion(LocalDateTime fechaModificacion) {
		this.fechaModificacion = fechaModificacion;
	}

	public LocalDateTime getFechaAcceso() {
		return fechaAcceso;
	}

	public void setFechaAcceso(LocalDateTime fechaAcceso) {
		this.fechaAcceso = fechaAcceso;
	}

	public String getPropietario() {
		return propietario;
	}

	public void setPropietario(String propietario) {
		this.propietario = propietario;
	}
}
