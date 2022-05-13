package com.joshgav.apiserver.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.endpoint.DefaultRefreshTokenTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping(value={"/refresh"})
public class RefreshController {
    private static final Logger logger = LoggerFactory.getLogger(RefreshController.class);

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    OAuth2AuthorizedClientService clientService;

    @Operation(summary = "refresh user profile", tags = {"profile"})
    @GetMapping(path="", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> handle(HttpServletRequest req) {
        logger.info("refresh requested");
        StringBuilder page = new StringBuilder();

        SecurityContext context = SecurityContextHolder.getContext();
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) context.getAuthentication();
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
            token.getAuthorizedClientRegistrationId(),
            token.getName());

        OAuth2AccessTokenResponseClient<OAuth2RefreshTokenGrantRequest> refreshTokenResponseClient = 
            new DefaultRefreshTokenTokenResponseClient();

        page.append("original:\n");
        page.append(String.format("  name: %s\n", token.getName()));
        page.append(String.format("  accessToken:\n"));
        page.append(String.format("    value: %s\n", client.getAccessToken().getTokenValue()));
        page.append(String.format("    iat: %s\n", client.getAccessToken().getIssuedAt()));
        page.append(String.format("  refreshToken:\n"));
        page.append(String.format("    value: %s\n", client.getRefreshToken().getTokenValue()));
        page.append(String.format("    iat: %s\n", client.getRefreshToken().getIssuedAt()));

        OAuth2AccessTokenResponse response;
        try {
            logger.info("INFO: attempting to force reauthentication");
            response = refreshTokenResponseClient.getTokenResponse(new OAuth2RefreshTokenGrantRequest(
                client.getClientRegistration(), client.getAccessToken(), client.getRefreshToken()));

            SecurityContext context2 = SecurityContextHolder.getContext();
            OAuth2AuthenticationToken token2 = (OAuth2AuthenticationToken) context2.getAuthentication();

            page.append("\nnew:\n");
            page.append(String.format("  name: %s\n", token2.getName()));
            page.append(String.format("  accessToken:\n"));
            page.append(String.format("    value: %s\n", response.getAccessToken().getTokenValue()));
            page.append(String.format("    iat: %s\n", response.getAccessToken().getIssuedAt()));
            page.append(String.format("  refreshToken:\n"));
            page.append(String.format("    value: %s\n", response.getRefreshToken().getTokenValue()));
            page.append(String.format("    iat: %s\n", response.getRefreshToken().getIssuedAt()));

        } catch (AuthenticationException e) {
            logger.warn("WARN: token refresh failed. Exception: %s", e);
        }

        return ResponseEntity.ok(page.toString());
    }
}
