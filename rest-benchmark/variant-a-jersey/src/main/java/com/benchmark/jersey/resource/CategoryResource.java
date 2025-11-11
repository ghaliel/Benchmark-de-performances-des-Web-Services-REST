package com.benchmark.jersey.resource;

import com.benchmark.entity.Category;
import com.benchmark.jersey.repository.CategoryRepository;
import com.benchmark.jersey.repository.ItemRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoryResource {

    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;

    @Inject
    public CategoryResource(CategoryRepository categoryRepository, ItemRepository itemRepository) {
        this.categoryRepository = categoryRepository;
        this.itemRepository = itemRepository;
    }

    @GET
    public Response getCategories(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        List<Category> categories = categoryRepository.findAll(page, size);
        long total = categoryRepository.count();

        Map<String, Object> response = new HashMap<>();
        response.put("content", categories);
        response.put("page", page);
        response.put("size", size);
        response.put("totalElements", total);
        response.put("totalPages", (total + size - 1) / size);

        return Response.ok(response).build();
    }

    @GET
    @Path("/{id}")
    public Response getCategory(@PathParam("id") Long id) {
        return categoryRepository.findById(id)
                .map(category -> Response.ok(category).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/{id}/items")
    public Response getCategoryItems(
            @PathParam("id") Long id,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        if (!categoryRepository.existsById(id)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        List<com.benchmark.entity.Item> items = itemRepository.findByCategoryId(id, page, size);
        long total = itemRepository.countByCategoryId(id);

        Map<String, Object> response = new HashMap<>();
        response.put("content", items);
        response.put("page", page);
        response.put("size", size);
        response.put("totalElements", total);
        response.put("totalPages", (total + size - 1) / size);

        return Response.ok(response).build();
    }

    @POST
    public Response createCategory(Category category) {
        try {
            Category saved = categoryRepository.save(category);
            return Response.status(Response.Status.CREATED).entity(saved).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateCategory(@PathParam("id") Long id, Category category) {
        return categoryRepository.findById(id)
                .map(existing -> {
                    category.setId(id);
                    Category updated = categoryRepository.save(category);
                    return Response.ok(updated).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @DELETE
    @Path("/{id}")
    public Response deleteCategory(@PathParam("id") Long id) {
        return categoryRepository.findById(id)
                .map(category -> {
                    categoryRepository.delete(id);
                    return Response.noContent().build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
}