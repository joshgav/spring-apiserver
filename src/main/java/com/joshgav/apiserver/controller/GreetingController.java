package com.joshgav.apiserver.controller;

import io.opentelemetry.api.trace.Span;
import io.swagger.v3.oas.annotations.Operation;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;

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

        // Spring Security
        SecurityContext context = SecurityContextHolder.getContext();
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) context.getAuthentication();
        page.append(String.format("tokenName: %s\n", token.getName()));


        token.getAuthorities().forEach(authority -> {
            if (OidcUserAuthority.class.isInstance(authority)) {
                OidcUserAuthority oidcUserAuthority = (OidcUserAuthority) authority;

                OidcIdToken idToken = oidcUserAuthority.getIdToken();
                OidcUserInfo userInfo = oidcUserAuthority.getUserInfo();

                page.append(String.format("tokenOidcUserAuthority: %s\n", oidcUserAuthority.getAuthority()));
                page.append(String.format("tokenOidcUserAuthorityIdTokenClaims:\n"));
                for (Map.Entry<String, Object> claim: idToken.getClaims().entrySet()) {
                    page.append(String.format("  %s: %s\n", claim.getKey(), claim.getValue()));
                }

                page.append(String.format("tokenOidcUserAuthorityUserInfoClaims:\n"));
                for (Map.Entry<String, Object> claim: userInfo.getClaims().entrySet()) {
                    page.append(String.format("  %s: %s\n", claim.getKey(), claim.getValue()));
                }


            } else if (OAuth2UserAuthority.class.isInstance(authority)) {
                OAuth2UserAuthority oauth2UserAuthority = (OAuth2UserAuthority) authority;

                Map<String, Object> userAttributes = oauth2UserAuthority.getAttributes();
                page.append(String.format("tokenOauth2UserAuthority: %s\n", oauth2UserAuthority.getAuthority()));
                page.append(String.format("tokenOauth2UserAuthorityAttributes:\n"));
                for (Map.Entry<String, Object> attribute: userAttributes.entrySet()) {
                    page.append(String.format("  %s: %s\n", attribute.getKey(), attribute.getValue()));
                }
            }
        });
        
        OAuth2User user = token.getPrincipal();
        page.append(String.format("\nuserName: %s\n", user.getName()));

        Map<String, Object> oauth2Attributes = user.getAttributes();
        page.append(String.format("oauthAttributes:\n"));
        for (Map.Entry<String, Object> attribute : oauth2Attributes.entrySet()) {
            page.append(String.format("  %s: %s\n", attribute.getKey(), attribute.getValue()));
        }
        
        return ResponseEntity.ok(page.toString());
    }
}
