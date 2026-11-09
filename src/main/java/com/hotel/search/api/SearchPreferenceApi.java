
package com.hotel.search.api;

import com.hotel.common.AccessControl;
import com.hotel.search.model.SearchPreference;
import com.hotel.search.service.SearchPreferenceService;
import com.hotel.user.model.User;
import io.javalin.config.JavalinConfig;

public class SearchPreferenceApi {

    private final SearchPreferenceService preferenceService;
    private final AccessControl accessControl;

    public SearchPreferenceApi(
            SearchPreferenceService preferenceService,
            AccessControl accessControl) {

        this.preferenceService = preferenceService;
        this.accessControl = accessControl;
    }

    public void registerRoutes(JavalinConfig config) {

        // CREATE - Save a search for logged-in customer.
        config.routes.post("/api/search-preferences", ctx -> {

            User customer = accessControl.requireCustomer(ctx);

            SearchPreference preference =
                    ctx.bodyAsClass(SearchPreference.class);

            // Set ownership from the authenticated session.
            // Never trust a userId sent by the frontend.
            preference.setUserId(customer.getId());

            SearchPreference created =
                    preferenceService.createPreference(preference);

            ctx.status(201).json(created);
        });

        // READ - Get my saved searches.
        config.routes.get("/api/search-preferences", ctx -> {

            User customer = accessControl.requireCustomer(ctx);

            ctx.json(
                    preferenceService.getPreferencesByUser(
                            customer.getId()
                    )
            );
        });

        // READ - Get one of my saved searches.
        config.routes.get("/api/search-preferences/{id}", ctx -> {

            User customer = accessControl.requireCustomer(ctx);

            String id = ctx.pathParam("id");

            ctx.json(
                    preferenceService.getPreferenceById(
                            id,
                            customer.getId()
                    )
            );
        });

        // UPDATE - Modify my saved search.
        config.routes.put("/api/search-preferences/{id}", ctx -> {

            User customer = accessControl.requireCustomer(ctx);

            String id = ctx.pathParam("id");

            SearchPreference updated =
                    ctx.bodyAsClass(SearchPreference.class);

            ctx.json(
                    preferenceService.updatePreference(
                            id,
                            customer.getId(),
                            updated
                    )
            );
        });

        // DELETE - Delete my saved search.
        config.routes.delete("/api/search-preferences/{id}", ctx -> {

            User customer = accessControl.requireCustomer(ctx);

            String id = ctx.pathParam("id");

            preferenceService.deletePreference(
                    id,
                    customer.getId()
            );

            ctx.status(204);
        });
    }
}
