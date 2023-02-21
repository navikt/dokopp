package no.nav.dokopp.consumer.pdl;

import org.mockito.ArgumentMatcher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;

import java.net.URI;

import static no.nav.dokopp.constants.HeaderConstants.NAV_CALL_ID;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class RequestEntityMatcher implements ArgumentMatcher<RequestEntity> {

    private final String personnummer;
    private final String callId;
    private final String token;
    private final URI pdlUrl;

    private static final String BEARER_PREFIX = "Bearer ";


    RequestEntityMatcher(String personnummer, String callId, String token, URI pdlUrl){
        this.personnummer = personnummer;
        this.callId = callId;
        this.token = token;
        this.pdlUrl = pdlUrl;
    }


    @Override
    public boolean matches(RequestEntity right) {
        HttpHeaders headers = right.getHeaders();
        HttpMethod method = right.getMethod();
        URI uri = right.getUrl();
        PdlRequest body = (PdlRequest) right.getBody();

        return  //header
            (headers.size() == 5) &&
            (headers.getContentType().equals(APPLICATION_JSON)) &&
            (headers.getAccept().size() == 1) &&
            (headers.getAccept().get(0).equals(APPLICATION_JSON)) &&
            (headers.get(NAV_CALL_ID).size() == 1) &&
            (headers.get(NAV_CALL_ID).get(0).equals(callId)) &&
            (headers.get(AUTHORIZATION).size() == 1) &&
            (headers.get(AUTHORIZATION).get(0).equals(BEARER_PREFIX + token)) &&
            (headers.get("Nav-Consumer-Token").size() == 1) &&
            (headers.get("Nav-Consumer-Token").get(0).equals(BEARER_PREFIX + token)) &&
            // method
            method.equals(POST) &&
            // URI
            uri.equals(pdlUrl) &&
            //body
            (body.getVariables().size() == 1) &&
            body.getVariables().get("ident").equals(personnummer);
    }
}