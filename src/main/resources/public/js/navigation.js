
document.addEventListener("DOMContentLoaded", async () => {

    const authLinks =
        document.querySelectorAll(".auth-only");

    const guestLinks =
        document.querySelectorAll(".guest-only");

    const adminLinks =
        document.querySelectorAll(".admin-only");

    try {
        const response = await fetch("/api/auth/me", {
            credentials: "same-origin"
        });

        if (response.ok) {
            const user = await response.json();

            // Hide Sign In after successful authentication.
            guestLinks.forEach(element => {
                element.hidden = true;
            });

            // Show customer/account navigation.
            authLinks.forEach(element => {
                element.hidden = false;
            });

            // Only administrators see management links.
            if (user.role === "ADMIN") {
                adminLinks.forEach(element => {
                    element.hidden = false;
                });
            }
        }

    } catch (error) {
        console.error("Session check failed:", error);
    }

    const logoutButton =
        document.getElementById("navLogout");

    if (logoutButton) {
        logoutButton.addEventListener("click", async () => {
            try {
                const response = await fetch(
                    "/api/auth/logout",
                    {
                        method: "POST",
                        credentials: "same-origin"
                    }
                );

                if (!response.ok) {
                    throw new Error("Logout failed.");
                }

                window.location.href = "/login.html";

            } catch (error) {
                alert("Unable to log out. Please try again.");
            }
        });
    }
});
