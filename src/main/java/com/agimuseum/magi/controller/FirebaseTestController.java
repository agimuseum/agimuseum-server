package com.agimuseum.magi.controller;

import com.agimuseum.magi.service.FirebaseTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/debug/firebase")
@Slf4j
@RequiredArgsConstructor
public class FirebaseTestController {

    private final FirebaseTestService firebaseTestService;

    @GetMapping("/test-storage")
    public ResponseEntity<Map<String, Object>> testFirebaseStorage() {
        log.info("Testing Firebase Storage connectivity");
        Map<String, Object> result = firebaseTestService.testFirebaseStorage();
        return ResponseEntity.ok(result);
    }
}