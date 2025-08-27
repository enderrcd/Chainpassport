package org.example.springsecurity.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "race.oauth2")
public class Oauth2Properties {
    private String client_id;
    private String redirect_uri;
    private String response_type;
    private String scope;
    private String state;
    private String client_secret;
}
