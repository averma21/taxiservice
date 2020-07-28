package com.amrit.taxiservice.persistence;


import com.amrit.taxiservice.model.Place;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceRepository extends Neo4jRepository<Place, Long> {

    Page<Place> findByLatitudeAndLongitude(long latitude, long longitude, Pageable pageable);

    Place findById(long id);

    Page<Place> findByName(String name, Pageable pageable);

}
