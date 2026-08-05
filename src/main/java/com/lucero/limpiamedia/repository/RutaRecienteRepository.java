package com.lucero.limpiamedia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lucero.limpiamedia.model.RutaReciente;

public interface RutaRecienteRepository extends JpaRepository<RutaReciente, Long> {

	Optional<RutaReciente> findByRuta(String ruta);

	List<RutaReciente> findTop10ByOrderByUltimaVezDesc();
}
