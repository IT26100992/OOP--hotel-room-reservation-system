
package com.hotel.user.api;

import com.hotel.user.service.UserService;
import io.javalin.http.UnauthorizedResponse;
import com.hotel.common.ValidationException;
import com.hotel.user.dto.RegisterRequest;
import com.hotel.user.dto.UserResponse;
import com.hotel.user.service.AuthenticationService;
import io.javalin.config.JavalinConfig;
import com.hotel.user.dto.LoginRequest;
import com.hotel.user.model.User;
import com.hotel.user.dto.UpdateProfileRequest;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;

public class AuthApi {


    private final AuthenticationService authenticationService;
    private final UserService userService;

    public AuthApi(
            AuthenticationService authenticationService,
            UserService userService) {

        this.authenticationService = authenticationService;
        this.userService = userService;
    }


    public void registerRoutes(JavalinConfig config) {

        config.routes.post("/api/auth/register", ctx -> {
            System.out.println("DEBUG: Registration endpoint reached");
            RegisterRequest request;


            try {
                request = ctx.bodyAsClass(RegisterRequest.class);

            } catch (Exception e) {
                throw new ValidationException(
                        "Invalid registration JSON. Only fullName, email, " +
                                "password, and phone are accepted."
                );
            }

            System.out.println("DEBUG: Calling AuthenticationService");

            UserResponse registeredUser =
                    authenticationService.register(request);

            System.out.println("DEBUG: Registration service completed");

            ctx.status(201).json(registeredUser);
        });


// LOGIN - Authenticate a customer
        config.routes.post("/api/auth/login", ctx -> {

            LoginRequest request;

            try {
                request = ctx.bodyAsClass(LoginRequest.class);
            } catch (Exception e) {
                throw new ValidationException(
                        "Invalid login JSON request."
                );
            }

            // Verify credentials
            User user = authenticationService.login(request);

            // Create or reuse an HTTP session and rotate its ID
            var session = ctx.req().getSession(true);
            ctx.req().changeSessionId();

            // Store the verified identity server-side
            session.setAttribute("userId", user.getId());

            // Return account details without the password hash
            ctx.status(200).json(new UserResponse(user));
        });


// CURRENT USER - Check the authenticated session
        config.routes.get("/api/auth/me", ctx -> {

            var session = ctx.req().getSession(false);

            if (session == null) {
                throw new UnauthorizedResponse(
                        "Please log in first.");
            }

            String userId =
                    (String) session.getAttribute("userId");

            if (userId == null) {
                throw new UnauthorizedResponse(
                        "Please log in first.");
            }

            User user = userService.getUserById(userId);

            if (!user.isActive()) {
                session.invalidate();
                throw new UnauthorizedResponse(
                        "Please log in again.");
            }

            ctx.json(new UserResponse(user));
        });


// LOGOUT - End the authenticated session
        config.routes.post("/api/auth/logout", ctx -> {

            var session = ctx.req().getSession(false);

            if (session != null) {
                session.invalidate();
            }

            ctx.status(204);
        });


// UPDATE - Modify the logged-in customer's profile
        config.routes.put("/api/users/me", ctx -> {

            // Identify the customer using the session
            String userId = requireAuthenticatedUserId(ctx);

            UpdateProfileRequest request;

            try {
                request = ctx.bodyAsClass(UpdateProfileRequest.class);
            } catch (Exception e) {
                throw new ValidationException(
                        "Invalid profile JSON. Only fullName, " +
                                "email and phone are accepted.");
            }

            UserResponse updatedUser =
                    userService.updateOwnProfile(userId, request);

            ctx.status(200).json(updatedUser);
        });


// DELETE - Soft-delete the logged-in customer's account
        config.routes.delete("/api/users/me", ctx -> {

            String userId = requireAuthenticatedUserId(ctx);

            // Deactivate the account without deleting its history
            userService.deactivateUser(userId);

            // End the login session immediately
            var session = ctx.req().getSession(false);

            if (session != null) {
                session.invalidate();
            }

            ctx.status(204);
        });



    }


    private String requireAuthenticatedUserId(Context ctx) {

        // Do not create a new session when checking authentication
        var session = ctx.req().getSession(false);

        if (session == null) {
            throw new UnauthorizedResponse(
                    "Please log in first.");
        }

        Object userId = session.getAttribute("userId");

        if (!(userId instanceof String id) || id.isBlank()) {
            throw new UnauthorizedResponse(
                    "Please log in first.");
        }

        // Check that the user still exists and is active
        User user;

        try {
            user = userService.getUserById(id);
        } catch (com.hotel.common.ResourceNotFoundException e) {
            session.invalidate();
            throw new UnauthorizedResponse(
                    "Please log in again.");
        }

        if (!user.isActive()) {
            session.invalidate();
            throw new UnauthorizedResponse(
                    "Please log in again.");
        }

        return id;
    }


}
