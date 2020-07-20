package com.amrit.taxiservice.service;

import com.amrit.taxiservice.model.Cab;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Service
public class CabService {

    private final Map<String, Cab> cabs;

    public CabService() {
        this.cabs = new HashMap<>();
    }

    public void registerCab(@NonNull Cab cab) {
        cabs.putIfAbsent(cab.getRegistrationNo(), cab);
    }

    public Optional<Cab> assignCab() {
        Optional<Cab> cab = cabs.values().stream().filter(Predicate.not(Cab::isBooked)).findFirst();
        cab.ifPresent(c -> c.setBooked(true));
        return cab;
    }

    public void updatePos(String regNo, double lat, double lon) {
        cabs.computeIfPresent(regNo, (r,c) -> {c.setLatitude(lat);c.setLongitude(lon);return c;});
    }

    public Cab getCab(String regNo) {
        return cabs.get(regNo);
    }
}
