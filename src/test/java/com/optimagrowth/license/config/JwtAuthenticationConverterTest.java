package com.optimagrowth.license.config;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationConverterTest {

    private final JwtAuthenticationConverter converter = new SecurityConfig().jwtAuthenticationConverter();

    @Test
    void mapsFlatRolesClaimToSpringAuthorities() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("roles", List.of("admin", "user"))
                .build();

        JwtAuthenticationToken authentication = (JwtAuthenticationToken) converter.convert(jwt);

        assertThat(authentication.getAuthorities())
                .contains(new SimpleGrantedAuthority("ROLE_admin"), new SimpleGrantedAuthority("ROLE_user"));
    }
}
