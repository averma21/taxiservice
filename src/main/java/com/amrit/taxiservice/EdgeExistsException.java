package com.amrit.taxiservice;

import static com.amrit.taxiservice.model.Graph.Vertex;

public class EdgeExistsException extends TaxiServiceException {

    private final Vertex v1;
    private final Vertex v2;

    public EdgeExistsException(Vertex v1, Vertex v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    @Override
    public String getMessage() {
        return String.format("An edge already exists between {%s} and {%s}", v1.getId(), v2.getId());
    }
}
