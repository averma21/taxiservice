package com.amrit.taxiservice.api;

import com.amrit.taxiservice.model.Cab;
import com.amrit.taxiservice.model.Graph;
import com.amrit.taxiservice.service.CabService;
import com.amrit.taxiservice.service.GraphService;
import com.amrit.taxiservice.service.messaging.MessagingGateway;
import com.amrit.taxiserviceapi.messaging.Duty;
import com.amrit.taxiserviceapi.messaging.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequestMapping("/api/v1/cabs")
@RestController
public class CabController {

    CabService cabService;
    SsePushNotificationService notificationService;
    MessagingGateway messagingGateway;
    GraphService graphService;



    @Autowired
    public CabController(CabService cabService, SsePushNotificationService notificationService,
                         MessagingGateway messagingGateway, GraphService graphService) {
        this.cabService = cabService;
        this.notificationService = notificationService;
        this.messagingGateway = messagingGateway;
        this.graphService = graphService;
        notificationService.doNotify();
        registerCabs();
    }

    private void registerCabs() {
        int cabCount = 5;
        for (int i = 0; i < cabCount; i++) {
            cabService.registerCab(new Cab("Cab-" + i));
        }
    }

    @PostMapping(path = "assign")
    public ResponseEntity<Optional<Cab>> assignCab(@RequestParam("v1") UUID v1, @RequestParam("v2") UUID v2) {
        Optional<Cab> cab = cabService.assignCab();
        if (cab.isPresent()) {
            List<Graph.Vertex> vertices = graphService.getPath(null, v1, v2);
            List<Position> positions = vertices.stream().map(v -> new Position(v.getLatitude(),
                    v.getLongitude())).collect(Collectors.toList());
            messagingGateway.sendMessage("taxiservice.assignduty", cab.get().getRegistrationNo(), serialize(new Duty(positions)));
            return ResponseEntity.status(200).body(cab);
        }
        return ResponseEntity.status(204).build();

    }


    public static byte[] serialize(final Object obj) {
        return org.apache.commons.lang3.SerializationUtils.serialize((Serializable) obj);
    }

    @PostMapping(path = "{id}")
    public void updateCabPosition(@PathVariable ("id") String regNo, double latitude, double longitude) {
        cabService.updatePos(regNo, latitude, longitude);
        notificationService.addNotification(new SsePushNotificationService.Notification(regNo, latitude, longitude));
    }

    @GetMapping(path = "{regNo}")
    public ResponseEntity<SseEmitter> subscribe(@PathVariable ("regNo") String regNo) {
        Cab cab = cabService.getCab(regNo);
        if (cab != null) {
            final SseEmitter emitter = new SseEmitter();
            notificationService.addEmitter(cab.getRegistrationNo(), emitter);
            emitter.onCompletion(() -> notificationService.removeEmitter(cab.getRegistrationNo()));
            emitter.onTimeout(() -> notificationService.removeEmitter(cab.getRegistrationNo()));
            return new ResponseEntity<>(emitter, HttpStatus.OK);
        }
        return ResponseEntity.status(204).build();
    }


}
