package com.amrit.taxiservice.service;

import com.amrit.taxiservice.core.PathFinder;
import com.amrit.taxiservice.model.Graph;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
public class GraphService {

    private static class GraphGenerator {

        Random random = new Random();

        Graph generate(double minLat, double maxLat, double minLon, double maxLon, int vertexCount, int edgeCount) {
            Graph graph = new Graph();
            List<Graph.Vertex> vertexList = new ArrayList<>(vertexCount);
            while (vertexCount > 0) {
                double ran1 = random.nextDouble();
                double ran2 = random.nextDouble();
                double lat = minLat + (maxLat - minLat) * ran1;
                double lon = minLon + (maxLon - minLon) * ran2;
                Graph.Vertex vertex = new Graph.Vertex("V-" + vertexCount, lat, lon);
                vertexList.add(vertex);
                graph.addVertex(vertex);
                vertexCount--;
            }
            int bidirectionalEdgeCount = (int) (0.75 * edgeCount);
            while (edgeCount > 0) {
                int r1 = random.nextInt(vertexList.size());
                int r2 = random.nextInt(vertexList.size());
                graph.addEdge(vertexList.get(r1), vertexList.get(r2), bidirectionalEdgeCount > 0);
                bidirectionalEdgeCount--;
                edgeCount--;
            }
            return graph;
        }
    }

    GraphGenerator generator = new GraphGenerator();
    Map<UUID, Graph> graphMap = new HashMap<>();

    public Graph getWholeNewGraph() {
        Graph g = generator.generate(-90, 90, -180, 180, 10, 7);
        graphMap.put(g.getId(), g);
        return g;
    }

    public List<Graph.Vertex> getPath(UUID graphId, Graph.Vertex v1, Graph.Vertex v2) {
        return PathFinder.doBFS(graphMap.get(graphId), v1, v2);
    }
}
