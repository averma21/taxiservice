package com.amrit.taxiservice.service;

import com.amrit.taxiservice.EdgeExistsException;
import com.amrit.taxiservice.core.PathFinder;
import com.amrit.taxiservice.model.Graph;
import com.amrit.taxiservice.model.Place;
import com.amrit.taxiservice.persistence.PlaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphService.class.getName());

    private final PlaceRepository placeRepository;

    @Autowired
    public GraphService(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

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
        Graph g = generator.generate(-90, 90, -180, 180, 6, 6);
        graphMap.put(g.getId(), g);
        latestGraphID = g.getId();
        return g;
    }

    public List<Graph.Vertex> getPath(UUID graphId, UUID fromVertex, UUID toVertex) {
        Graph graph = graphMap.get(graphId == null ? latestGraphID : graphId);
        Graph.Vertex v1 = graph.getVertex(fromVertex).orElse(null);
        Graph.Vertex v2 = graph.getVertex(toVertex).orElse(null);
        if (v1 == null) {
            LOGGER.error("From vertex doesn't exist"); //todo log
            return Collections.emptyList();
        }
        if (v2 == null) {
            LOGGER.error("To vertex doesn't exist"); //todo log
            return Collections.emptyList();
        }
        return PathFinder.doBFS(graph, v1, v2);
    }

    public void addPlace(Place place) {
        placeRepository.save(place);
    }

    public void connect(double flat, double flon, double tolat, double tolon, boolean bidirectional) {
        Place from = placeRepository.findByLatitudeAndLongitude(flat, flon);
        Place to = placeRepository.findByLatitudeAndLongitude(tolat, tolon);

        if (from != null && to != null) {
            if (bidirectional) {
                from.connectionBiDirectional(to);
            } else {
                from.connectUniDirectionallyTo(to);
            }
            placeRepository.save(from);
        }
    }

//    public Page<Place> getAllPlacesAround(long latitude, long longitude, int page, int size) {
//        return placeRepository.findByLatitudeAndLongitude(latitude, longitude, PageRequest.of(page, size));
//    }
}
