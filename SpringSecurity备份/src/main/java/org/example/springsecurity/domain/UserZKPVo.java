package org.example.springsecurity.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.PrivateKey;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserZKPVo {
    private PrivateKey PrivateSecret;
    private String jwt;
}
