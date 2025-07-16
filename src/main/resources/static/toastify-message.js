const params = new URLSearchParams(window.location.search);

const messageMap = {
    "login-ok": "Login successfully",
    "login-failed": "Login failed",
    "register-successfully": "Register successfully",
    "username-exists": "Username already exists",
    "email-exists": "Email already exists",
    "bad-credentials": "Username or password is incorrect",
    "disabled": "Your account is disabled",
    "login-error": "An error occurred while logging in",
}
if (params.has("success")) {
    const key = params.get("success");
    const message = messageMap[key] || "Success!";
    Toastify( {
        text: message,
        duration: 3000,
        gravity: "top",
        position: "right",
        newWindow: true,
        close: true,
        stopOnFocus: true,
        style: {
            background: "#4caf50",
        }
    }).showToast();
}
if (params.has("error")) {
    const key = params.get("error");
    const message = messageMap[key] || "An error occurred!";
    Toastify( {
        text: message,
        duration: 3000,
        gravity: "top",
        position: "right",
        newWindow: true,
        close: true,
        style: {
            background: "#f44336",
        }
    }).showToast();
}
