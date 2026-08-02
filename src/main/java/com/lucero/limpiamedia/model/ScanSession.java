package com.lucero.limpiamedia.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class ScanSession {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private ScanType tipo;

	private String rutaRaiz;

	private String extensionesFiltradas;

	@Enumerated(EnumType.STRING)
	private ScanStatus estado = ScanStatus.EN_PROGRESO;

	@Enumerated(EnumType.STRING)
	@Column(nullable = true)
	private ScanPhase fase = ScanPhase.CONTANDO;

	private LocalDateTime inicio;
	private LocalDateTime fin;

	private long totalArchivos;
	private long procesados;
	private long duplicados;
	private Long totalVerificar;
	private Long procesadosVerificar;

	private String carpetaDestino;

	@OneToMany(mappedBy = "sesion", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ScannedFile> archivos = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ScanType getTipo() {
		return tipo;
	}

	public void setTipo(ScanType tipo) {
		this.tipo = tipo;
	}

	public String getRutaRaiz() {
		return rutaRaiz;
	}

	public void setRutaRaiz(String rutaRaiz) {
		this.rutaRaiz = rutaRaiz;
	}

	public String getExtensionesFiltradas() {
		return extensionesFiltradas;
	}

	public void setExtensionesFiltradas(String extensionesFiltradas) {
		this.extensionesFiltradas = extensionesFiltradas;
	}

	public ScanStatus getEstado() {
		return estado;
	}

	public void setEstado(ScanStatus estado) {
		this.estado = estado;
	}

	public ScanPhase getFase() {
		return fase;
	}

	public void setFase(ScanPhase fase) {
		this.fase = fase;
	}

	public LocalDateTime getInicio() {
		return inicio;
	}

	public void setInicio(LocalDateTime inicio) {
		this.inicio = inicio;
	}

	public LocalDateTime getFin() {
		return fin;
	}

	public void setFin(LocalDateTime fin) {
		this.fin = fin;
	}

	public long getTotalArchivos() {
		return totalArchivos;
	}

	public void setTotalArchivos(long totalArchivos) {
		this.totalArchivos = totalArchivos;
	}

	public long getProcesados() {
		return procesados;
	}

	public void setProcesados(long procesados) {
		this.procesados = procesados;
	}

	public long getDuplicados() {
		return duplicados;
	}

	public void setDuplicados(long duplicados) {
		this.duplicados = duplicados;
	}

	public Long getTotalVerificar() {
		return totalVerificar;
	}

	public void setTotalVerificar(Long totalVerificar) {
		this.totalVerificar = totalVerificar;
	}

	public Long getProcesadosVerificar() {
		return procesadosVerificar;
	}

	public void setProcesadosVerificar(Long procesadosVerificar) {
		this.procesadosVerificar = procesadosVerificar;
	}

	public String getCarpetaDestino() {
		return carpetaDestino;
	}

	public void setCarpetaDestino(String carpetaDestino) {
		this.carpetaDestino = carpetaDestino;
	}

	public List<ScannedFile> getArchivos() {
		return archivos;
	}

	public void setArchivos(List<ScannedFile> archivos) {
		this.archivos = archivos;
	}
}
