package com.hireforge.api_gateway.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import static org.mockito.Mockito.lenient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private GatewayFilterChain chain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private GatewayFilter filter() {
        return jwtAuthenticationFilter.apply(new Object());
    }

    @Test
    void filter_ShouldPassRequest_WhenPathIsPublicLogin() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/auth/login").build());
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        filter().filter(exchange, chain).block();

        verify(chain).filter(exchange);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void filter_ShouldReturn401_WhenAuthorizationHeaderIsMissing() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/resumes/history").build());

        filter().filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verifyNoInteractions(chain);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void filter_ShouldReturn401_WhenHeaderDoesNotStartWithBearer() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/resumes/history")
                        .header("Authorization", "Basic abc123")
                        .build());

        filter().filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verifyNoInteractions(chain);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void filter_ShouldReturn401_WhenTokenIsInvalid() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/resumes/history")
                        .header("Authorization", "Bearer bad.token.value")
                        .build());
        when(jwtUtil.validateToken("bad.token.value")).thenReturn(false);

        filter().filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verifyNoInteractions(chain);
    }

    @Test
    void filter_ShouldAddUserIdHeaderAndContinue_WhenTokenIsValid() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/resumes/history")
                        .header("Authorization", "Bearer good.token.value")
                        .build());
        when(jwtUtil.validateToken("good.token.value")).thenReturn(true);
        when(jwtUtil.getUserId("good.token.value")).thenReturn(5L);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        filter().filter(exchange, chain).block();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());
        List<String> userIdHeader = captor.getValue().getRequest().getHeaders().get("X-User-Id");

        assertEquals(List.of("5"), userIdHeader);
    }

    @Test
    void filter_ShouldOverwriteSpoofedUserIdHeader_WhenClientSendsItsOwn() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/resumes/history")
                        .header("Authorization", "Bearer good.token.value")
                        .header("X-User-Id", "999")
                        .build());
        when(jwtUtil.validateToken("good.token.value")).thenReturn(true);
        when(jwtUtil.getUserId("good.token.value")).thenReturn(5L);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        filter().filter(exchange, chain).block();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());
        List<String> userIdHeader = captor.getValue().getRequest().getHeaders().get("X-User-Id");

        assertEquals(List.of("5"), userIdHeader);
    }

    @Test
    void filter_ShouldReturn401_WhenPublicPathIsOnlyEmbeddedInAnotherPath() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/resumes/api/auth/login").build());
        lenient().when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        filter().filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verifyNoInteractions(chain);
    }
}