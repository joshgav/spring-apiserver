package com.joshgav.apiserver.controller;

import io.opentelemetry.api.trace.Span;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value={"/greeting", "/"})
public class GreetingController {
    private static final Logger logger = LoggerFactory.getLogger(GreetingController.class);

    @Operation(summary = "greet user", tags = {"profile"})
    @GetMapping(path="", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> greet(HttpServletRequest req) {
        logger.info("greeting requested");
        String traceId = Span.current().getSpanContext().getTraceId();
        String spanId = Span.current().getSpanContext().getSpanId();
        logger.debug("traceId {}, spanId {}", traceId, spanId);

        StringBuilder page = new StringBuilder();

        page.append("Hello world!");
        
        return ResponseEntity.ok(page.toString());
    }
}
