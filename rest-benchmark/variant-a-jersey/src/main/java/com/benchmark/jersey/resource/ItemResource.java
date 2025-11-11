package com.benchmark.jersey.resource;

import com.benchmark.entity.Category;
import com.benchmark.entity.Item;
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
@Path("/items")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ItemResource {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;

    @Inject
    public ItemResource(ItemRepository itemRepository, CategoryRepository categoryRepository) {
        this.itemRepository = itemRepository;
        this.categoryRepository = categoryRepository;
    }

    @GET
    public Response getItems(
            @QueryParam("categoryId") Long categoryId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        List<Item> items;
        long total;

        if (categoryId != null) {
            items = itemRepository.findByCategoryId(categoryId, page, size);
            total = itemRepository.countByCategoryId(categoryId);
        } else {
            items = itemRepository.findAll(page, size);
            total = itemRepository.count();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("content", items.stream().map(this::toResponse).toList());
        response.put("page", page);
        response.put("size", size);
        response.put("totalElements", total);
        response.put("totalPages", (total + size - 1) / size);

        return Response.ok(response).build();
    }

    @GET
    @Path("/{id}")
    public Response getItem(@PathParam("id") Long id) {
        return itemRepository.findById(id)
                .map(item -> Response.ok(toResponse(item)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    public Response createItem(Map<String, Object> payload) {
        try {
            Item item = new Item();
            item.setSku((String) payload.get("sku"));
            item.setName((String) payload.get("name"));
            item.setPrice(new java.math.BigDecimal(payload.get("price").toString()));
            item.setStock(((Number) payload.get("stock")).intValue());
            item.setDescription((String) payload.get("description"));

            Long categoryId = ((Number) payload.get("categoryId")).longValue();
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            item.setCategory(category);

            Item saved = itemRepository.save(item);
            return Response.status(Response.Status.CREATED).entity(toResponse(saved)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateItem(@PathParam("id") Long id, Map<String, Object> payload) {
        return itemRepository.findById(id)
                .map(existing -> {
                    try {
                        existing.setSku((String) payload.get("sku"));
                        existing.setName((String) payload.get("name"));
                        existing.setPrice(new java.math.BigDecimal(payload.get("price").toString()));
                        existing.setStock(((Number) payload.get("stock")).intValue());
                        existing.setDescription((String) payload.get("description"));

                        if (payload.containsKey("categoryId")) {
                            Long categoryId = ((Number) payload.get("categoryId")).longValue();
                            Category category = categoryRepository.findById(categoryId)
                                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
                            existing.setCategory(category);
                        }

                        Item updated = itemRepository.save(existing);
                        return Response.ok(toResponse(updated)).build();
                    } catch (Exception e) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity(Map.of("error", e.getMessage()))
                                .build();
                    }
                })
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @DELETE
    @Path("/{id}")
    public Response deleteItem(@PathParam("id") Long id) {
        return itemRepository.findById(id)
                .map(item -> {
                    itemRepository.delete(id);
                    return Response.noContent().build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    private Map<String, Object> toResponse(Item item) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", item.getId());
        result.put("sku", item.getSku());
        result.put("name", item.getName());
        result.put("description", item.getDescription());
        result.put("price", item.getPrice());
        result.put("stock", item.getStock());
        result.put("updatedAt", item.getUpdatedAt());

        Category category = item.getCategory();
        if (category != null) {
            Map<String, Object> categoryMap = new HashMap<>();
            categoryMap.put("id", category.getId());
            categoryMap.put("code", category.getCode());
            categoryMap.put("name", category.getName());
            result.put("category", categoryMap);
        }

        return result;
    }
}