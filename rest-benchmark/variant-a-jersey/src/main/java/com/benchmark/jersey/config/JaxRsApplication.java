package com.benchmark.jersey.config;

import com.benchmark.jersey.repository.CategoryRepository;
import com.benchmark.jersey.repository.ItemRepository;
import com.benchmark.jersey.resource.CategoryResource;
import com.benchmark.jersey.resource.ItemResource;
import jakarta.persistence.EntityManagerFactory;
import jakarta.ws.rs.ApplicationPath;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/api")
public class JaxRsApplication extends ResourceConfig {

    public JaxRsApplication(EntityManagerFactory emf) {
        // Register Jackson JSON provider
        register(JacksonFeature.class);
        register(JacksonConfig.class);

        // Create repositories
        CategoryRepository categoryRepository = new CategoryRepository(emf);
        ItemRepository itemRepository = new ItemRepository(emf);

        // Bind repositories for injection
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(categoryRepository).to(CategoryRepository.class);
                bind(itemRepository).to(ItemRepository.class);
            }
        });

        // Register resource classes
        register(CategoryResource.class);
        register(ItemResource.class);

        // Enable CORS if needed
        register(new CorsFilter());
    }

    // Simple CORS filter
    private static class CorsFilter implements jakarta.ws.rs.container.ContainerResponseFilter {
        @Override
        public void filter(jakarta.ws.rs.container.ContainerRequestContext requestContext,
                           jakarta.ws.rs.container.ContainerResponseContext responseContext) {
            responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
            responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            responseContext.getHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        }
    }
}