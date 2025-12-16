package com.joshgav.apiserver.controller;

import com.google.common.collect.Iterables;
import com.joshgav.apiserver.model.Widget;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureEmbeddedDatabase(provider = AutoConfigureEmbeddedDatabase.DatabaseProvider.ZONKY)
@AutoConfigureTestRestTemplate
public class WidgetControllerTest {
    private static final Logger logger = LoggerFactory.getLogger(WidgetControllerTest.class);
    private static final ParameterizedTypeReference<Collection<Widget>> responseClass = new ParameterizedTypeReference<>(){};
    private URI baseURI;
    @LocalServerPort private int port;
    @Autowired private TestRestTemplate restTemplate;

    @BeforeEach
    void setup() throws URISyntaxException {
        baseURI = new URI("http://localhost:" + port);
    }

    @DisplayName("get one widget")
    @Test
    public void getWidget() {
        Widget testWidget = Widget.builder().id(UUID.randomUUID().toString()).type("WidgetX").modelName(UUID.randomUUID().toString()).build();
        RequestEntity<Widget> putReq = new RequestEntity<Widget>(testWidget, HttpMethod.PUT, URI.create(baseURI + "/widgets/" + testWidget.getId()));
        ResponseEntity<Collection<Widget>> putRes = this.restTemplate.exchange(putReq, responseClass);
        assertTrue(putRes.hasBody(), "failed to create widget");

        RequestEntity<String> getReq = new RequestEntity<>("", HttpMethod.GET, URI.create(baseURI + "/widgets/" + testWidget.getId()));
        ResponseEntity<Collection<Widget>> getRes = this.restTemplate.exchange(getReq,responseClass);
        assertTrue(getRes.hasBody(), "did not find created widget");
        assertEquals(HttpStatus.OK, getRes.getStatusCode(), "status code not OK");
        assertNotNull(getRes.getBody(), "did not find created widget in body");
        assertEquals(testWidget.getId(), Iterables.getOnlyElement(getRes.getBody()).getId(), "retrieved id doesn't match original");

        RequestEntity<String> deleteReq = new RequestEntity<>("", HttpMethod.DELETE, URI.create(baseURI + "/widgets/" + testWidget.getId()));
        ResponseEntity<String> deleteRes = this.restTemplate.exchange(deleteReq, String.class);
        assertEquals(HttpStatus.ACCEPTED, deleteRes.getStatusCode());

        RequestEntity<String> getAfterDeleteReq = new RequestEntity<>("", HttpMethod.GET, URI.create(baseURI + "/widgets/" + testWidget.getId()));
        ResponseEntity<Collection<Widget>> getAfterDeleteRes = this.restTemplate.exchange(getAfterDeleteReq, responseClass);
        assertFalse(getAfterDeleteRes.hasBody(), "expected empty body after delete");
        assertEquals(HttpStatus.NOT_FOUND, getAfterDeleteRes.getStatusCode(), "expected NOT_FOUND after delete");


    }
}
