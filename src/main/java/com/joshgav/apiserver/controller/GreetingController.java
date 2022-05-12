package com.joshgav.apiserver.controller;

import io.opentelemetry.api.trace.Span;
import io.swagger.v3.oas.annotations.Operation;

import java.text.Format;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.IDToken;

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

        KeycloakSecurityContext context =
            (KeycloakSecurityContext) req.getAttribute(KeycloakSecurityContext.class.getName());
        IDToken idToken = context.getIdToken();

        String preferredUsername = idToken.getPreferredUsername();
        String issuer = idToken.getIssuer();
        String subject = idToken.getSubject();
       
        String content = String.format("Hello %s!\nID token issued by: %s\nto subject: %s",
            preferredUsername, issuer, subject);
        
        return ResponseEntity.ok(content);
    }

}
