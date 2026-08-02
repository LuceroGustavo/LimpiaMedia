package com.lucero.limpiamedia;

import java.awt.Desktop;
import java.net.URI;

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

	public static void main(String[] args) {
		SpringApplication.run(LimpiaMediaApplication.class, args);
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
			try {
				if (Desktop.isDesktopSupported()) {
					Desktop.getDesktop().browse(new URI(url));
				} else {
					Runtime.getRuntime().exec(new String[] { "rundll32", "url.dll,FileProtocolHandler", url });
				}
			} catch (Exception e) {
				log.warn("No se pudo abrir el navegador automáticamente: {}", e.getMessage());
			}
		}
	}

}
