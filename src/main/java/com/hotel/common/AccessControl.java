
package com.hotel.common;

import com.hotel.user.model.User;
import com.hotel.user.model.UserRole;
import com.hotel.user.repository.UserRepository;
import io.javalin.http.Context;

public class AccessControl {

    private final UserRepository userRepository;

    public AccessControl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Check whether the request has a valid login session.
    public User requireLogin(Context ctx) {

        var session = ctx.req().getSession(false);

        if (session == null) {
            throw new AppException(
                    "Please log in first.", 401);
        }

        Object sessionUserId =
                session.getAttribute("userId");

        if (!(sessionUserId instanceof String userId)
                || userId.isBlank()) {
            throw new AppException(
                    "Please log in first.", 401);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new AppException(
                                "Invalid login session.", 401));

        if (!user.isActive()) {
            session.invalidate();
            throw new AppException(
                    "Account is inactive.", 401);
        }

        return user;
    }

    // Allow administrators only.
    public User requireAdmin(Context ctx) {

        User user = requireLogin(ctx);

        if (user.getRole() != UserRole.ADMIN) {
            throw new AppException(
                    "Admin access required.", 403);
        }

        return user;
    }

    // Allow customers only.
    public User requireCustomer(Context ctx) {

        User user = requireLogin(ctx);

        if (user.getRole() != UserRole.CUSTOMER) {
            throw new AppException(
                    "Customer access required.", 403);
        }

        return user;
    }

    // Allow the record owner or an administrator.
    public User requireOwnerOrAdmin(
            Context ctx, String ownerId) {

        User user = requireLogin(ctx);

        if (user.getRole() != UserRole.ADMIN
                && !user.getId().equals(ownerId)) {

            throw new AppException(
                    "Access denied.", 403);
        }

        return user;
    }
}
