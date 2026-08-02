package com.lucero.limpiamedia.config;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class VistaAdvice {

	@ModelAttribute("rutaActual")
	public String rutaActual(HttpServletRequest request) {
		return request.getRequestURI();
	}
}
