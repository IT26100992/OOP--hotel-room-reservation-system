
const searchForm = document.getElementById("searchForm");
const roomResults = document.getElementById("roomResults");
const message = document.getElementById("message");
const searchButton = searchForm.querySelector("button[type='submit']");

searchForm.addEventListener("submit", async function (event) {

    // Prevent the browser from reloading the page
    event.preventDefault();

    const checkIn = document.getElementById("checkIn").value;
    const checkOut = document.getElementById("checkOut").value;
    const guests = document.getElementById("guests").value;
    const roomType = document.getElementById("roomType").value;
    const minPrice = document.getElementById("minPrice").value;
    const maxPrice = document.getElementById("maxPrice").value;

    // Client-side validation
    if (checkOut <= checkIn) {
        message.textContent =
            "Check-out date must be after check-in date.";
        roomResults.replaceChildren();
        return;
    }

    if (minPrice !== "" && maxPrice !== "" &&
        Number(minPrice) > Number(maxPrice)) {
        message.textContent =
            "Minimum price cannot exceed maximum price.";
        roomResults.replaceChildren();
        return;
    }

    // Construct the query string
    const params = new URLSearchParams({
        checkIn: checkIn,
        checkOut: checkOut,
        guests: guests
    });

    if (roomType) {
        params.append("roomType", roomType);
    }

    if (minPrice !== "") {
        params.append("minPrice", minPrice);
    }

    if (maxPrice !== "") {
        params.append("maxPrice", maxPrice);
    }

    searchButton.disabled = true;
    message.textContent = "Searching for available rooms...";
    roomResults.replaceChildren();

    try {
        const response = await fetch(
            "/api/rooms/available?" + params.toString()
        );

        const data = await response.json();

        if (!response.ok) {
            throw new Error(
                data.error || "Unable to search rooms."
            );
        }

        displayRooms(data);

    } catch (error) {
        message.textContent = error.message;
    } finally {
        searchButton.disabled = false;
    }
});

function displayRooms(rooms) {

    roomResults.replaceChildren();

    if (rooms.length === 0) {
        message.textContent =
            "No rooms are available for the selected criteria.";
        return;
    }

    message.textContent =
        `${rooms.length} available room(s) found.`;

    const formatter = new Intl.NumberFormat("en-LK", {
        style: "currency",
        currency: "LKR"
    });

    rooms.forEach(room => {

        const card = document.createElement("div");
        card.className = "room-card";

        const title = document.createElement("h3");
        title.textContent = "Room " + room.roomNumber;

        const type = document.createElement("p");
        type.textContent = "Type: " + room.roomType;

        const capacity = document.createElement("p");
        capacity.textContent =
            "Capacity: " + room.capacity + " guest(s)";

        const price = document.createElement("p");
        price.className = "room-price";
        price.textContent =
            formatter.format(room.pricePerNight) + " / night";

        const description = document.createElement("p");
        description.textContent = room.description || "";


        const bookButton = document.createElement("button");
        bookButton.type = "button";
        bookButton.className = "search-button";
        bookButton.textContent = "Book Now";

        bookButton.addEventListener("click", () => {
            const params = new URLSearchParams({
                roomId: room.id,
                checkIn: document.getElementById("checkIn").value,
                checkOut: document.getElementById("checkOut").value,
                guests: document.getElementById("guests").value
            });

            window.location.href =
                "/reservation.html?" + params.toString();
        });

        card.append(
            title,
            type,
            capacity,
            price,
            description,
            bookButton
        );


        roomResults.appendChild(card);
    });
}


// ========================================
// Saved Search Preferences - Sithum
// ========================================

const saveSearchBtn =
    document.getElementById("saveSearchBtn");

const loadSavedSearchesBtn =
    document.getElementById("loadSavedSearchesBtn");

const savedSearchMessage =
    document.getElementById("savedSearchMessage");

const savedSearchResults =
    document.getElementById("savedSearchResults");

function getTestUserId() {
    const userId = document
        .getElementById("savedSearchUserId")
        .value.trim();

    if (!userId) {
        throw new Error("Please enter a test user ID.");
    }

    return userId;
}

async function readApiResponse(response) {
    if (response.status === 204) {
        return null;
    }

    const data = await response.json();

    if (!response.ok) {
        throw new Error(
            data.error || "The request failed."
        );
    }

    return data;
}

// CREATE - Save current search
saveSearchBtn.addEventListener("click", async () => {

    try {
        const userId = getTestUserId();

        const checkInDate =
            document.getElementById("checkIn").value;

        const checkOutDate =
            document.getElementById("checkOut").value;

        const guestCount =
            Number(document.getElementById("guests").value);

        const roomType =
            document.getElementById("roomType").value;

        const minPrice =
            document.getElementById("minPrice").value;

        const maxPrice =
            document.getElementById("maxPrice").value;

        if (!checkInDate || !checkOutDate ||
            checkOutDate <= checkInDate) {
            throw new Error(
                "Please select a valid check-in and check-out date."
            );
        }

        if (!Number.isInteger(guestCount) || guestCount <= 0) {
            throw new Error("Guest count must be positive.");
        }

        if (minPrice !== "" && Number(minPrice) < 0 ||
            maxPrice !== "" && Number(maxPrice) < 0) {
            throw new Error("Prices cannot be negative.");
        }

        if (minPrice !== "" && maxPrice !== "" &&
            Number(minPrice) > Number(maxPrice)) {
            throw new Error(
                "Minimum price cannot exceed maximum price."
            );
        }

        const preference = {
            userId: userId,
            checkInDate: checkInDate,
            checkOutDate: checkOutDate,
            guestCount: guestCount,
            roomType: roomType || null,
            minPrice: minPrice === ""
                ? null : Number(minPrice),
            maxPrice: maxPrice === ""
                ? null : Number(maxPrice)
        };

        saveSearchBtn.disabled = true;

        const response = await fetch(
            "/api/search-preferences",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(preference)
            }
        );

        const created = await readApiResponse(response);

        savedSearchMessage.textContent =
            "Search preference saved successfully!";

        console.log("Created preference:", created);

        await loadSavedSearches();

    } catch (error) {
        savedSearchMessage.textContent = error.message;
    } finally {
        saveSearchBtn.disabled = false;
    }
});

// READ - View saved searches
loadSavedSearchesBtn.addEventListener(
    "click",
    loadSavedSearches
);

async function loadSavedSearches() {

    try {
        const userId = getTestUserId();

        const response = await fetch(
            "/api/search-preferences?userId=" +
            encodeURIComponent(userId)
        );

        const preferences =
            await readApiResponse(response);

        displaySavedPreferences(preferences);

    } catch (error) {
        savedSearchMessage.textContent = error.message;
        savedSearchResults.replaceChildren();
    }
}

// Display saved searches
function displaySavedPreferences(preferences) {

    savedSearchResults.replaceChildren();

    if (preferences.length === 0) {
        savedSearchMessage.textContent =
            "No saved searches found.";
        return;
    }

    savedSearchMessage.textContent =
        `${preferences.length} saved search(es) found.`;

    preferences.forEach(preference => {

        const card = document.createElement("div");
        card.className = "saved-preference-card";

        const title = document.createElement("h3");
        title.textContent =
            (preference.roomType || "Any Room Type") +
            " — " + preference.guestCount + " Guest(s)";

        const dates = document.createElement("p");
        dates.textContent =
            "Dates: " + preference.checkInDate +
            " to " + preference.checkOutDate;

        const price = document.createElement("p");
        price.textContent =
            "Price Range: LKR " +
            (preference.minPrice ?? "Any") +
            " - " + (preference.maxPrice ?? "Any");

        const actions = document.createElement("div");
        actions.className = "preference-actions";

        // LOAD SEARCH
        const loadBtn = document.createElement("button");
        loadBtn.type = "button";
        loadBtn.textContent = "Load Search";

        loadBtn.addEventListener("click", () => {
            stopEditing();
            fillSearchForm(preference);
            savedSearchMessage.textContent =
                "Search conditions loaded. Click Search Rooms.";

            searchForm.scrollIntoView({
                behavior: "smooth",
                block: "start"
            });
        });

        // EDIT
        const editBtn = document.createElement("button");
        editBtn.type = "button";
        editBtn.textContent = "Edit";

        editBtn.addEventListener("click", () => {
            try {
                startEditing(preference);
            } catch (error) {
                savedSearchMessage.textContent = error.message;
            }
        });

        // DELETE
        const deleteBtn = document.createElement("button");
        deleteBtn.type = "button";
        deleteBtn.textContent = "Delete";
        deleteBtn.className = "delete-btn";

        deleteBtn.addEventListener("click", () => {
            deleteSavedPreference(preference.id);
        });

        actions.append(loadBtn, editBtn, deleteBtn);

        card.append(title, dates, price, actions);

        savedSearchResults.appendChild(card);
    });
}


// ========================================
// Edit and Delete Saved Searches - Sithum
// ========================================

const updateSearchBtn =
    document.getElementById("updateSearchBtn");

const cancelEditBtn =
    document.getElementById("cancelEditBtn");

let editingPreferenceId = null;
let editingUserId = null;

// Fill the main search form with a saved preference
function fillSearchForm(preference) {
    document.getElementById("checkIn").value =
        preference.checkInDate;

    document.getElementById("checkOut").value =
        preference.checkOutDate;

    document.getElementById("guests").value =
        preference.guestCount;

    document.getElementById("roomType").value =
        preference.roomType || "";

    document.getElementById("minPrice").value =
        preference.minPrice ?? "";

    document.getElementById("maxPrice").value =
        preference.maxPrice ?? "";
}

// Get search conditions from the form
function getPreferenceFormData() {
    const checkInDate =
        document.getElementById("checkIn").value;

    const checkOutDate =
        document.getElementById("checkOut").value;

    const guestCount =
        Number(document.getElementById("guests").value);

    const roomType =
        document.getElementById("roomType").value;

    const minPrice =
        document.getElementById("minPrice").value;

    const maxPrice =
        document.getElementById("maxPrice").value;

    if (!checkInDate || !checkOutDate ||
        checkOutDate <= checkInDate) {
        throw new Error("Please select valid dates.");
    }

    if (!Number.isInteger(guestCount) || guestCount <= 0) {
        throw new Error("Guest count must be positive.");
    }

    if ((minPrice !== "" && Number(minPrice) < 0) ||
        (maxPrice !== "" && Number(maxPrice) < 0)) {
        throw new Error("Prices cannot be negative.");
    }

    if (minPrice !== "" && maxPrice !== "" &&
        Number(minPrice) > Number(maxPrice)) {
        throw new Error(
            "Minimum price cannot exceed maximum price."
        );
    }

    return {
        checkInDate,
        checkOutDate,
        guestCount,
        roomType: roomType || null,
        minPrice: minPrice === ""
            ? null : Number(minPrice),
        maxPrice: maxPrice === ""
            ? null : Number(maxPrice)
    };
}

// Start editing a preference
function startEditing(preference) {
    editingPreferenceId = preference.id;
    editingUserId = getTestUserId();

    fillSearchForm(preference);

    updateSearchBtn.hidden = false;
    cancelEditBtn.hidden = false;
    saveSearchBtn.hidden = true;

    savedSearchMessage.textContent =
        "Editing a saved search. Change the form values " +
        "and click Update Saved Search.";

    searchForm.scrollIntoView({
        behavior: "smooth",
        block: "start"
    });
}

// Return to normal mode
function stopEditing() {
    editingPreferenceId = null;
    editingUserId = null;

    updateSearchBtn.hidden = true;
    cancelEditBtn.hidden = true;
    saveSearchBtn.hidden = false;
}

cancelEditBtn.addEventListener("click", () => {
    stopEditing();
    savedSearchMessage.textContent = "Editing cancelled.";
});

// UPDATE - Save edited conditions
updateSearchBtn.addEventListener("click", async () => {
    if (!editingPreferenceId) {
        return;
    }

    try {
        const userId = getTestUserId();

        if (userId !== editingUserId) {
            throw new Error(
                "Do not change the test user while editing."
            );
        }

        const updatedData = getPreferenceFormData();

        updateSearchBtn.disabled = true;

        const response = await fetch(
            "/api/search-preferences/" +
            encodeURIComponent(editingPreferenceId) +
            "?userId=" + encodeURIComponent(userId),
            {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(updatedData)
            }
        );

        await readApiResponse(response);

        stopEditing();
        await loadSavedSearches();

        savedSearchMessage.textContent =
            "Saved search updated successfully!";

    } catch (error) {
        savedSearchMessage.textContent = error.message;
    } finally {
        updateSearchBtn.disabled = false;
    }
});

// DELETE - Remove a preference
async function deleteSavedPreference(preferenceId) {
    try {
        const userId = getTestUserId();

        if (!confirm(
            "Are you sure you want to delete this saved search?"
        )) {
            return;
        }

        const response = await fetch(
            "/api/search-preferences/" +
            encodeURIComponent(preferenceId) +
            "?userId=" + encodeURIComponent(userId),
            { method: "DELETE" }
        );

        await readApiResponse(response);

        if (editingPreferenceId === preferenceId) {
            stopEditing();
        }

        await loadSavedSearches();

        savedSearchMessage.textContent =
            "Saved search deleted successfully!";

    } catch (error) {
        savedSearchMessage.textContent = error.message;
    }
}


