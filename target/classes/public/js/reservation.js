
const API_URL = "/api/reservations";

document.addEventListener("DOMContentLoaded", () => {

    const form = document.getElementById("reservation-form");
    const reservationList = document.getElementById("reservation-list");
    const refreshButton = document.getElementById("refresh-reservations");
    const statusMessage = document.getElementById("reservation-message");
    const cancelEditButton = document.getElementById("cancel-edit");
    const submitButton = document.getElementById("submit-reservation");

    const editingHeading = document.getElementById("form-heading");

    let editingReservationId = null;
    let reservationsCache = [];
    let user;
    const bookingPanel = document.querySelector("[data-booking-panel]");
    const userIdInput = document.getElementById("userId");
    const columnCount = () => user.role === "ADMIN" ? 8 : 7;


    const bookingParams = new URLSearchParams(window.location.search);

    const selectedRoomId = bookingParams.get("roomId");
    const selectedCheckIn = bookingParams.get("checkIn");
    const selectedCheckOut = bookingParams.get("checkOut");
    const selectedGuests = bookingParams.get("guests");

    if (selectedRoomId) {
        document.getElementById("roomId").value = selectedRoomId;
    }

    if (selectedCheckIn) {
        document.getElementById("checkInDate").value = selectedCheckIn;
    }

    if (selectedCheckOut) {
        document.getElementById("checkOutDate").value = selectedCheckOut;
    }

    if (selectedGuests) {
        document.getElementById("guestCount").value = selectedGuests;
    }

    function showMessage(message, isError = false) {
        statusMessage.textContent = message;
        statusMessage.style.color = isError ? "#dc3545" : "#198754";
    }

    async function getErrorMessage(response) {
        try {
            const data = await response.json();
            return data.error || data.message ||
                `Request failed (${response.status})`;
        } catch {
            return `Request failed (${response.status})`;
        }
    }

    function updateStatistics() {
        document.getElementById("total-count").textContent =
            reservationsCache.length;

        document.getElementById("pending-count").textContent =
            reservationsCache.filter(r => r.status === "PENDING").length;

        document.getElementById("confirmed-count").textContent =
            reservationsCache.filter(r => r.status === "CONFIRMED").length;

        document.getElementById("cancelled-count").textContent =
            reservationsCache.filter(r => r.status === "CANCELLED").length;
    }

    function resetEditMode() {
        editingReservationId = null;

        form.reset();

        // Restore the authenticated user after resetting.
        userIdInput.value = user.id;
        bookingPanel.hidden = user.role !== "CUSTOMER";

        submitButton.textContent = "Create Reservation";
        editingHeading.textContent = "Create a Reservation";

        cancelEditButton.hidden = true;
    }

    function addCell(row, value) {
        const cell = document.createElement("td");
        cell.textContent = value ?? "";
        row.appendChild(cell);
        return cell;
    }

    function renderReservations() {
        reservationList.replaceChildren();

        if (reservationsCache.length === 0) {
            const row = document.createElement("tr");
            const cell = addCell(row, "No reservations found.");
            cell.colSpan = columnCount();
            cell.className = "empty-state";
            reservationList.appendChild(row);
            return;
        }

        reservationsCache.forEach(reservation => {

            const row = document.createElement("tr");

            addCell(row, reservation.id);
            if (user.role === "ADMIN") addCell(row, reservation.userId);
            addCell(row, reservation.roomId);
            addCell(row, reservation.checkInDate);
            addCell(row, reservation.checkOutDate);
            addCell(row, reservation.guestCount);

            const statusCell = addCell(row, "");

            const statusBadge = document.createElement("span");
            statusBadge.className =
                `status-badge status-${reservation.status}`;

            statusBadge.textContent = reservation.status;
            statusCell.appendChild(statusBadge);

            const actionsCell = document.createElement("td");

            const actions = document.createElement("div");
            actions.className = "table-action";

            const editButton = document.createElement("button");
            editButton.type = "button";
            editButton.textContent = "Edit";
            editButton.className = "btn edit-button";

            const cancelButton = document.createElement("button");
            cancelButton.type = "button";
            cancelButton.textContent = "Cancel";
            cancelButton.className = "btn cancel-button";

            const inactive =
                reservation.status === "CANCELLED" ||
                reservation.status === "COMPLETED";

            editButton.disabled = inactive;
            cancelButton.disabled = inactive;

            editButton.addEventListener("click", () => {
                editReservation(reservation);
            });

            cancelButton.addEventListener("click", () => {
                cancelReservation(reservation.id);
            });

            actions.append(editButton, cancelButton);

            if (user.role === "CUSTOMER" && !inactive) {
                const paymentLink = document.createElement("a");
                paymentLink.className = "btn payment-link";
                paymentLink.textContent = "Payment";
                paymentLink.href = "/payment.html?reservationId=" + encodeURIComponent(reservation.id);
                actions.appendChild(paymentLink);
            }
            actionsCell.appendChild(actions);
            row.appendChild(actionsCell);

            reservationList.appendChild(row);
        });
    }

    async function loadReservations() {
        refreshButton.disabled = true;
        showMessage("Loading reservations...");

        try {
            const response = await fetch(API_URL);

            if (!response.ok) {
                throw new Error(await getErrorMessage(response));
            }

            const data = await response.json();

            if (!Array.isArray(data)) {
                throw new Error("Invalid reservation data received.");
            }

            reservationsCache = data;

            renderReservations();
            updateStatistics();

            showMessage("Reservations loaded successfully.");

        } catch (error) {
            console.error("Load reservations error:", error);

            reservationList.replaceChildren();

            const row = document.createElement("tr");
            const cell = addCell(row, error.message);
            cell.colSpan = columnCount();
            cell.className = "empty-state";

            reservationList.appendChild(row);

            showMessage(error.message, true);

        } finally {
            refreshButton.disabled = false;
        }
    }

    function editReservation(reservation) {

        editingReservationId = reservation.id;
        bookingPanel.hidden = false;

        document.getElementById("userId").value =
            reservation.userId;

        document.getElementById("roomId").value =
            reservation.roomId;

        document.getElementById("checkInDate").value =
            reservation.checkInDate;

        document.getElementById("checkOutDate").value =
            reservation.checkOutDate;

        document.getElementById("guestCount").value =
            reservation.guestCount;

        submitButton.textContent = "Update Reservation";
        editingHeading.textContent = "Update Reservation";

        cancelEditButton.hidden = false;

        showMessage(`Editing reservation ${reservation.id}`);

        form.scrollIntoView({
            behavior: "smooth",
            block: "center"
        });
    }

    async function cancelReservation(id) {

        if (!confirm(
            "Are you sure you want to cancel this reservation?"
        )) {
            return;
        }

        try {
            showMessage("Cancelling reservation...");

            const response = await fetch(
                `${API_URL}/${encodeURIComponent(id)}/cancel`,
                { method: "PATCH" }
            );

            if (!response.ok) {
                throw new Error(await getErrorMessage(response));
            }

            showMessage("Reservation cancelled successfully.");

            await loadReservations();

        } catch (error) {
            console.error("Cancel error:", error);
            showMessage(error.message, true);
        }
    }

    form.addEventListener("submit", async event => {

        event.preventDefault();

        if (!user || (user.role !== "CUSTOMER" && !editingReservationId)) return;

        const reservation = {
            userId: user.id,
            roomId: document.getElementById("roomId").value.trim(),
            checkInDate: document.getElementById("checkInDate").value,
            checkOutDate: document.getElementById("checkOutDate").value,
            guestCount: Number(
                document.getElementById("guestCount").value
            )
        };

        if (
            !reservation.roomId ||
            !reservation.checkInDate ||
            !reservation.checkOutDate ||
            !Number.isInteger(reservation.guestCount) ||
            reservation.guestCount <= 0
        ) {
            showMessage("Please complete all fields correctly.", true);
            return;
        }

        const today = new Date();
        const todayString =
            `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, "0")}-${String(today.getDate()).padStart(2, "0")}`;

        if (reservation.checkInDate < todayString) {
            showMessage("Check-in date cannot be in the past.", true);
            return;
        }

        if (reservation.checkOutDate <= reservation.checkInDate) {
            showMessage("Check-out must be after check-in.", true);
            return;
        }

        const isEditing = editingReservationId !== null;

        let url = API_URL;
        let method = "POST";

        if (isEditing) {

            const existing = reservationsCache.find(
                item => item.id === editingReservationId
            );

            if (!existing) {
                showMessage("Reservation not found.", true);
                return;
            }

            url += `/${encodeURIComponent(editingReservationId)}`;
            method = "PUT";

            reservation.id = existing.id;
            reservation.userId = existing.userId;
            reservation.status = existing.status;
            reservation.createdAt = existing.createdAt;

        } else {

            // Current backend repository requires these fields.
            reservation.id = crypto.randomUUID();
            reservation.status = "PENDING";
            reservation.createdAt = new Date().toISOString();
        }

        try {

            submitButton.disabled = true;

            showMessage(
                isEditing
                    ? "Updating reservation..."
                    : "Creating reservation..."
            );

            const response = await fetch(url, {
                method,
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(reservation)
            });

            if (!response.ok) {
                throw new Error(await getErrorMessage(response));
            }


            const savedReservation = await response.json();

            if (!isEditing) {
                window.location.href =
                    "/payment.html?reservationId=" +
                    encodeURIComponent(savedReservation.id);
                return;
            }

            resetEditMode();


            showMessage(
                isEditing
                    ? "Reservation updated successfully."
                    : "Reservation created successfully."
            );

            await loadReservations();

        } catch (error) {

            console.error("Reservation operation error:", error);
            showMessage(error.message, true);

        } finally {
            submitButton.disabled = false;
        }
    });

    cancelEditButton.addEventListener("click", () => {
        resetEditMode();
        showMessage("Edit cancelled.");
    });

    document.getElementById("clear-form")
        .addEventListener("click", event => {

            event.preventDefault();

            resetEditMode();
            showMessage("Form cleared.");
        });

    refreshButton.addEventListener("click", loadReservations);

    GrandStaySession.ready.then(sessionUser => {
        if (!sessionUser) return;

        user = sessionUser;
        userIdInput.value = user.id;
        bookingPanel.hidden = user.role !== "CUSTOMER";
        loadReservations();
    });
});
