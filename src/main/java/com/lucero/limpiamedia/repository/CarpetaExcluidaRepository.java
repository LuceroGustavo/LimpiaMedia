package com.lucero.limpiamedia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lucero.limpiamedia.model.CarpetaExcluida;

public interface CarpetaExcluidaRepository extends JpaRepository<CarpetaExcluida, Long> {

	List<CarpetaExcluida> findByRutaRaiz(String rutaRaiz);

	boolean existsByRutaRaizAndRutaExcluida(String rutaRaiz, String rutaExcluida);
}
