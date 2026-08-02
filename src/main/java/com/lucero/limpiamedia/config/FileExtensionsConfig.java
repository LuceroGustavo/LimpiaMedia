package com.lucero.limpiamedia.config;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.lucero.limpiamedia.model.ScanType;

@Component
public class FileExtensionsConfig {

	private final Map<ScanType, Set<String>> extensiones = new EnumMap<>(ScanType.class);

	public FileExtensionsConfig() {
		extensiones.put(ScanType.FOTOS, Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "tiff", "heic",
				"raw", "cr2", "nef", "jfif", "svg"));
		extensiones.put(ScanType.VIDEOS, Set.of("mp4", "avi", "mkv", "mov", "wmv", "flv", "webm", "m4v",
				"mpeg", "mpg", "3gp"));
		extensiones.put(ScanType.DOCUMENTOS, Set.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
				"txt", "rtf", "csv", "odt", "ods"));
		extensiones.put(ScanType.SONIDO, Set.of("mp3", "wav", "flac", "aac", "ogg", "m4a", "wma"));
	}

	public boolean tieneExtension(ScanType tipo, String nombreArchivo) {
		return tieneExtension(extensionesDe(tipo), nombreArchivo);
	}

	public boolean tieneExtension(Set<String> extensionesBuscar, String nombreArchivo) {
		int i = nombreArchivo.lastIndexOf('.');
		if (i < 0) {
			return false;
		}
		String ext = nombreArchivo.substring(i + 1).toLowerCase(Locale.ROOT);
		return extensionesBuscar.contains(ext);
	}

	public Set<String> extensionesDe(ScanType tipo) {
		return extensiones.getOrDefault(tipo, Set.of());
	}

	/**
	 * Categorías con sus extensiones para el formulario de búsqueda personalizada,
	 * en orden de presentación.
	 */
	public Map<String, Set<String>> categorias() {
		Map<String, Set<String>> categorias = new LinkedHashMap<>();
		categorias.put("Imágenes", Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp", "tiff", "heic",
				"raw", "cr2", "nef", "jfif", "svg", "ico", "psd", "ai", "eps"));
		categorias.put("Videos", Set.of("mp4", "avi", "mkv", "mov", "wmv", "flv", "webm", "m4v",
				"mpeg", "mpg", "3gp", "mts", "m2ts"));
		categorias.put("Documentos", Set.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
				"txt", "rtf", "csv", "odt", "ods", "odp", "epub", "md", "xml", "json", "html"));
		categorias.put("Sonido", Set.of("mp3", "wav", "flac", "aac", "ogg", "m4a", "wma", "opus"));
		categorias.put("Comprimidos", Set.of("zip", "rar", "7z", "tar", "gz", "bz2", "iso"));
		categorias.put("Programas", Set.of("exe", "msi", "bat", "cmd", "sh", "apk", "jar"));
		categorias.put("Otros", Set.of("dll", "bin", "dat", "db", "log", "tmp", "bak"));
		return categorias;
	}

	public static String extension(String nombreArchivo) {
		int i = nombreArchivo.lastIndexOf('.');
		return i < 0 ? "" : nombreArchivo.substring(i + 1).toLowerCase(Locale.ROOT);
	}
}
