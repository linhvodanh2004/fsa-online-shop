const params = new URLSearchParams(window.location.search);

const messsageMap = {
    "login-ok": "Login successfully",
    "login-failed": "Login failed"
}
if (params.has("success")) {
    const key = params.get("success");
    const message = messsageMap[key] || "Success!";
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
    const message = messsageMap[key] || "An error occurred!";
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
