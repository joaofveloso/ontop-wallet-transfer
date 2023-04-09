package com.ontop.balance.app;

import java.net.URI;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public class LocationUtils {

    public static HttpHeaders getHttpHeadersWithLocation(String key) {
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(key).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return headers;
    }
}