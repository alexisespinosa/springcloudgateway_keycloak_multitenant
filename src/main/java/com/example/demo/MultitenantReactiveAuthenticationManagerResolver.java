package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenReactiveAuthenticationManager;
import org.springframework.security.oauth2.server.resource.introspection.NimbusReactiveOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenIntrospector;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MultitenantReactiveAuthenticationManagerResolver implements ReactiveAuthenticationManagerResolver<ServerWebExchange> {
    Logger logger = LoggerFactory.getLogger(MultitenantReactiveAuthenticationManagerResolver.class);

    private final Map<String, OpaqueTokenReactiveAuthenticationManager> cache = new ConcurrentHashMap<>();

    @Override
    public Mono<ReactiveAuthenticationManager> resolve(ServerWebExchange exchange) {
        ReactiveOpaqueTokenIntrospector opaqueTokenIntrospector;
        String tenantId = exchange.getRequest().getHeaders().getFirst("tenant");

        this.logger.info(tenantId);

        if (!cache.containsKey(tenantId)) {
            String instrospectionUri = null;
            String clientId = null;
            String clientSecret = null;

//            ToDo Automate getting config by tenant
            if (tenantId.equals("tenant01")) {
                instrospectionUri = "http://localhost:8080/realms/tenant01/protocol/openid-connect/token/introspect";
                clientId = "clientId";
                clientSecret = "clientSecret";
            } else if (tenantId.equals("tenant02")) {
                instrospectionUri = "http://localhost:8080/realms/tenant02/protocol/openid-connect/token/introspect";
                clientId = "clientId";
                clientSecret = "clientSecret";
            }

            opaqueTokenIntrospector =  new NimbusReactiveOpaqueTokenIntrospector(
                    instrospectionUri,
                    clientId,
                    clientSecret
            );
            cache.put(tenantId, new OpaqueTokenReactiveAuthenticationManager(opaqueTokenIntrospector));
        }

        return Mono.just(cache.get(tenantId));
    }
}
