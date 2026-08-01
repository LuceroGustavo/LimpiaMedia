package com.lucero.limpiamedia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lucero.limpiamedia.model.MoveRecord;

public interface MoveRecordRepository extends JpaRepository<MoveRecord, Long> {

	List<MoveRecord> findAllByOrderByFechaDesc();
}
