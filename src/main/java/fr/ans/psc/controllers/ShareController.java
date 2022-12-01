package fr.ans.psc.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.net.URI;

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
    public ResponseEntity<String> getContextInCache(@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient client) {
        log.debug("getting stored ProSanteConnect context...");
        HttpEntity<String> entity = prepareRequest(client, null);

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
    public ResponseEntity<String> putContextInCache(@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient client, @RequestBody String jsonContext) {
        log.debug("putting context in ProSanteConnect Cache...");
        HttpEntity<String> entity = prepareRequest(client, jsonContext);

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

    private HttpEntity<String> prepareRequest(OAuth2AuthorizedClient client, String requestBody) {
        log.debug("retrieving access token...");
        String accessToken = client.getAccessToken().getTokenValue();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
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
