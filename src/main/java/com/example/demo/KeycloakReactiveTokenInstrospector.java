package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.ReactiveOpaqueTokenIntrospector;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

public class KeycloakReactiveTokenInstrospector implements ReactiveOpaqueTokenIntrospector {
    Logger logger = LoggerFactory.getLogger(KeycloakReactiveTokenInstrospector.class);

    private ReactiveOpaqueTokenIntrospector delegate;

    public KeycloakReactiveTokenInstrospector(ReactiveOpaqueTokenIntrospector delegate) {
        this.delegate = delegate;
    }

    public Mono<OAuth2AuthenticatedPrincipal> introspect(String token) {
        return this.delegate.introspect(token)
                .map(principal -> new DefaultOAuth2AuthenticatedPrincipal(
                        principal.getName(), principal.getAttributes(), extractAuthorities(principal)));
    }

    private Collection<GrantedAuthority> extractAuthorities(OAuth2AuthenticatedPrincipal principal) {
        Map<String, List<String>> realm_access = principal.getAttribute("realm_access");
        List<String> roles = realm_access.getOrDefault("roles", Collections.emptyList());
        List<GrantedAuthority> rolesAuthorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        Set<GrantedAuthority> allAuthorities = new HashSet<>();
        allAuthorities.addAll(principal.getAuthorities());
        allAuthorities.addAll(rolesAuthorities);

        return allAuthorities;
    }
}