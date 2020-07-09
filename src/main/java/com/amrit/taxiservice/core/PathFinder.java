package com.amrit.taxiservice.core;

import com.amrit.taxiservice.model.Graph;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class PathFinder {

    private static class BFS {

        private final Graph graph;
        private final Map<Graph.Vertex, Graph.Vertex> pathMap;
        private final Queue<Graph.Vertex> queue;
        private final Set<Graph.Vertex> visited;
        private boolean found;

        public BFS(Graph graph) {
            this.graph = graph;
            queue = new LinkedList<>();
            this.pathMap = new HashMap<>();
            this.visited = new HashSet<>();
        }

        private List<Graph.Vertex> doBFSInternal(Graph.Vertex v1, Graph.Vertex v2) {
            if (v1.equals(v2)) {
                found = true;
                return Collections.singletonList(v1);
            }
            queue.add(v1);
            pathMap.putIfAbsent(v1, null);
            List<Graph.Vertex> path = new LinkedList<>();
            while (!queue.isEmpty()) {
                Graph.Vertex v = queue.poll();
                if (v.equals(v2)) {
                    found = true;
                    break;
                }
                visited.add(v);
                for (Graph.Connection connection : graph.getConnections(v)) {
                    if (!visited.contains(connection.to)) {
                        queue.add(connection.to);
                        pathMap.put(connection.to, v);
                    }
                }
            }
            if (found) {
                Graph.Vertex current = v2;
                while (current != null) {
                    path.add(current);
                    current = pathMap.get(current);
                }
            }
            Collections.reverse(path);
            return path;
        }
    }



    public static List<Graph.Vertex> doBFS(Graph graph, @NonNull Graph.Vertex v1, Graph.Vertex v2) {
        BFS bfs = new BFS(graph);
        return bfs.doBFSInternal(v1, v2);
    }

}
