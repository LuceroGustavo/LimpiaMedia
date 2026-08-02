package com.lucero.limpiamedia.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lucero.limpiamedia.model.CarpetaExcluida;
import com.lucero.limpiamedia.model.MoveRecord;
import com.lucero.limpiamedia.model.ScanSession;
import com.lucero.limpiamedia.model.ScannedFile;
import com.lucero.limpiamedia.repository.CarpetaExcluidaRepository;
import com.lucero.limpiamedia.repository.MoveRecordRepository;
import com.lucero.limpiamedia.repository.ScanSessionRepository;
import com.lucero.limpiamedia.repository.ScannedFileRepository;

@Service
public class MoveService {

	private static final Logger log = LoggerFactory.getLogger(MoveService.class);
	private static final Path ARCHIVO_REGISTRO = Paths.get("registro", "movimientos.jsonl");

	private final ScanSessionRepository sessionRepo;
	private final ScannedFileRepository fileRepo;
	private final MoveRecordRepository moveRepo;
	private final CarpetaExcluidaRepository exclRepo;

	public MoveService(ScanSessionRepository sessionRepo, ScannedFileRepository fileRepo,
			MoveRecordRepository moveRepo, CarpetaExcluidaRepository exclRepo) {
		this.sessionRepo = sessionRepo;
		this.fileRepo = fileRepo;
		this.moveRepo = moveRepo;
		this.exclRepo = exclRepo;
	}

	public int moverDuplicados(Long sesionId, String carpetaDestino) throws IOException {
		ScanSession sesion = sessionRepo.findById(sesionId).orElseThrow(
				() -> new IllegalArgumentException("Sesión de escaneo no encontrada"));
		if (carpetaDestino == null || carpetaDestino.isBlank()) {
			throw new IllegalArgumentException("Indicá una carpeta destino");
		}

		Path destino = Paths.get(carpetaDestino.trim()).toAbsolutePath().normalize();
		Files.createDirectories(destino);

		registrarExclusion(sesion, destino);

		List<ScannedFile> duplicados = fileRepo.findBySesionIdAndEsDuplicadoTrue(sesionId);
		int movidos = 0;
		for (ScannedFile f : duplicados) {
			Path origen = Paths.get(f.getRuta());
			if (!Files.exists(origen)) {
				continue;
			}
			Path nuevo = resolverDestino(destino, f.getNombre());
			Files.move(origen, nuevo);
			f.setMovido(true);
			fileRepo.save(f);

			MoveRecord rec = new MoveRecord();
			rec.setSesionId(sesionId);
			rec.setArchivoId(f.getId());
			rec.setTipo(sesion.getTipo());
			rec.setArchivoOriginal(f.getRuta());
			rec.setArchivoNuevo(nuevo.toString());
			rec.setFecha(LocalDateTime.now());
			rec.setRestaurado(false);
			moveRepo.save(rec);

			appendAlRegistro(rec, "mover");
			movidos++;
		}

		sesion.setCarpetaDestino(destino.toString());
		sessionRepo.save(sesion);
		log.info("Se movieron {} duplicados de la sesion {} a {}", movidos, sesionId, destino);
		return movidos;
	}

	public int borrarHistorial() {
		long antes = moveRepo.count();
		moveRepo.deleteAll();
		log.info("Historial de movimientos borrado ({} registros)", antes);
		return (int) antes;
	}

	public void restaurar(Long moveId) throws IOException {
		MoveRecord rec = moveRepo.findById(moveId).orElseThrow(
				() -> new IllegalArgumentException("Movimiento no encontrado"));
		if (rec.isRestaurado()) {
			throw new IllegalArgumentException("Este archivo ya fue restaurado");
		}
		Path nuevo = Paths.get(rec.getArchivoNuevo());
		if (!Files.exists(nuevo)) {
			throw new IllegalArgumentException("El archivo ya no existe en su destino: " + nuevo);
		}
		Path original = Paths.get(rec.getArchivoOriginal());
		Path destino = original;
		if (Files.exists(original)) {
			destino = resolverDestino(original.getParent(), original.getFileName().toString());
		}
		Files.move(nuevo, destino);

		rec.setRestaurado(true);
		moveRepo.save(rec);

		fileRepo.findById(rec.getArchivoId()).ifPresent(f -> {
			f.setMovido(false);
			fileRepo.save(f);
		});

		appendAlRegistro(rec, "restaurar");
		log.info("Restaurado {} hacia {}", rec.getArchivoNuevo(), destino);
	}

	private void registrarExclusion(ScanSession sesion, Path destino) {
		String raiz = normalizar(sesion.getRutaRaiz());
		String excluida = destino.toString();
		if (!exclRepo.existsByRutaRaizAndRutaExcluida(raiz, excluida)) {
			CarpetaExcluida ce = new CarpetaExcluida();
			ce.setRutaRaiz(raiz);
			ce.setRutaExcluida(excluida);
			exclRepo.save(ce);
		}
	}

	private Path resolverDestino(Path carpeta, String nombre) {
		Path p = carpeta.resolve(nombre);
		if (!Files.exists(p)) {
			return p;
		}
		int dot = nombre.lastIndexOf('.');
		String base = dot < 0 ? nombre : nombre.substring(0, dot);
		String ext = dot < 0 ? "" : nombre.substring(dot);
		int i = 1;
		while (Files.exists(carpeta.resolve(base + "_" + i + ext))) {
			i++;
		}
		return carpeta.resolve(base + "_" + i + ext);
	}

	private void appendAlRegistro(MoveRecord rec, String accion) {
		try {
			String linea = "{\"accion\":\"" + accion
					+ "\",\"fecha\":\"" + rec.getFecha()
					+ "\",\"tipo\":\"" + rec.getTipo().name()
					+ "\",\"sesion\":" + rec.getSesionId()
					+ ",\"original\":\"" + esc(rec.getArchivoOriginal())
					+ "\",\"nuevo\":\"" + esc(rec.getArchivoNuevo()) + "\"}";
			Files.createDirectories(ARCHIVO_REGISTRO.getParent());
			Files.writeString(ARCHIVO_REGISTRO, linea + System.lineSeparator(),
					StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (Exception e) {
			log.warn("No se pudo escribir el registro de movimientos", e);
		}
	}

	private String esc(String valor) {
		if (valor == null) {
			return "";
		}
		return valor.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n");
	}

	private String normalizar(String ruta) {
		return Paths.get(ruta).toAbsolutePath().normalize().toString();
	}
}
