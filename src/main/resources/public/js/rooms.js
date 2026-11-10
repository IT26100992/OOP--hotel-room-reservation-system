"use strict";

// =====================================
// Room Management - Frontend Controller
// =====================================

const API_URL = "/api/rooms";

// HTML elements
const roomsTableBody =
    document.getElementById("roomsTableBody");

const totalRooms =
    document.getElementById("totalRooms");

const availableRooms =
    document.getElementById("availableRooms");

const maintenanceRooms =
    document.getElementById("maintenanceRooms");

const inactiveRooms =
    document.getElementById("inactiveRooms");

const addRoomButton =
    document.getElementById("addRoomButton");

const refreshRoomsButton =
    document.getElementById("refreshRoomsButton");

const searchRoomInput =
    document.getElementById("searchRoomInput");

const statusFilter =
    document.getElementById("statusFilter");

const roomFormSection =
    document.getElementById("roomFormSection");

const roomFormTitle =
    document.getElementById("roomFormTitle");

const roomForm =
    document.getElementById("roomForm");

const saveRoomButton =
    document.getElementById("saveRoomButton");

const closeFormButton =
    document.getElementById("closeFormButton");

const cancelFormButton =
    document.getElementById("cancelFormButton");

const roomMessage =
    document.getElementById("roomMessage");

// Store rooms loaded from the backend
let rooms = [];

// null means creating a new room
// Otherwise, the value is the room ID being edited
let editingRoomId = null;


// =====================================
// Utility Functions
// =====================================

function showMessage(message, type = "") {
    roomMessage.textContent = message;
    roomMessage.className =
        type ? `notification ${type}` : "notification";
}

function formatPrice(price) {
    return new Intl.NumberFormat("en-LK", {
        style: "currency",
        currency: "LKR",
        minimumFractionDigits: 2
    }).format(price);
}

function formatRoomType(type) {
    const labels = {
        SINGLE: "Single",
        DOUBLE: "Double",
        TWIN: "Twin",
        DELUXE: "Deluxe",
        SUITE: "Suite"
    };

    return labels[type] || type;
}

// Safely create a table cell without innerHTML
function createCell(value) {
    const cell = document.createElement("td");
    cell.textContent = value ?? "";
    return cell;
}

// Parse API error messages consistently
async function getErrorMessage(response) {
    try {
        const data = await response.json();
        return data.error || `Request failed (${response.status}).`;
    } catch {
        return `Request failed (${response.status}).`;
    }
}


// =====================================
// Load Rooms
// =====================================

async function loadRooms() {
    refreshRoomsButton.disabled = true;

    try {
        const response = await fetch(API_URL, {
            credentials: "same-origin"
        });

        if (!response.ok) {
            throw new Error(await getErrorMessage(response));
        }

        const data = await response.json();

        if (!Array.isArray(data)) {
            throw new Error("Invalid room data received.");
        }

        rooms = data;

        updateStatistics();
        renderRooms();

    } catch (error) {
        showMessage(
            error.message || "Unable to load rooms.",
            "error"
        );

        roomsTableBody.replaceChildren();

        const row = document.createElement("tr");
        const cell = createCell("Unable to load rooms.");
        cell.colSpan = 6;
        cell.className = "empty-state";

        row.appendChild(cell);
        roomsTableBody.appendChild(row);

    } finally {
        refreshRoomsButton.disabled = false;
    }
}


// =====================================
// Dashboard Statistics
// =====================================

function updateStatistics() {
    totalRooms.textContent = rooms.length;

    availableRooms.textContent = rooms.filter(
        room => room.status === "AVAILABLE"
    ).length;

    maintenanceRooms.textContent = rooms.filter(
        room => room.status === "MAINTENANCE"
    ).length;

    inactiveRooms.textContent = rooms.filter(
        room => room.status === "INACTIVE"
    ).length;
}


// =====================================
// Render Room Table
// =====================================

function renderRooms() {
    roomsTableBody.replaceChildren();

    const searchTerm =
        searchRoomInput.value.trim().toLowerCase();

    const selectedStatus = statusFilter.value;

    const filteredRooms = rooms.filter(room => {
        const matchesNumber =
            String(room.roomNumber)
                .toLowerCase()
                .includes(searchTerm);

        const matchesStatus =
            selectedStatus === "ALL" ||
            room.status === selectedStatus;

        return matchesNumber && matchesStatus;
    });

    if (filteredRooms.length === 0) {
        const row = document.createElement("tr");

        const cell = createCell("No rooms found.");
        cell.colSpan = 6;
        cell.className = "empty-state";

        row.appendChild(cell);
        roomsTableBody.appendChild(row);
        return;
    }

    filteredRooms.forEach(room => {
        const row = document.createElement("tr");

        row.appendChild(createCell(room.roomNumber));

        row.appendChild(
            createCell(formatRoomType(room.roomType))
        );

        row.appendChild(
            createCell(formatPrice(room.pricePerNight))
        );

        row.appendChild(
            createCell(room.capacity)
        );

        // Room status badge
        const statusCell = document.createElement("td");

        const badge = document.createElement("span");
        badge.className =
            "status-badge status-" +
            room.status.toLowerCase();

        badge.textContent = room.status;

        statusCell.appendChild(badge);
        row.appendChild(statusCell);

        // Edit/Delete actions
        const actionCell = document.createElement("td");

        const editButton = document.createElement("button");
        editButton.type = "button";
        editButton.className = "table-action";
        editButton.textContent = "Edit";

        editButton.addEventListener("click", () => {
            openEditForm(room.id);
        });

        const deleteButton = document.createElement("button");
        deleteButton.type = "button";
        deleteButton.className = "table-action delete";
        deleteButton.textContent = "Delete";

        deleteButton.addEventListener("click", () => {
            deleteRoom(room.id);
        });

        actionCell.appendChild(editButton);
        actionCell.appendChild(deleteButton);

        row.appendChild(actionCell);
        roomsTableBody.appendChild(row);
    });
}


// =====================================
// Add / Edit Form
// =====================================

function showForm() {
    roomFormSection.hidden = false;

    roomFormSection.scrollIntoView({
        behavior: "smooth",
        block: "start"
    });
}

function hideForm() {
    roomFormSection.hidden = true;
    roomForm.reset();
    editingRoomId = null;
}

function openAddForm() {
    editingRoomId = null;

    roomForm.reset();
    roomFormTitle.textContent = "Add New Room";
    saveRoomButton.textContent = "Create Room";

    document.getElementById("status").value =
        "AVAILABLE";

    showMessage("");
    showForm();
}

function openEditForm(roomId) {
    const room = rooms.find(item => item.id === roomId);

    if (!room) {
        showMessage("Room not found.", "error");
        return;
    }

    editingRoomId = room.id;

    document.getElementById("roomNumber").value =
        room.roomNumber;

    document.getElementById("roomType").value =
        room.roomType;

    document.getElementById("pricePerNight").value =
        room.pricePerNight;

    document.getElementById("capacity").value =
        room.capacity;

    document.getElementById("status").value =
        room.status;

    document.getElementById("description").value =
        room.description || "";

    roomFormTitle.textContent =
        `Edit Room ${room.roomNumber}`;

    saveRoomButton.textContent = "Save Changes";

    showMessage("");
    showForm();
}


// =====================================
// Create / Update Room
// =====================================

roomForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const roomData = {
        roomNumber:
            document.getElementById("roomNumber").value.trim(),

        roomType:
        document.getElementById("roomType").value,

        pricePerNight:
            Number(document.getElementById("pricePerNight").value),

        capacity:
            Number(document.getElementById("capacity").value),

        status:
        document.getElementById("status").value,

        description:
            document.getElementById("description").value.trim()
    };

    const isEditing = editingRoomId !== null;

    const url = isEditing
        ? `${API_URL}/${encodeURIComponent(editingRoomId)}`
        : API_URL;

    const method = isEditing ? "PUT" : "POST";

    saveRoomButton.disabled = true;

    saveRoomButton.textContent =
        isEditing ? "Saving..." : "Creating...";

    try {
        const response = await fetch(url, {
            method,
            credentials: "same-origin",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(roomData)
        });

        if (!response.ok) {
            throw new Error(await getErrorMessage(response));
        }

        const message = isEditing
            ? "Room updated successfully!"
            : "Room created successfully!";

        hideForm();
        await loadRooms();

        showMessage(message, "success");

    } catch (error) {
        showMessage(
            error.message || "Unable to save room.",
            "error"
        );
    } finally {
        saveRoomButton.disabled = false;

        saveRoomButton.textContent =
            editingRoomId === null
                ? "Create Room"
                : "Save Changes";
    }
});


// =====================================
// Delete Room
// =====================================

async function deleteRoom(roomId) {
    const room = rooms.find(item => item.id === roomId);

    if (!room) {
        showMessage("Room not found.", "error");
        return;
    }

    const confirmed = window.confirm(
        `Are you sure you want to delete Room ${room.roomNumber}?`
    );

    if (!confirmed) {
        return;
    }

    try {
        const response = await fetch(
            `${API_URL}/${encodeURIComponent(roomId)}`,
            {
                method: "DELETE",
                credentials: "same-origin"
            }
        );

        if (!response.ok) {
            throw new Error(await getErrorMessage(response));
        }

        await loadRooms();

        showMessage(
            `Room ${room.roomNumber} deleted successfully!`,
            "success"
        );

    } catch (error) {
        showMessage(
            error.message || "Unable to delete room.",
            "error"
        );
    }
}


// =====================================
// Event Listeners
// =====================================

addRoomButton.addEventListener("click", openAddForm);

refreshRoomsButton.addEventListener("click", loadRooms);

searchRoomInput.addEventListener("input", renderRooms);

statusFilter.addEventListener("change", renderRooms);

closeFormButton.addEventListener("click", hideForm);

cancelFormButton.addEventListener("click", hideForm);


// =====================================
// Initialize Dashboard
// =====================================

GrandStaySession.ready.then(user => {
    if (user?.role === "ADMIN") loadRooms();
});
