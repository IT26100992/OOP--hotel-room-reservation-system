
document.addEventListener("DOMContentLoaded", () => {

    const statusElement = document.getElementById("api-status");

    async function checkBackend() {

        if (!statusElement) {
            console.error("API status element not found.");
            return;
        }

        statusElement.textContent = "Checking backend connection...";
        statusElement.style.color = "#555";

        try {
            const response = await fetch("/api/health");

            if (!response.ok) {
                throw new Error(
                    `Backend error: HTTP ${response.status}`
                );
            }

            const result = await response.json();

            if (result.status !== "ok") {
                throw new Error("Backend health check failed.");
            }

            statusElement.textContent =
                "✓ " + result.message;

            statusElement.style.color = "green";

        } catch (error) {

            statusElement.textContent =
                "✗ Could not connect to the backend.";

            statusElement.style.color = "red";

            console.error("Backend connection error:", error);
        }
    }

    checkBackend();

});
