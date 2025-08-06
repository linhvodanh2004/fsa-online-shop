document.addEventListener("DOMContentLoaded", function () {
    const params = new URLSearchParams(window.location.search);

    const messageMap = {
        "register-successfully": "Register successfully",
        "username-exists": "Username already exists",
        "email-exists": "Email already exists",
        "phone-exists": "Phone already exists",
        "bad-credentials": "Username or password is incorrect",
        "disabled": "Your account is disabled",
        "login-error": "An error occurred while logging in",
        "reset-successfully": "Reset password successfully",
        "username-or-email-not-matched": "Username or email not matched",
        "session-expired": "Your session has expired",
        "logout-successfully": "Logout successfully",
        "category-exists": "Category already exists",
        "add-category-successfully": "Category has been added successfully",
        "edit-category-successfully": "Category has been updated successfully",
        "delete-category-successfully": "Category has been deleted successfully",
        "category-not-found": "Category not found",
        "failed-to-upload-category-image": "An error occurred while uploading category image",
        "failed-to-delete-category-image": "An error occurred while deleting category image",
        "category-has-products": "Category is temporarily unable to be deleted due to the existence of products",
        "email-failed": "An error occurred while sending email",
        "checkout-success": "Checkout successfully",
        "checkout-failed": "Checkout failed",
        "update-order-status-successfully": "Update status of order successfully",
        "fail-to-update-order-status": "Fail to update order status",
        "profile-updated-successfully": "Profile updated successfully",
        "old-password-invalid": "Old password is invalid",
        "product-not-found": "Product not found",
    }

    if (params.has("success")) {
        const key = params.get("success");
        const message = messageMap[key] || "Success!";
        Toastify({
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
        Toastify({
            text: message,
            duration: 3000,
            gravity: "top",
            position: "right",
            newWindow: true,
            close: true,
            stopOnFocus: true,
            style: {
                background: "#f44336",
            }
        }).showToast();
    }
});
