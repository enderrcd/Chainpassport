package org.example.springsecurity.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Oauth2VO {
    private String client_id;
    private String redirect_uri;
    private String response_type;
    private String scope;
}
