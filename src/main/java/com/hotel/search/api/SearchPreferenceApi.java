
package com.hotel.search.api;

import com.hotel.common.ValidationException;
import com.hotel.search.model.SearchPreference;
import com.hotel.search.service.SearchPreferenceService;
import io.javalin.config.JavalinConfig;

public class SearchPreferenceApi {

    private final SearchPreferenceService preferenceService;

    public SearchPreferenceApi(
            SearchPreferenceService preferenceService) {

        this.preferenceService = preferenceService;
    }

    public void registerRoutes(JavalinConfig config) {

        // CREATE - Save a search preference
        config.routes.post("/api/search-preferences", ctx -> {

            SearchPreference preference =
                    ctx.bodyAsClass(SearchPreference.class);

            SearchPreference created =
                    preferenceService.createPreference(preference);

            ctx.status(201).json(created);
        });

        // READ - Retrieve all preferences for a user
        config.routes.get("/api/search-preferences", ctx -> {

            String userId = requireUserId(ctx.queryParam("userId"));

            ctx.json(
                    preferenceService.getPreferencesByUser(userId)
            );
        });

        // READ - Retrieve a preference by ID
        config.routes.get("/api/search-preferences/{id}", ctx -> {

            String id = ctx.pathParam("id");
            String userId = requireUserId(ctx.queryParam("userId"));

            ctx.json(
                    preferenceService.getPreferenceById(id, userId)
            );
        });

        // UPDATE - Update a preference
        config.routes.put("/api/search-preferences/{id}", ctx -> {

            String id = ctx.pathParam("id");
            String userId = requireUserId(ctx.queryParam("userId"));

            SearchPreference updated =
                    ctx.bodyAsClass(SearchPreference.class);

            ctx.json(
                    preferenceService.updatePreference(
                            id, userId, updated
                    )
            );
        });

        // DELETE - Delete a preference
        config.routes.delete("/api/search-preferences/{id}", ctx -> {

            String id = ctx.pathParam("id");
            String userId = requireUserId(ctx.queryParam("userId"));

            preferenceService.deletePreference(id, userId);

            ctx.status(204);
        });
    }

    private String requireUserId(String userId) {

        if (userId == null || userId.isBlank()) {
            throw new ValidationException(
                    "User ID is required.");
        }

        return userId;
    }
}
