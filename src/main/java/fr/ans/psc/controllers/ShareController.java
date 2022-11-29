package fr.ans.psc.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;
import java.util.Objects;

@RestController
@Slf4j
public class ShareController {

    private final RestTemplate restTemplate;

    private final String APPLICATION_JSON = MediaType.APPLICATION_JSON_VALUE;
    private final String ACCESS_TOKEN_HEADER = "oidc_access_token";

    @Value("${psc.context.sharing.api.url}")
    private String shareApiBaseUrl;

    public ShareController() {
        this.restTemplate = new RestTemplate();
    }

    @GetMapping(value = "/secure/share", produces = APPLICATION_JSON)
    public ResponseEntity<String> getContextInCache() {
        log.debug("getting stored ProSanteConnect context...");
        HttpEntity<String> entity = prepareRequest(null);

        try {
            log.debug("calling ProSanteConnect API...");
            String response = restTemplate.exchange(URI.create(shareApiBaseUrl), HttpMethod.GET, entity, String.class).getBody();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while requesting ProSanteConnect context sharing API with root cause : {}", e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping(value = "/secure/share", produces = APPLICATION_JSON, consumes = APPLICATION_JSON)
    public ResponseEntity<String> putContextInCache(@RequestBody String jsonContext) {
        log.debug("putting context in ProSanteConnect Cache...");
        HttpEntity<String> entity = prepareRequest(jsonContext);

        try {
            log.debug("calling ProSanteConnect API...");
            log.debug(entity.getBody());
            String response = restTemplate.exchange(URI.create(shareApiBaseUrl), HttpMethod.PUT, entity, String.class).getBody();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while requesting ProSanteConnect context sharing API with root cause : {}", e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private HttpEntity<String> prepareRequest(String requestBody) {
        log.debug("retrieving access token...");
        HttpServletRequest incoming = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + incoming.getHeader(ACCESS_TOKEN_HEADER));
        headers.add(HttpHeaders.ACCEPT, APPLICATION_JSON);

        if (requestBody != null) {
            headers.add(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
            log.debug("request successfully prepared");
            return new HttpEntity<>(requestBody, headers);
        } else {
            log.debug("request successfully prepared");
            return new HttpEntity<>(headers);
        }
    }
}
