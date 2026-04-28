package com.optimagrowth.license.config;

import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClientConfigTest {

    private static final String TOKEN_VALUE = "relay-token";
    private static final String BEARER_TOKEN = "Bearer " + TOKEN_VALUE;

    private final ClientConfig clientConfig = new ClientConfig();
    private final BearerTokenResolver bearerTokenResolver = new BearerTokenResolver();
    private final OrganizationFeignClientConfiguration feignClientConfiguration =
            new OrganizationFeignClientConfiguration();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void restClientInterceptorRelaysJwtBearerToken() throws Exception {
        authenticateWithJwt();

        MockClientHttpRequest request = applyRestClientInterceptor();

        assertThat(authorizationHeader(request)).isEqualTo(BEARER_TOKEN);
    }

    @Test
    void restClientInterceptorDoesNotAddAuthorizationWhenAuthenticationHasNoBearerToken() throws Exception {
        authenticateWithoutBearerToken();

        MockClientHttpRequest request = applyRestClientInterceptor();

        assertThat(request.getHeaders()).doesNotContainKey(HttpHeaders.AUTHORIZATION);
    }

    @Test
    void feignInterceptorRelaysJwtBearerToken() {
        authenticateWithJwt();

        RequestTemplate requestTemplate = applyFeignInterceptor();

        assertThat(requestTemplate.headers())
                .containsEntry(HttpHeaders.AUTHORIZATION, List.of(BEARER_TOKEN));
    }

    @Test
    void feignInterceptorReplacesExistingAuthorizationHeader() {
        authenticateWithJwt();
        RequestTemplate requestTemplate = new RequestTemplate();
        requestTemplate.header(HttpHeaders.AUTHORIZATION, "Bearer stale-token");

        applyFeignInterceptor(requestTemplate);

        assertThat(requestTemplate.headers())
                .containsEntry(HttpHeaders.AUTHORIZATION, List.of(BEARER_TOKEN));
    }

    @Test
    void feignInterceptorDoesNotAddAuthorizationWhenAuthenticationHasNoBearerToken() {
        authenticateWithoutBearerToken();

        RequestTemplate requestTemplate = applyFeignInterceptor();

        assertThat(requestTemplate.headers()).doesNotContainKey(HttpHeaders.AUTHORIZATION);
    }

    private void authenticateWithJwt() {
        Jwt jwt = Jwt.withTokenValue(TOKEN_VALUE)
                .header("alg", "none")
                .claim("sub", "user")
                .build();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    private void authenticateWithoutBearerToken() {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("user", "password", null));
    }

    private MockClientHttpRequest applyRestClientInterceptor() throws Exception {
        ClientHttpRequestInterceptor interceptor =
                clientConfig.bearerTokenRelayRestClientInterceptor(bearerTokenResolver);
        MockClientHttpRequest request = new MockClientHttpRequest(
                HttpMethod.GET,
                URI.create("http://organization-service"));

        try (ClientHttpResponse response = interceptor.intercept(request, emptyRequestBody(), successfulResponse())) {
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            return request;
        }
    }

    private RequestTemplate applyFeignInterceptor() {
        RequestTemplate requestTemplate = new RequestTemplate();

        applyFeignInterceptor(requestTemplate);

        return requestTemplate;
    }

    private void applyFeignInterceptor(RequestTemplate requestTemplate) {
        feignClientConfiguration
                .organizationFeignBearerTokenRelayInterceptor(bearerTokenResolver)
                .apply(requestTemplate);
    }

    private String authorizationHeader(MockClientHttpRequest request) {
        return request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    }

    private byte[] emptyRequestBody() {
        return new byte[0];
    }

    private ClientHttpRequestExecution successfulResponse() {
        return (_, _) -> new MockClientHttpResponse(emptyRequestBody(), HttpStatus.OK);
    }
}
