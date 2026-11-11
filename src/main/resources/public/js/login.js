const loginForm = document.getElementById("loginForm");
const loginButton = document.getElementById("loginButton");
const formMessage = document.getElementById("formMessage");

const passwordInput = document.getElementById("password");
const togglePassword = document.getElementById("togglePassword");

function showMessage(message, type) {
    formMessage.textContent = message;
    formMessage.className = `form-message ${type}`;
}

// Show or hide the password
togglePassword.addEventListener("click", () => {
    const hidden = passwordInput.type === "password";

    passwordInput.type = hidden ? "text" : "password";
    togglePassword.textContent = hidden ? "Hide" : "Show";
    togglePassword.setAttribute("aria-label", hidden ? "Hide password" : "Show password");
});

// Handle login
loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    showMessage("", "");

    const email = document.getElementById("email").value.trim();
    const password = passwordInput.value;

    loginButton.disabled = true;
    loginButton.textContent = "Signing in...";

    try {
        const response = await fetch("/api/auth/login", {
            method: "POST",
            credentials: "same-origin",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                email: email,
                password: password
            })
        });

        const data = await response.json();

        if (!response.ok) {
            showMessage(
                data.error || "Invalid email or password.",
                "error"
            );
            return;
        }

        showMessage("Login successful!", "success");

        window.location.replace(GrandStaySession.afterLogin(data.role));

    } catch (error) {
        showMessage(
            "Unable to connect to the server.",
            "error"
        );
    } finally {
        loginButton.disabled = false;
        loginButton.textContent = "Sign In";
    }
});
