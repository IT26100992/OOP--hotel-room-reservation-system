const registerForm = document.getElementById("registerForm");
const registerButton = document.getElementById("registerButton");
const formMessage = document.getElementById("formMessage");

const passwordInput = document.getElementById("password");
const confirmPasswordInput =
    document.getElementById("confirmPassword");

const togglePassword =
    document.getElementById("togglePassword");

function showMessage(message, type) {
    formMessage.textContent = message;
    formMessage.className = `form-message ${type}`;
}

// Toggle password visibility
togglePassword.addEventListener("click", () => {
    const isHidden = passwordInput.type === "password";

    passwordInput.type = isHidden ? "text" : "password";

    togglePassword.textContent = isHidden ? "Hide" : "Show";

    togglePassword.setAttribute(
        "aria-label",
        isHidden ? "Hide password" : "Show password"
    );
});

// Handle registration form
registerForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    showMessage("", "");

    const fullName =
        document.getElementById("fullName").value.trim();

    const email =
        document.getElementById("email").value.trim();

    const phone =
        document.getElementById("phone").value.trim();

    const password = passwordInput.value;

    const confirmPassword = confirmPasswordInput.value;

    if (password !== confirmPassword) {
        showMessage("Passwords do not match.", "error");
        return;
    }

    if (password.length < 12) {
        showMessage(
            "Password must contain at least 12 characters.",
            "error"
        );
        return;
    }

    const requestData = {
        fullName: fullName,
        email: email,
        password: password,
        phone: phone
    };

    registerButton.disabled = true;
    registerButton.textContent = "Creating account...";

    try {
        const response = await fetch("/api/auth/register", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(requestData)
        });

        const data = await response.json();

        if (!response.ok) {
            showMessage(
                data.error || "Registration failed.",
                "error"
            );
            return;
        }

        showMessage(
            "Account created successfully! You can now log in.",
            "success"
        );

        registerForm.reset();

    } catch (error) {
        showMessage(
            "Unable to connect to the server. Please try again.",
            "error"
        );
    } finally {
        registerButton.disabled = false;
        registerButton.textContent = "Create Account";
    }
});