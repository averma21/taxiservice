package com.amrit.taxiservice.model;

import com.amrit.taxiservice.EdgeExistsException;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * A graph to represent maps. A vertex is considered a point on ground and has a latitude and a longitude. An edge of this
 * graph is considered a straight line. Hence there can be only one edge between two vertices. It could be bidirectional
 * or unidirectional.
 */
public class Graph {

    public static class Vertex {
        UUID id;
        String name;
        double latitude;
        double longitude;

        public Vertex(String name, double latitude, double longitude) {
            this.id = UUID.randomUUID();
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Vertex vertex = (Vertex) o;
            return Double.compare(vertex.latitude, latitude) == 0 &&
                    Double.compare(vertex.longitude, longitude) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(latitude, longitude);
        }

        @Override
        public String toString() {
            return String.format("Latitude %s, Longitude %s", latitude, longitude);
        }

        public UUID getId() {
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
    }

    public static class Edge {
        Vertex v1;
        Vertex v2;
        int cost;
        /**
         * If true, this is bidirectional otherwise only from v1 to v2.
         */
        boolean bidirectional;

        private Edge(Vertex v1, Vertex v2, int cost, boolean bidirectional) {
            this.v1 = v1;
            this.v2 = v2;
            this.cost = cost;
            this.bidirectional = bidirectional;
        }

        public Vertex getV1() {
            return v1;
        }

        public Vertex getV2() {
            return v2;
        }

        public boolean isBidirectional() {
            return bidirectional;
        }

        public int getCost() {
            return cost;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edge edge = (Edge) o;
            return cost == edge.cost &&
                    Objects.equals(v1, edge.v1) &&
                    Objects.equals(v2, edge.v2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(v1, v2, cost);
        }
    }

    /**
     * Class to help in queries like - what are the available edges <b>between</b> two vertices. Direction of edge doesn't
     * matter.
     */
    private static class EdgeContainer {

        private final Map<String, Edge> edgeMap;

        private EdgeContainer() {
            edgeMap = new HashMap<>();
        }

        private String getIDForMap(Vertex v1, Vertex v2) {
            UUID v1Id = v1.getId();
            UUID v2Id = v2.getId();
            if (v1Id.compareTo(v2Id) <= 0) {
                return v1Id.toString() + v2Id.toString();
            }
            return v2Id.toString() + v1Id.toString();
        }

        private synchronized void addEdge(Vertex v1, Vertex v2, int cost, boolean bidirectional) throws EdgeExistsException {
            Edge edge = new Edge(v1, v2, cost, bidirectional);
            Edge prev = edgeMap.putIfAbsent(getIDForMap(v1, v2), edge);
            if (prev != null) {
                throw new EdgeExistsException(v1, v2);
            }
        }

        private Collection<Edge> getAllEdges() {
            return edgeMap.values();
        }

        private boolean edgeExists(Vertex v1, Vertex v2) {
            return edgeMap.containsKey(getIDForMap(v1, v2));
        }
    }

    Map<Vertex, Set<Vertex>> connections;
    EdgeContainer edgeContainer;
    UUID id;

    public Graph() {
        this.connections = new HashMap<>();
        this.edgeContainer = new EdgeContainer();
        this.id = UUID.randomUUID();
    }

    public void addVertex(@NonNull Vertex vertex) {
        this.connections.putIfAbsent(vertex, new HashSet<>());
    }

    public synchronized void addEdge(Vertex v1, Vertex v2, int cost, boolean bidirectional) throws EdgeExistsException {

        if (!edgeContainer.edgeExists(v1, v2)) {
            addVertex(v1);
            addVertex(v2);
            this.connections.computeIfPresent(v1, (v, sv) -> {
                sv.add(v2);
                return sv;
            });
            if (bidirectional) {
                this.connections.computeIfPresent(v2, (v, sv) -> {
                    sv.add(v1);
                    return sv;
                });
            }
            edgeContainer.addEdge(v1, v2, cost, bidirectional);
        } else {
            System.out.println("Edge Already exists"); //todo use logger
        }
    }

    public UUID getId() {
        return id;
    }

    public Set<Vertex> getVertices() {
        return connections.keySet();
    }

    public Optional<Vertex> getVertex(UUID vertexID) {
        return connections.keySet().stream().filter(v -> v.getId().equals(vertexID)).findFirst();
    }

    public Set<Vertex> getConnectedVerticesFrom(Vertex v) {
        return connections.get(v);
    }

    public Collection<Edge> getEdges() {
        return edgeContainer.getAllEdges();
    }

}
