package com.amrit.taxiservice.api;

import com.amrit.taxiservice.model.Graph;
import com.amrit.taxiservice.model.Place;
import com.amrit.taxiservice.service.GraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@RequestMapping("/api/v1/maps")
@RestController
public class MapController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapController.class.getName());

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

    /**
     * Sample Request -
     * curl -XPOST localhost:8080/api/v1/maps --data '{"name":"My Place", "latitude":48.235345,"longitude":121.845972,"description":"Second Place","_csrf":"xxxx"}' \
     * -H "Content-Type:application/json" -v --cookie "XSRF-TOKEN=xxxx" -H "X-XSRF-TOKEN: xxxx"
     * @param place place to add
     */
    @PostMapping
    public void addPlace(@Valid @NonNull @RequestBody Place place) {
        graphService.addPlace(place);
    }

    /**
     * Sample Request -
     * curl -XPOST localhost:8080/api/v1/maps/connect --cookie "XSRF-TOKEN=$token" -H "X-XSRF-TOKEN: $token" -d 'flat=48.235345&flon=121.845972&tolat=48.234165&tolon=120.870972&bi=true'
     */
    @PostMapping(path = "/connect")
    public void connect( @RequestParam("flat")  double flat,  @RequestParam("flon")  double flon, @RequestParam("tolat")
            double tolat, @RequestParam("tolon") double tolon, @RequestParam("bi") boolean bi) {
        graphService.connect(flat, flon, tolat, tolon, bi);
    }

//    @GetMapping(path = "/find")
//    public Page<Place> getAllPlacesNear( @RequestParam("latitude")  long latitude,  @RequestParam("longitude")  long longitude) {
//        return graphService.getAllPlacesAround(latitude, longitude, 1, 10);
//    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handle(HttpMessageNotReadableException e) {
        LOGGER.warn("Returning HTTP 400 Bad Request", e);
    }

}
