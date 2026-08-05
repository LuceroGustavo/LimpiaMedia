package com.lucero.limpiamedia.service;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lucero.limpiamedia.config.FileExtensionsConfig;
import com.lucero.limpiamedia.dto.FileComparativaDTO;

@Service
public class ComparativaService {

	private static final Logger log = LoggerFactory.getLogger(ComparativaService.class);

	public List<FileComparativaDTO> buscar(String rutaRaiz, String patron) {
		Path raiz = Paths.get(rutaRaiz);
		if (!Files.isDirectory(raiz)) {
			throw new IllegalArgumentException("La ruta no es una carpeta válida: " + rutaRaiz);
		}
		Pattern regex = compilarPatron(patron);
		List<FileComparativaDTO> resultados = new ArrayList<>();
		try {
			Files.walkFileTree(raiz, new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					String nombre = file.getFileName().toString();
					if (regex.matcher(nombre).find()) {
						resultados.add(convertir(file, attrs));
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) {
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			log.warn("No se pudo completar la búsqueda en {}: {}", rutaRaiz, e.getMessage());
		}
		return resultados;
	}

	private FileComparativaDTO convertir(Path file, BasicFileAttributes attrs) {
		String nombre = file.getFileName().toString();
		Path padre = file.getParent();
		FileComparativaDTO dto = new FileComparativaDTO();
		dto.setNombre(nombre);
		dto.setRuta(file.toString());
		dto.setCarpetaPadre(padre == null ? "" : padre.toString());
		dto.setExtension(FileExtensionsConfig.extension(nombre));
		dto.setTamanio(attrs.size());
		dto.setFechaCreacion(convertir(attrs.creationTime()));
		dto.setFechaModificacion(convertir(attrs.lastModifiedTime()));
		dto.setFechaAcceso(convertir(attrs.lastAccessTime()));
		dto.setPropietario(propietario(file));
		return dto;
	}

	private LocalDateTime convertir(FileTime tiempo) {
		if (tiempo == null || tiempo.toMillis() == 0) {
			return null;
		}
		return LocalDateTime.ofInstant(tiempo.toInstant(), ZoneId.systemDefault());
	}

	private String propietario(Path file) {
		try {
			return Files.getOwner(file).getName();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Traduce el patrón del usuario a una regex.
	 * - Con comodines (* y ?) se exige que el nombre completo calce.
	 * - Sin comodines se busca "contiene".
	 * Siempre case-insensitive.
	 */
	static Pattern compilarPatron(String patron) {
		String p = patron == null ? "" : patron.trim();
		if (p.isEmpty()) {
			throw new IllegalArgumentException("Indicá un patrón de búsqueda (ej: *.xlsx, *avances*).");
		}
		boolean conComodin = p.indexOf('*') >= 0 || p.indexOf('?') >= 0;
		StringBuilder sb = new StringBuilder("(?i)");
		if (conComodin) {
			sb.append('^');
		} else {
			sb.append(".*");
		}
		for (char c : p.toCharArray()) {
			switch (c) {
				case '*' -> sb.append(".*");
				case '?' -> sb.append('.');
				case '.', '\\', '(', ')', '[', ']', '{', '}', '+', '^', '$', '|' -> sb.append('\\').append(c);
				default -> sb.append(c);
			}
		}
		if (conComodin) {
			sb.append('$');
		} else {
			sb.append(".*");
		}
		return Pattern.compile(sb.toString());
	}
}
