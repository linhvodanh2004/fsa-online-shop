// cart-api.js - External JavaScript for Cart API interactions

class CartAPI {
    constructor() {
        this.baseUrl = window.location.origin;
        this.csrfToken = this.getCSRFToken();
    }

    // Get CSRF token from meta tag or hidden input
    getCSRFToken() {
        const csrfMeta = document.querySelector('meta[name="_csrf"]');
        const csrfInput = document.querySelector('input[name="_token"]');

        if (csrfMeta) {
            return csrfMeta.getAttribute('content');
        } else if (csrfInput) {
            return csrfInput.value;
        }
        return null;
    }

    // Add product to cart
    async addToCart(productId, quantity = 1) {
        try {
            const body = {
                productId: productId,
                quantity: quantity,
            };

            const response = await fetch(`${this.baseUrl}/cart/api/add-to-cart`, {
                method: 'POST',
                body: JSON.stringify(body), // ✅ Correctly serialize JSON
                credentials: 'same-origin',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            const data = await response.json();
            if (data.success) {
                this.updateCartUI(data.cartItemCount);
                this.showToast(data.message, 'success');
                return data;
            } else {
                this.showToast(data.message, 'error');
                return null;
            }
        } catch (error) {
            console.error('Error adding to cart:', error);
            this.showToast('Failed to add product to cart', 'error');
            return null;
        }
    }

    // Delete item from cart by productId
    async removeFromCart(productId, callback = null) {
        try {
            const response = await fetch(`${this.baseUrl}/cart/api/delete/${productId}`, {
                method: 'DELETE',
                credentials: 'same-origin',
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });
            if (!response.ok) {
                throw new Error("Delete failed");
            }
            const msg = await response.text();
            this.showToast("Remove item successfully", 'success');

        } catch (error) {
            console.error("Lỗi khi xoá sản phẩm:", error);
            // this.showToast("Fail to remove item", 'error');
        }
        finally {
            // Optional callback (like reloadCart)
            if (typeof callback === 'function') {
                callback();
            }
            // Update cart count
            this.getCartCount().then(count => this.updateCartUI(count));
        }
    }

    async increaseQuantity(productId, callback = null) {
        try {
            const response = await fetch(`${this.baseUrl}/cart/api/increase-quantity/${productId}`, {
                method: 'POST',
                credentials: 'same-origin',
                headers: { 'Content-Type': 'application/json' }
            });
            const data = await response.json();
            this.showToast(data.message, (data.success) ? 'success' : 'error');
            return data;
        } catch (error) {
            this.showToast(error, 'error');
        } finally {
            // Optional callback (like reloadCart)
            if (typeof callback === 'function') {
                callback();
            }
        }
    }

    async decreaseQuantity(productId, callback = null) {
        try {
            const response = await fetch(`${this.baseUrl}/cart/api/decrease-quantity/${productId}`, {
                method: 'POST',
                credentials: 'same-origin',
                headers: { 'Content-Type': 'application/json' }
            });
            const data = await response.json();
            this.showToast(data.message, (data.success) ? 'success' : 'error');
            return data;
        } catch (error) {
            this.showToast(error, 'error');
        } finally {
            // Optional callback (like reloadCart)
            if (typeof callback === 'function') {
                callback();
            }
        }
    }

    // Get cart item count
    async getCartCount() {
        try {
            const response = await fetch(`${this.baseUrl}/cart/api/count`, {
                method: 'GET',
                credentials: 'same-origin',
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });
            if (response.ok) {
                return await response.json();
            }
            return 0;
        } catch (error) {
            console.error('Error getting cart count:', error);
            return 0;
        }
    }

    // This method will display number of distinct item in cart
    updateCartUI(count) {
        const cartCountElements = document.querySelectorAll('.cart-count');
        // const cartCountElement = document.getElementById('cart-count');
        cartCountElements.forEach(element => {
            element.textContent = count;
        })
        // cartCountElements.textContent = count;
    }
    showToast = (msg, type = 'error') => {
        Toastify({
            text: msg,
            duration: 3000,
            gravity: "top",
            position: "right",
            newWindow: true,
            close: true,
            style: {
                background: (type === 'error') ? "#f44336" : "#4caf50",
            }
        }).showToast();
    };
    // Initialize cart functionality
    init() {
        // Add event listeners to add-to-cart buttons
        document.addEventListener('click', async (e) => {
            const button = e.target.closest('.add-to-cart-btn, [data-add-to-cart]');
            if (!button) return;
            e.preventDefault();
            const productId = button.getAttribute('data-product-id') || button.dataset.productId;
            const quantity = button.getAttribute('data-quantity') || button.dataset.quantity || 1;
            if (!productId) {
                console.error('Product ID not found');
                return;
            }
            try {
                await this.addToCart(productId, parseInt(quantity));
            } finally {
                // Re-enable button
                // button.disabled = false;
                // button.innerHTML = originalText;
            }
        });

        // Load initial cart count
        this.getCartCount().then(count => {
            this.updateCartUI(count);
        });
    }
}

// Initialize cart API when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.cartAPI = new CartAPI();
    window.cartAPI.init();
});

// Utility functions for direct use
window.addToCart = (productId, quantity = 1) => {
    if (window.cartAPI) {
        return window.cartAPI.addToCart(productId, quantity);
    }
};
// Utility function
window.removeFromCart = (productId, callback = null) => {
    if (window.cartAPI) {
        return window.cartAPI.removeFromCart(productId, callback);
    }
};
window.increaseQuantity = (productId, callback = null) => {
    if (window.cartAPI) {
        return window.cartAPI.increaseQuantity(productId, callback);
    }
};
window.decreaseQuantity = (productId, callback = null) => {
    if (window.cartAPI) {
        return window.cartAPI.decreaseQuantity(productId, callback);
    }
};
window.showToast = (msg, type = 'error') => {
    if (window.cartAPI) {
        return window.cartAPI.showToast(msg, type);
    }
}
