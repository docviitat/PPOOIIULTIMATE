package com.PPOOII.Laboratorio.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import com.PPOOII.Laboratorio.Entities.Trayecto;

@RepositoryRestResource(exported = false)
@Repository("ITrayectoRepository")
public interface TrayectoRepository extends JpaRepository<Trayecto, Long> {
}
