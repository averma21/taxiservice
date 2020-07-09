package com.amrit.taxiservice.model;

import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

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

    public static class Connection {
        public final Vertex to;
        public final int cost;

        public Connection(Vertex to) {
            this.to = to;
            this.cost = 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Connection that = (Connection) o;
            return cost == that.cost &&
                    Objects.equals(to, that.to);
        }

        @Override
        public int hashCode() {
            return Objects.hash(to, cost);
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

    Map<Vertex, Set<Connection>> connections;
    UUID id;

    public Graph() {
        this.connections = new HashMap<>();
        this.id = UUID.randomUUID();
    }

    public void addVertex(@NonNull Vertex vertex) {
        this.connections.putIfAbsent(vertex, new HashSet<>());
    }

    public void addEdge(Vertex v1, Vertex v2, boolean bidirectional) {
        addVertex(v1);
        addVertex(v2);
        this.connections.computeIfPresent(v1, (v, c) -> {
            c.add(new Connection(v2));
            return c;
        });
        if (bidirectional) {
            this.connections.computeIfPresent(v2, (v, c) -> {
                c.add(new Connection(v1));
                return c;
            });
        }
    }

    public UUID getId() {
        return id;
    }

    public Set<Vertex> getVertices() {
        return connections.keySet();
    }

    public Set<Connection> getConnections(Vertex v) {
        return connections.get(v);
    }

    public Set<Edge> getEdges() {
        Set<Edge> edges = new HashSet<>();
        for (Map.Entry<Vertex, Set<Connection>> entry : connections.entrySet()) {
            Vertex from = entry.getKey();
            for (Connection connection : entry.getValue()) {
                Vertex to = connection.to;
                edges.add(new Edge(from, to, connection.cost, getConnections(to).contains(new Connection(from))));
            }
        }
        return edges;
    }

}
