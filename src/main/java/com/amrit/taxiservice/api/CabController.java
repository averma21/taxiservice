package com.amrit.taxiservice.api;

import com.amrit.taxiservice.model.Cab;
import com.amrit.taxiservice.service.CabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Optional;

@RequestMapping("/api/v1/cabs")
@RestController
public class CabController {

    CabService cabService;
    SsePushNotificationService notificationService;

    @Autowired
    public CabController(CabService cabService, SsePushNotificationService notificationService) {
        this.cabService = cabService;
        this.notificationService = notificationService;
        notificationService.doNotify();
        registerCabs();
    }

    private void registerCabs() {
        int cabCount = 5;
        for (int i = 0; i < cabCount; i++) {
            cabService.registerCab(new Cab("Cab-" + i));
        }
    }

    @PostMapping
    public ResponseEntity<Optional<Cab>> assignCab() {
        Optional<Cab> cab = cabService.assignCab();
        if (cab.isPresent()) {
            new Thread() {
                @Override
                public void run() {
                    int x = 10, y = 10;
                    while (x < 20) {
                        try {
                            sleep(1000);
                            updateCabPosition(cab.get().getRegistrationNo(), x++, y++);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
            return ResponseEntity.status(200).body(cab);
        }
        return ResponseEntity.status(204).build();

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
