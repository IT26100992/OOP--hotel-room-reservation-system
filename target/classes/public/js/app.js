
async function checkBackend() {
    const statusElement = document.getElementById("api-status");

    try {
        const response = await fetch("/api/health");

        if (!response.ok) {
            throw new Error("Backend returned an error");
        }

        const result = await response.json();
        statusElement.textContent = result.message;
    } catch (error) {
        statusElement.textContent = "Could not connect to the backend.";
        console.error(error);
    }
}

checkBackend();
