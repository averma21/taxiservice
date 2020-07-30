package com.amrit.taxiservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

import static org.neo4j.ogm.annotation.Relationship.UNDIRECTED;

@NodeEntity
public class Place {

    @Id @GeneratedValue
    private Long id;
    private String name;

    private double latitude;
    private double longitude;
    private String description;

//    public Place(@JsonProperty("name") String name, @JsonProperty("latitude") long latitude,
//                 @JsonProperty("longitude") long longitude, @JsonProperty("description") String description) {
//        this.name = name;
//        this.latitude = latitude;
//        this.longitude = longitude;
//        this.description = description;
//    }

    @Relationship(type = "connection", direction = UNDIRECTED)
    Set<Place> biDirectionalConnections = new HashSet<>();

    @Relationship(type = "connection")
    Set<Place> outgoingConnections = new HashSet<>();

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getDescription() {
        return description;
    }

    public void connectionBiDirectional(Place to) {
        biDirectionalConnections.add(to);
    }

    public void connectUniDirectionallyTo(Place to) {
        outgoingConnections.add(to);
    }
}
