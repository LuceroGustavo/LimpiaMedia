package com.lucero.limpiamedia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lucero.limpiamedia.model.DuplicateGroup;

public interface DuplicateGroupRepository extends JpaRepository<DuplicateGroup, Long> {

	List<DuplicateGroup> findBySesion_IdOrderById(Long sesionId);
}
