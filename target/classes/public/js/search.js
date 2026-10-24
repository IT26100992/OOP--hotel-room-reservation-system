
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

        card.append(
            title,
            type,
            capacity,
            price,
            description
        );

        roomResults.appendChild(card);
    });
}
