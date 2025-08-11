package org.example.springsecurity.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Oauth2User {
    private String userName;
    private String giteeId;
    private String nickName;
    private String avatar;
    private String email;
}
