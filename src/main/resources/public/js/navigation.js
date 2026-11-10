const GrandStaySession = (() => {
    const returnPages = new Set([
        "/search.html", "/reservation.html", "/payment.html", "/receipt.html",
        "/profile.html", "/rooms.html", "/admin.html"
    ]);

    function signInUrl(destination = location.pathname + location.search + location.hash) {
        return "/login.html?" + new URLSearchParams({ next: destination });
    }

    function afterLogin(role) {
        const next = new URLSearchParams(location.search).get("next");

        const path = next?.split(/[?#]/, 1)[0];
        const adminPage = path === "/rooms.html" || path === "/admin.html";

        if (returnPages.has(path) && (!adminPage || role === "ADMIN")) {
            return next;
        }

        return role === "ADMIN" ? "/admin.html" : "/search.html";
    }

    function updateVisibility(selector, visible) {
        document.querySelectorAll(selector).forEach(element => {
            element.hidden = !visible;
        });
    }

    async function loadSession() {
        const requiredRole = document.body.dataset.requires;
        const status = document.querySelector("[data-session-status]");

        try {
            const response = await fetch("/api/auth/me", {
                credentials: "same-origin",
                cache: "no-store"
            });

            if (response.status === 401) {
                if (requiredRole) location.replace(signInUrl());
                return null;
            }

            if (!response.ok) throw new Error("Could not verify your session.");

            const user = await response.json();
            updateVisibility(".guest-only", false);
            updateVisibility(".auth-only", true);
            updateVisibility(".admin-only", user.role === "ADMIN");
            updateVisibility(".customer-only", user.role === "CUSTOMER");

            if (requiredRole === "ADMIN" && user.role !== "ADMIN") {
                status.textContent = "Only administrators can access this page.";
            } else if (requiredRole) {
                status.parentElement.hidden = true;
                updateVisibility("[data-session-content]", true);
            }

            return user;
        } catch {
            if (status) status.textContent = "Unable to verify your access. Refresh the page to try again.";
            return null;
        }
    }

    document.querySelector("[data-logout]")?.addEventListener("click", async event => {
        const button = event.currentTarget;
        button.disabled = true;

        try {
            const response = await fetch("/api/auth/logout", { method: "POST" });
            if (!response.ok) throw new Error("Logout failed.");
            location.replace("/login.html");
        } catch {
            button.disabled = false;
            window.alert("Unable to log out. Please try again.");
        }
    });

    return { ready: loadSession(), signInUrl, afterLogin };
})();
