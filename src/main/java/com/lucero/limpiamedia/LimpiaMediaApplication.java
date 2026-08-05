package com.lucero.limpiamedia;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class LimpiaMediaApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(LimpiaMediaApplication.class);

	private static FileLock bloqueo;

	public static void main(String[] args) {
		if (!adquirirInstanciaUnica()) {
			LOGGER.warn("LimpiaMedia ya se está ejecutando. Reabriendo navegador y saliendo.");
			abrirNavegador("http://localhost:" + puertoEsperado());
			return;
		}
		SpringApplication.run(LimpiaMediaApplication.class, args);
	}

	/**
	 * Evita que dos instancias corran a la vez (chocaban por el puerto y el
	 * lanzador mostraba "Failed to launch JVM"). La primera obtiene el bloqueo;
	 * las siguientes salen sin arrancar Spring.
	 */
	private static boolean adquirirInstanciaUnica() {
		FileChannel canal = null;
		try {
			Path dir = Path.of(System.getProperty("user.home"), "LimpiaMedia");
			Files.createDirectories(dir);
			Path lock = dir.resolve("limpiamedia.lock");
			canal = FileChannel.open(lock, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
			FileLock intento = canal.tryLock();
			if (intento == null) {
				canal.close();
				return false;
			}
			bloqueo = intento;
			return true;
		} catch (OverlappingFileLockException e) {
			// DevTools reinicia la app en el mismo JVM y la clase anterior ya
			// tiene el lock del mismo archivo. Se continúa sin un lock nuevo
			// (el lock real del proceso sigue activo para otras instancias).
			cerrarCanal(canal);
			LOGGER.debug("Reinicio de DevTools en el mismo JVM, se continúa sin nuevo lock");
			return true;
		} catch (IOException e) {
			LOGGER.warn("No se pudo verificar instancia única, se continúa: {}", e.getMessage());
			return true;
		}
	}

	private static void cerrarCanal(FileChannel canal) {
		if (canal != null) {
			try {
				canal.close();
			} catch (IOException ignorada) {
			}
		}
	}

	private static int puertoEsperado() {
		String puerto = System.getProperty("server.port");
		if (puerto == null) {
			puerto = System.getenv("SERVER_PORT");
		}
		if (puerto == null && "prod".equals(System.getProperty("spring.profiles.active"))) {
			puerto = "8090";
		}
		if (puerto == null) {
			puerto = "8080";
		}
		return Integer.parseInt(puerto);
	}

	private static void abrirNavegador(String url) {
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().browse(new URI(url));
			} else {
				Runtime.getRuntime().exec(new String[] { "rundll32", "url.dll,FileProtocolHandler", url });
			}
		} catch (Exception e) {
			LOGGER.warn("No se pudo abrir el navegador: {}", e.getMessage());
		}
	}

	@Component
	public static class AbrirNavegador implements ApplicationRunner {

		private static final Logger log = LoggerFactory.getLogger(AbrirNavegador.class);

		private final Environment env;

		public AbrirNavegador(Environment env) {
			this.env = env;
		}

		@Override
		public void run(ApplicationArguments args) {
			try {
				Thread.sleep(1200);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			int puerto = Integer.parseInt(env.getProperty("server.port", "8080"));
			String url = "http://localhost:" + puerto;
			log.info("Abriendo navegador en {}", url);
			abrirNavegador(url);
		}
	}

}
