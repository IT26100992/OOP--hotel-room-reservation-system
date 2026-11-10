const displayName = document.getElementById("displayName");
const displayEmail = document.getElementById("displayEmail");
const userInitial = document.getElementById("userInitial");

const detailName = document.getElementById("detailName");
const detailEmail = document.getElementById("detailEmail");
const detailPhone = document.getElementById("detailPhone");
const detailRole = document.getElementById("detailRole");

const editButton = document.getElementById("editButton");
const editSection = document.getElementById("editSection");
const cancelButton = document.getElementById("cancelButton");
const saveButton = document.getElementById("saveButton");

const profileForm = document.getElementById("profileForm");
const profileMessage = document.getElementById("profileMessage");

const deactivateButton =
    document.getElementById("deactivateButton");

let currentUser = null;

function showMessage(message, type = "") {
    profileMessage.textContent = message;
    profileMessage.className = `form-message ${type}`;
}

// Display user information safely using textContent
function displayProfile(user) {
    currentUser = user;

    displayName.textContent = user.fullName;
    displayEmail.textContent = user.email;

    userInitial.textContent =
        user.fullName?.charAt(0).toUpperCase() || "?";

    detailName.textContent = user.fullName;
    detailEmail.textContent = user.email;
    detailPhone.textContent = user.phone || "Not provided";
    detailRole.textContent = user.role;

    document.getElementById("fullName").value = user.fullName;
    document.getElementById("email").value = user.email;
    document.getElementById("phone").value = user.phone || "";
}

// Show edit form
editButton.addEventListener("click", () => {
    editSection.hidden = false;
    showMessage("", "");

    editSection.scrollIntoView({
        behavior: "smooth",
        block: "start"
    });
});

// Hide edit form and discard unsaved input
cancelButton.addEventListener("click", () => {
    if (currentUser) {
        displayProfile(currentUser);
    }

    editSection.hidden = true;
    showMessage("", "");
});

// Save updated profile
profileForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    showMessage("", "");

    const updatedData = {
        fullName: document.getElementById("fullName").value.trim(),
        email: document.getElementById("email").value.trim(),
        phone: document.getElementById("phone").value.trim()
    };

    saveButton.disabled = true;
    saveButton.textContent = "Saving...";

    try {
        const response = await fetch("/api/users/me", {
            method: "PUT",
            credentials: "same-origin",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(updatedData)
        });

        if (response.status === 401) {
            window.location.replace(GrandStaySession.signInUrl());
            return;
        }

        const result = await response.json();

        if (!response.ok) {
            showMessage(
                result.error || "Profile update failed.",
                "error"
            );
            return;
        }

        displayProfile(result);
        editSection.hidden = true;

        showMessage(
            "Profile updated successfully!",
            "success"
        );

    } catch (error) {
        showMessage(
            "Unable to save changes. Please try again.",
            "error"
        );
    } finally {
        saveButton.disabled = false;
        saveButton.textContent = "Save Changes";
    }
});

// Deactivate account
deactivateButton.addEventListener("click", async () => {

    const confirmed = window.confirm(
        "Are you sure you want to deactivate your account? " +
        "You will not be able to sign in again."
    );

    if (!confirmed) {
        return;
    }

    deactivateButton.disabled = true;

    try {
        const response = await fetch("/api/users/me", {
            method: "DELETE",
            credentials: "same-origin"
        });

        if (!response.ok) {
            throw new Error("Account deactivation failed.");
        }

        window.location.replace("/login.html");

    } catch (error) {
        showMessage(
            "Unable to deactivate your account.",
            "error"
        );
        deactivateButton.disabled = false;
    }
});

// Initialize the page
GrandStaySession.ready.then(user => {
    if (user) displayProfile(user);
});
