package com.PPOOII.Laboratorio.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.PPOOII.Laboratorio.Entities.Coordenadas;

@Repository("ICoordenadasRepository")
public interface CoordenadasRepository extends JpaRepository<Coordenadas, Integer>, CrudRepository<Coordenadas, Integer>{
	//Hay Métodos que JPA ya los tiene desarrollados, se pueden crear para tener
	//una manipulación más especifica a la hora de usarlos en el service	

	public abstract Page<Coordenadas> findAll(Pageable pageable);
	
	@Query("SELECT coord FROM COOR coord WHERE coord.persona = :id_persona ")
	public abstract Coordenadas getCoordenadaXPersona(@Param("id_persona") int persona);
}

