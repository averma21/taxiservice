package com.amrit.taxiservice.api;

import com.amrit.taxiservice.model.Graph;
import com.amrit.taxiservice.service.GraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotBlank;

@RequestMapping("/api/v1/maps")
@RestController
public class MapController {

    GraphService graphService;

    @Autowired
    public MapController(GraphService graphService) {
        this.graphService = graphService;
    }

    @GetMapping
    public Graph getGraph() {
        return graphService.getWholeNewGraph();
    }

    @GetMapping(path = "{id}")
    public List<Graph.Vertex> getPath(@PathVariable("id")  UUID graphID, @NotBlank @RequestParam("v1") UUID v1,
                                      @NotBlank @RequestParam("v2")  UUID v2) {
        return graphService.getPath(graphID, v1, v2);
    }

}
