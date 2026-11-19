package com.empresa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.empresa.entity.Reclamo;

public interface ReclamoRepository extends JpaRepository<Reclamo, Integer> {

}
