package com.amrit.taxiservice.service;

import com.amrit.taxiservice.EdgeExistsException;
import com.amrit.taxiservice.core.PathFinder;
import com.amrit.taxiservice.model.Graph;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Data fetched from https://simplemaps.com/data/world-cities.
 */
@Service
public class GraphService {

    private UUID latestGraphID;

    private static class GraphGenerator {

        Random random = new Random();
        Random costRandomizer = new Random();

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
                try {
                    graph.addEdge(vertexList.get(r1), vertexList.get(r2), costRandomizer.nextInt(20),bidirectionalEdgeCount > 0);
                } catch (EdgeExistsException e) {
                    //ignore
                }
                bidirectionalEdgeCount--;
                edgeCount--;
            }
            return graph;
        }
    }

    GraphGenerator generator = new GraphGenerator();
    Map<UUID, Graph> graphMap = new HashMap<>();

    public Graph getWholeNewGraph() {
        Graph g = generator.generate(-90, 90, -180, 180, 16, 15);
        graphMap.put(g.getId(), g);
        latestGraphID = g.getId();
        return g;
    }

    public List<Graph.Vertex> getPath(UUID graphId, UUID fromVertex, UUID toVertex) {
        Graph graph = graphMap.get(graphId == null ? latestGraphID : graphId);
        Graph.Vertex v1 = graph.getVertex(fromVertex).orElse(null);
        Graph.Vertex v2 = graph.getVertex(toVertex).orElse(null);
        if (v1 == null) {
            System.out.println("From vertex doesn't exist"); //todo log
            return Collections.emptyList();
        }
        if (v2 == null) {
            System.out.println("To vertex doesn't exist"); //todo log
            return Collections.emptyList();
        }
        return PathFinder.doBFS(graph, v1, v2);
    }
}
