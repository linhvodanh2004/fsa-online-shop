/**
 * Order Service - Handles order-related API calls and modal operations
 */
class OrderService {
    constructor() {
        this.baseUrl = '/orders/api';
        this.modal = null;
        this.init();
    }

    /**
     * Initialize the service - create modal and bind events
     */
    init() {
        this.createModal();
        this.bindEvents();
    }

    /**
     * Create the order detail modal HTML structure
     */
    createModal() {
        const modalHtml = `
            <div class="modal fade" id="orderDetailModal" tabindex="-1" aria-labelledby="orderDetailModalLabel" aria-hidden="true">
                <div class="modal-dialog modal-xl">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title" id="orderDetailModalLabel">Order Details</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body" id="orderDetailContent">
                            <div class="text-center">
                                <div class="spinner-border text-primary" role="status">
                                    <span class="visually-hidden">Loading...</span>
                                </div>
                                <p class="mt-2">Loading order details...</p>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                        </div>
                    </div>
                </div>
            </div>
        `;

        // Remove existing modal if it exists
        const existingModal = document.getElementById('orderDetailModal');
        if (existingModal) {
            existingModal.remove();
        }

        // Append modal to body
        document.body.insertAdjacentHTML('beforeend', modalHtml);
        this.modal = new bootstrap.Modal(document.getElementById('orderDetailModal'));
    }

    /**
     * Bind click events to view order buttons
     */
    // bindEvents() {
    //     document.addEventListener('click', (e) => {
    //         const viewBtn = e.target.closest('a[href*="/admin/orders/"][title="View order detail"]');
    //         if (viewBtn) {
    //             e.preventDefault();
    //             const href = viewBtn.getAttribute('href');
    //             const orderId = href.split('/').pop();
    //             this.showOrderDetail(orderId);
    //         }
    //     });
    // }
    /**
     * Bind click events to view order buttons (by href or data attributes)
     */
    bindEvents() {
        document.addEventListener('click', (e) => {
            // Support both <a> and <button> or any element with data-action="view-detail"
            const viewBtn = e.target.closest('[data-action="view-detail"]');
            if (viewBtn) {
                e.preventDefault();

                // Get order ID from data attribute
                const orderId = viewBtn.getAttribute('data-order-id');
                if (orderId) {
                    this.showOrderDetail(orderId);
                }
            }
        });
    }


    /**
     * Fetch and display order details
     * @param {string|number} orderId - The order ID
     */
    async showOrderDetail(orderId) {
        try {
            // Show loading state
            this.showLoading();
            this.modal.show();

            // Fetch order data
            const orderData = await this.fetchOrderDetail(orderId);

            // Render order details
            this.renderOrderDetail(orderData);
        } catch (error) {
            this.showError(error.message);
        }
    }

    /**
     * Fetch order detail from API
     * @param {string|number} orderId - The order ID
     * @returns {Promise<Object>} Order data
     */
    async fetchOrderDetail(orderId) {
        const response = await fetch(`${this.baseUrl}/order-detail/${orderId}`);

        if (!response.ok) {
            throw new Error(`Failed to fetch order details: ${response.status} ${response.statusText}`);
        }

        console.log(response);
        return await response.json();
    }

    /**
     * Show loading state in modal
     */
    showLoading() {
        const content = document.getElementById('orderDetailContent');
        content.innerHTML = `
            <div class="text-center">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Loading...</span>
                </div>
                <p class="mt-2">Loading order details...</p>
            </div>
        `;
    }

    /**
     * Show error message in modal
     * @param {string} message - Error message
     */
    showError(message) {
        const content = document.getElementById('orderDetailContent');
        content.innerHTML = `
            <div class="alert alert-danger" role="alert">
                <i data-feather="alert-triangle"></i>
                <strong>Error:</strong> ${message}
            </div>
        `;
        // Re-initialize feather icons
        if (typeof feather !== 'undefined') {
            feather.replace();
        }
    }

    /**
     * Render order details in modal
     * @param {Object} orderData - Order data from API
     */
    renderOrderDetail(orderData) {
        const content = document.getElementById('orderDetailContent');

        content.innerHTML = `
            <div class="row">
                <!-- Order Information -->
                <div class="col-md-6">
                    <div class="card mb-3">
                        <div class="card-header">
                            <h6 class="mb-0"><i data-feather="file-text"></i> Order Information</h6>
                        </div>
                        <div class="card-body">
                            <table class="table table-sm table-borderless">
                                <tr>
                                    <td><strong>Order ID:</strong></td>
                                    <td>#${orderData.orderId}</td>
                                </tr>
                                <tr>
                                    <td><strong>Status:</strong></td>
                                    <td>
                                        <span class="status-badge ${this.getStatusClass(orderData.status)}">
                                            ${orderData.status}
                                        </span>
                                    </td>
                                </tr>
                                <tr>
                                    <td><strong>Total Amount:</strong></td>
                                    <td><strong>${this.formatCurrency(orderData.sum)} VND</strong></td>
                                </tr>
                                <tr>
                                    <td><strong>Order Date:</strong></td>
                                    <td>${this.formatDateTime(orderData.creationTime)}</td>
                                </tr>
                                ${orderData.transitTime ? `
                                <tr>
                                    <td><strong>Transit Time:</strong></td>
                                    <td>${this.formatDateTime(orderData.transitTime)}</td>
                                </tr>
                                ` : ''}
                                ${orderData.deliveryTime ? `
                                <tr>
                                    <td><strong>Delivery Time:</strong></td>
                                    <td>${this.formatDateTime(orderData.deliveryTime)}</td>
                                </tr>
                                ` : ''}
                                ${orderData.cancellationTime ? `
                                <tr>
                                    <td><strong>Cancellation Time:</strong></td>
                                    <td>${this.formatDateTime(orderData.cancellationTime)}</td>
                                </tr>
                                ` : ''}
                                ${orderData.note ? `
                                <tr>
                                    <td><strong>Note:</strong></td>
                                    <td>${orderData.note}</td>
                                </tr>
                                ` : ''}
                            </table>
                        </div>
                    </div>
                </div>

                <!-- Customer & Delivery Information -->
                <div class="col-md-6">
                    <div class="card mb-3">
                        <div class="card-header">
                            <h6 class="mb-0"><i data-feather="user"></i> Customer & Delivery</h6>
                        </div>
                        <div class="card-body">
                            <table class="table table-sm table-borderless">
                                <tr>
                                    <td><strong>Username:</strong></td>
                                    <td>${orderData.username || 'N/A'}</td>
                                </tr>
                                <tr>
                                    <td><strong>Receiver Name:</strong></td>
                                    <td>${orderData.receiverName || 'N/A'}</td>
                                </tr>
                                <tr>
                                    <td><strong>Phone:</strong></td>
                                    <td>${orderData.receiverPhone || 'N/A'}</td>
                                </tr>
                                <tr>
                                    <td><strong>Email:</strong></td>
                                    <td>${orderData.receiverEmail || 'N/A'}</td>
                                </tr>
                                <tr>
                                    <td><strong>Address:</strong></td>
                                    <td>${orderData.receiverAddress || 'N/A'}</td>
                                </tr>
                            </table>
                        </div>
                    </div>

                    <!-- Payment Information -->
                    <div class="card mb-3">
                        <div class="card-header">
                            <h6 class="mb-0"><i data-feather="credit-card"></i> Payment Information</h6>
                        </div>
                        <div class="card-body">
                            <table class="table table-sm table-borderless">
                                <tr>
                                    <td><strong>Payment Method:</strong></td>
                                    <td>
                                        <span class="payment-badge ${this.getPaymentClass(orderData.paymentMethod)}">
                                            ${orderData.paymentMethod}
                                        </span>
                                    </td>
                                </tr>
                                <tr>
                                    <td><strong>Payment Status:</strong></td>
                                    <td>
                                        <span class="paid-badge ${orderData.paymentStatus ? 'paid-yes' : 'paid-no'}">
                                            ${orderData.paymentStatus ? 'PAID' : 'UNPAID'}
                                        </span>
                                    </td>
                                </tr>
                                ${orderData.paymentTransactionId ? `
                                <tr>
                                    <td><strong>Transaction ID:</strong></td>
                                    <td>${orderData.paymentTransactionId}</td>
                                </tr>
                                ` : ''}
                                ${orderData.paymentDate ? `
                                <tr>
                                    <td><strong>Payment Date:</strong></td>
                                    <td>${this.formatDateTime(orderData.paymentDate)}</td>
                                </tr>
                                ` : ''}
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Order Items -->
            <div class="card">
                <div class="card-header">
                    <h6 class="mb-0"><i data-feather="shopping-cart"></i> Order Items</h6>
                </div>
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="table table-hover">
                            <thead class="table-light">
                                <tr>
                                    <th>Product</th>
                                    <th>Image</th>
                                    <th>Unit Price</th>
                                    <th>Quantity</th>
                                    <th>Total</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${orderData.orderItems.map(item => `
                                    <tr>
                                        <td>
                                            <strong>${item.productName}</strong><br>
                                            <small class="text-muted">ID: ${item.productId}</small>
                                        </td>
                                        <td>
                                            <img src="${item.productImage}" 
                                                 alt="${item.productName}" 
                                                 class="img-thumbnail" 
                                                 style="width: 60px; height: 60px; object-fit: cover;"
                                                 onerror="this.src='data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjAiIGhlaWdodD0iNjAiIHZpZXdCb3g9IjAgMCA2MCA2MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHJlY3Qgd2lkdGg9IjYwIiBoZWlnaHQ9IjYwIiBmaWxsPSIjRjNGNEY2Ii8+CjxwYXRoIGQ9Ik0yNSAyNUgzNVYzNUgyNVYyNVoiIGZpbGw9IiNEMUQ1REIiLz4KPC9zdmc+'">
                                        </td>
                                        <td>${this.formatCurrency(item.unitPrice)} VND</td>
                                        <td>
                                            <span class="badge bg-secondary">${item.quantity}</span>
                                        </td>
                                        <td><strong>${this.formatCurrency(item.price)} VND</strong></td>
                                    </tr>
                                `).join('')}
                            </tbody>
                            <tfoot class="table-light">
                                <tr>
                                    <th colspan="4" class="text-end">Total Amount:</th>
                                    <th>${this.formatCurrency(orderData.sum)} VND</th>
                                </tr>
                            </tfoot>
                        </table>
                    </div>
                </div>
            </div>
        `;

        // Update modal title with order ID
        document.getElementById('orderDetailModalLabel').textContent = `Order Details - #${orderData.orderId}`;

        // Re-initialize feather icons
        if (typeof feather !== 'undefined') {
            feather.replace();
        }
    }

    /**
     * Get CSS class for order status
     * @param {string} status - Order status
     * @returns {string} CSS class
     */
    getStatusClass(status) {
        const statusClasses = {
            'PENDING': 'status-pending',
            'IN TRANSIT': 'status-in-transit',
            'DELIVERED': 'status-delivered',
            'CANCELLED': 'status-cancelled'
        };
        return statusClasses[status] || 'status-pending';
    }

    /**
     * Get CSS class for payment method
     * @param {string} method - Payment method
     * @returns {string} CSS class
     */
    getPaymentClass(method) {
        const paymentClasses = {
            'COD': 'payment-cash',
            'VNPAY': 'payment-card',
            'PAYPAL': 'payment-paypal',
            'BANK': 'payment-bank'
        };
        return paymentClasses[method] || 'payment-card';
    }

    /**
     * Format currency with thousand separators
     * @param {number} amount - Amount to format
     * @returns {string} Formatted currency
     */
    formatCurrency(amount) {
        if (amount == null) return '0';
        return new Intl.NumberFormat('vi-VN').format(amount);
    }

    /**
     * Format date time string
     * @param {string} dateTimeString - ISO date time string
     * @returns {string} Formatted date time
     */
    formatDateTime(dateTimeString) {
        if (!dateTimeString) return 'N/A';
        const date = new Date(dateTimeString);
        return date.toLocaleString('vi-VN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
    }

    /**
     * Hide the modal
     */
    hide() {
        if (this.modal) {
            this.modal.hide();
        }
    }

    /**
     * Destroy the service and clean up
     */
    destroy() {
        if (this.modal) {
            this.modal.dispose();
        }
        const modalElement = document.getElementById('orderDetailModal');
        if (modalElement) {
            modalElement.remove();
        }
    }
}

// Initialize the service when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    window.orderService = new OrderService();
});

// Export for module usage
if (typeof module !== 'undefined' && module.exports) {
    module.exports = OrderService;
}