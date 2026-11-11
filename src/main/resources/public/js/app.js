async function checkBookingService() {
    const status = document.getElementById("api-status");

    try {
        const response = await fetch("/api/health", { cache: "no-store" });
        if (!response.ok || (await response.json()).status !== "ok") {
            throw new Error("Booking service unavailable");
        }

        status.textContent = "Booking service online";
    } catch {
        status.textContent = "Booking service unavailable. Try again shortly.";
    }
}

checkBookingService();
