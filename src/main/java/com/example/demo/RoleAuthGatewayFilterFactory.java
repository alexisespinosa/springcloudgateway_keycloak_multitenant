package com.example.demo;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
// ToDo add Ordered interface with -1
public class RoleAuthGatewayFilterFactory extends
        AbstractGatewayFilterFactory<RoleAuthGatewayFilterFactory.Config> {

    Logger logger = LoggerFactory.getLogger(RoleAuthGatewayFilterFactory.class);

    public RoleAuthGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
//    ToDo Add tests
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            return exchange.getPrincipal().cast(BearerTokenAuthentication.class).flatMap(bearerTokenAuthentication -> {
                List<RoleMethods> authorizedRoles = this.transformRolesMethods(config.getRolesMethods());
                List<String> userRoles = bearerTokenAuthentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).toList();

                RoleMethods validRole = authorizedRoles.stream()
                        .filter(
                                roleMethods ->
                                        userRoles.contains(roleMethods.getRole()))
                        .findAny()
                        .orElse(null);

                if (validRole == null || !validRole.getMethods().contains(exchange.getRequest().getMethod())) {
                    var response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);

//                    ToDo Log error

                    return response.setComplete();
                }

//                ToDo Log debug success access

                return chain.filter(exchange);
            });
        };
    }

// ToDo Review if I can use a converter instead of this method
    private List<RoleMethods> transformRolesMethods(List<String> rolesMethods) {List<RoleMethods> roles = new ArrayList<>();

        for (String roleMethod: rolesMethods) {
            RoleMethods role = new RoleMethods();
            String[] splittedRoleFromMethods = roleMethod.split(" ");
            String[] splittedMethods = splittedRoleFromMethods[1].split("-");

            role.setRole(splittedRoleFromMethods[0]);

            for (String method: splittedMethods) {
                role.addMethod(HttpMethod.valueOf(method));
            }

            roles.add(role);
        }

//        ToDo Validate roles have at least one role with one method

        return roles;
    }

    private class RoleMethods {
        private String role;
        private List<HttpMethod> methods = new ArrayList<>();

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public List<HttpMethod> getMethods() {
            return methods;
        }

        public void addMethod(HttpMethod method) {
            this.methods.add(method);
        }
    }

    @Data
    public static class Config {
        private List<String> rolesMethods;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        // we need this to use shortcuts in the application.yml
        return Arrays.asList("rolesMethods");
    }
}