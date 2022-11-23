package com.example.demo;
//
//import org.keycloak.adapters.KeycloakConfigResolver;
//import org.keycloak.adapters.KeycloakDeployment;
//import org.keycloak.adapters.KeycloakDeploymentBuilder;
//import org.keycloak.adapters.OIDCHttpFacade;
//import org.keycloak.representations.adapters.config.AdapterConfig;
//
//import java.io.InputStream;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//public class HeaderBasedKeycloakConfigResolver implements KeycloakConfigResolver {
//    private final Map<String, KeycloakDeployment> cache = new ConcurrentHashMap<>();
//
//    private static AdapterConfig adapterConfig;
//
//    @Override
//    public KeycloakDeployment resolve(OIDCHttpFacade.Request request) {
//        String realm = request.getHeader("realm");
//        if (!cache.containsKey(realm)) {
//            InputStream is = getClass().getResourceAsStream("/" + realm + "-keycloak.json");
//            cache.put(realm, KeycloakDeploymentBuilder.build(is));
//        }
//
//        return cache.get(realm);
//    }
//}
