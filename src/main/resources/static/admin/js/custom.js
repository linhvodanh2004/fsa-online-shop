// Pagination functionality
let currentPage = 1;
let itemsPerPage = 5;
let allUsers = [];
let filteredUsers = [];

// Variables for role management modal
let currentUserId = null;
let currentAction = null;

document.addEventListener('DOMContentLoaded', function() {
    // Initialize users array from the table
    initializeUsers();

    // Set up event listeners for pagination
    const itemsPerPageSelect = document.getElementById('itemsPerPage');
    if (itemsPerPageSelect) {
        itemsPerPageSelect.addEventListener('change', function() {
            itemsPerPage = parseInt(this.value);
            currentPage = 1;
            updateTable();
        });
    }

    // Search and filter functionality
    const searchBox = document.getElementById('userSearchBox');
    if (searchBox) {
        searchBox.addEventListener('input', applyFilters);
    }

    const sortSelect = document.getElementById('sortSelect');
    if (sortSelect) {
        sortSelect.addEventListener('change', applyFilters);
    }

    const roleFilter = document.getElementById('roleFilter');
    if (roleFilter) {
        roleFilter.addEventListener('change', applyFilters);
    }

    const providerFilter = document.getElementById('providerFilter');
    if (providerFilter) {
        providerFilter.addEventListener('change', applyFilters);
    }

    // Form validation setup
    setupFormValidation();

    // Initial table update
    applyFilters();
});

function initializeUsers() {
    const tableBody = document.getElementById('userTableBody') || document.querySelector('#datatables-column-search-text-inputs tbody');
    if (!tableBody) {
        console.error('Table body not found');
        return;
    }

    const rows = tableBody.querySelectorAll('tr');

    allUsers = [];
    rows.forEach((row, index) => {
        // Skip empty rows or rows that don't have enough cells
        if (row.cells.length < 7) return;

        // Skip the "add user" row (usually the last row or has specific classes)
        if (row.classList.contains('add-user-row') ||
            row.querySelector('input[type="text"]') ||
            row.querySelector('button[type="submit"]')) {
            return;
        }

        try {
            const userData = {
                element: row.cloneNode(true),
                username: row.cells[1] ? row.cells[1].textContent.trim() : '',
                fullname: row.cells[2] ? row.cells[2].textContent.trim() : '',
                email: row.cells[3] ? row.cells[3].textContent.trim() : '',
                phone: row.cells[4] ? row.cells[4].textContent.trim() : '',
                role: row.cells[5] ? row.cells[5].textContent.trim() : '',
                provider: row.cells[6] ? row.cells[6].textContent.trim() : '',
                status: row.cells[7] ? (row.cells[7].querySelector('i') ?
                    row.cells[7].querySelector('i').getAttribute('data-status') === 'true' : false) : false
            };

            // Only add if we have at least username or email
            if (userData.username || userData.email) {
                allUsers.push(userData);
            }
        } catch (error) {
            console.warn('Error processing row:', error, row);
        }
    });

    console.log('Initialized users:', allUsers.length);
}

function applyFilters() {
    const searchBox = document.getElementById('userSearchBox');
    const sortSelect = document.getElementById('sortSelect');
    const roleFilter = document.getElementById('roleFilter');
    const providerFilter = document.getElementById('providerFilter');

    const searchTerm = searchBox ? searchBox.value.toLowerCase().trim() : '';
    const sortValue = sortSelect ? sortSelect.value : '';
    const roleFilterValue = roleFilter ? roleFilter.value : '';
    const providerFilterValue = providerFilter ? providerFilter.value : '';

    console.log('Applying filters:', { searchTerm, roleFilterValue, providerFilterValue });

    // Filter users
    filteredUsers = allUsers.filter(user => {
        let matchesSearch = true;
        let matchesRole = true;
        let matchesProvider = true;

        // Search filter
        if (searchTerm) {
            matchesSearch = user.username.toLowerCase().includes(searchTerm) ||
                           user.fullname.toLowerCase().includes(searchTerm) ||
                           user.email.toLowerCase().includes(searchTerm);
        }

        // Role filter
        if (roleFilterValue) {
            matchesRole = user.role === roleFilterValue;
        }

        // Provider filter
        if (providerFilterValue) {
            matchesProvider = user.provider === providerFilterValue;
        }

        return matchesSearch && matchesRole && matchesProvider;
    });

    console.log('Filtered users:', filteredUsers.length);

    // Sort users
    if (sortValue) {
        const [field, direction] = sortValue.split('-');
        filteredUsers.sort((a, b) => {
            let aValue = (a[field] || '').toLowerCase();
            let bValue = (b[field] || '').toLowerCase();

            if (direction === 'asc') {
                return aValue.localeCompare(bValue);
            } else {
                return bValue.localeCompare(aValue);
            }
        });
    }

    currentPage = 1;
    updateTable();
}

function updateTable() {
    const tableBody = document.getElementById('userTableBody') || document.querySelector('#datatables-column-search-text-inputs tbody');
    if (!tableBody) {
        console.error('Table body not found for update');
        return;
    }

    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const pageUsers = filteredUsers.slice(startIndex, endIndex);

    // Find and preserve the "add user" row
    const addUserRow = Array.from(tableBody.querySelectorAll('tr')).find(row =>
        row.classList.contains('add-user-row') ||
        row.querySelector('input[type="text"]') ||
        row.querySelector('button[type="submit"]')
    );

    // Clear table body
    tableBody.innerHTML = '';

    // Add filtered users for current page
    pageUsers.forEach(user => {
        const clonedRow = user.element.cloneNode(true);
        tableBody.appendChild(clonedRow);
    });

    // Re-add the "add user" row if it exists
    if (addUserRow) {
        tableBody.appendChild(addUserRow.cloneNode(true));
    }

    // Reinitialize feather icons for the new content
    if (typeof feather !== 'undefined') {
        feather.replace();
    }

    updatePaginationInfo();
    updatePaginationNav();
}

function updatePaginationInfo() {
    const paginationInfo = document.getElementById('paginationInfo');
    if (!paginationInfo) return;

    const totalItems = filteredUsers.length;
    const startIndex = Math.min((currentPage - 1) * itemsPerPage + 1, totalItems);
    const endIndex = Math.min(currentPage * itemsPerPage, totalItems);

    if (totalItems === 0) {
        paginationInfo.textContent = 'No entries found';
    } else {
        paginationInfo.textContent = `Showing ${startIndex} to ${endIndex} of ${totalItems} entries`;
    }
}

function updatePaginationNav() {
    const paginationNav = document.getElementById('paginationNav');
    if (!paginationNav) return;

    const totalPages = Math.ceil(filteredUsers.length / itemsPerPage);

    // Clear existing page numbers
    const prevButton = document.getElementById('prevPage');
    const nextButton = document.getElementById('nextPage');
    paginationNav.innerHTML = '';

    // Add previous button
    if (prevButton) {
        const newPrevButton = prevButton.cloneNode(true);
        newPrevButton.classList.toggle('disabled', currentPage === 1);
        newPrevButton.addEventListener('click', function(e) {
            e.preventDefault();
            if (currentPage > 1) {
                currentPage--;
                updateTable();
            }
        });
        paginationNav.appendChild(newPrevButton);
    }

    // Add page numbers
    const startPage = Math.max(1, currentPage - 2);
    const endPage = Math.min(totalPages, currentPage + 2);

    for (let i = startPage; i <= endPage; i++) {
        const pageItem = document.createElement('li');
        pageItem.className = 'page-item';
        if (i === currentPage) {
            pageItem.classList.add('active');
        }

        const pageLink = document.createElement('a');
        pageLink.className = 'page-link';
        pageLink.href = '#';
        pageLink.textContent = i;
        pageLink.addEventListener('click', function(e) {
            e.preventDefault();
            currentPage = i;
            updateTable();
        });

        pageItem.appendChild(pageLink);
        paginationNav.appendChild(pageItem);
    }

    // Add next button
    if (nextButton) {
        const newNextButton = nextButton.cloneNode(true);
        newNextButton.classList.toggle('disabled', currentPage === totalPages || totalPages === 0);
        newNextButton.addEventListener('click', function(e) {
            e.preventDefault();
            if (currentPage < totalPages) {
                currentPage++;
                updateTable();
            }
        });
        paginationNav.appendChild(newNextButton);
    }
}

function updateRowOpacity(icon, newStatus) {
    const row = icon.closest('tr');
    if (!newStatus) {
        row.classList.add('opacity-50');
    } else {
        row.classList.remove('opacity-50');
    }
}

function toggleStatus(icon) {
    const currentStatus = icon.getAttribute('data-status') === 'true';
    const userId = icon.getAttribute('data-id');
    const newStatus = !currentStatus;

    updateRowOpacity(icon, newStatus);

    // Get CSRF token and header name from meta tags
    const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    const csrfToken = csrfTokenMeta ? csrfTokenMeta.getAttribute('content') : null;
    const csrfHeader = csrfHeaderMeta ? csrfHeaderMeta.getAttribute('content') : null;

    // Build headers object
    const headers = {
        'Content-Type': 'application/json'
    };
    if (csrfHeader && csrfToken && csrfHeader !== 'null' && csrfHeader !== 'undefined') {
        headers[csrfHeader] = csrfToken;
    }

    fetch(`/admin/users/${userId}/status`, {
        method: 'POST',
        headers: headers,
        body: JSON.stringify({ status: newStatus })
    })
    .then(response => {
        if (!response.ok) throw new Error('Failed to update status');
        // Update icon and data-status without reload
        icon.setAttribute('data-status', newStatus);
        if (newStatus) {
            icon.setAttribute('data-feather', 'toggle-right');
            icon.style.color = 'green';
        } else {
            icon.setAttribute('data-feather', 'toggle-left');
            icon.style.color = 'red';
        }
        if (window.feather) feather.replace();
    })
    .catch(error => {
        alert('Error updating user status!');
        console.error(error);
        updateRowOpacity(icon, currentStatus);
    });
}

function deleteUser(userId) {
    if (confirm('Are you sure you want to delete this user? This action cannot be undone.')) {
        // Get CSRF token and header name from meta tags
        const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
        const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
        const csrfToken = csrfTokenMeta ? csrfTokenMeta.getAttribute('content') : null;
        const csrfHeader = csrfHeaderMeta ? csrfHeaderMeta.getAttribute('content') : null;

        // Build headers object
        const headers = {
            'Content-Type': 'application/json'
        };
        if (csrfHeader && csrfToken && csrfHeader !== 'null' && csrfHeader !== 'undefined') {
            headers[csrfHeader] = csrfToken;
        }

        fetch(`/admin/users/${userId}/delete`, {
            method: 'DELETE',
            headers: headers
        })
        .then(response => {
            if (response.ok) {
                location.reload();
            } else {
                throw new Error('Failed to delete user');
            }
        })
        .catch(error => {
            alert('Error deleting user!');
            console.error(error);
        });
    }
}

function setupFormValidation() {
    // Add User Form Validation
    const addUserForm = document.getElementById("addUserForm");
    if (addUserForm) {
        addUserForm.addEventListener("submit", function (e) {
            e.preventDefault();

            const username = this.username.value.trim();
            const password = this.password.value.trim();
            const fullname = this.fullname.value.trim();
            const email = this.email.value.trim();
            const roleId = this.roleId.value;

            const showToast = (msg) => {
                Toastify({
                    text: msg,
                    duration: 3000,
                    gravity: "top",
                    position: "right",
                    newWindow: true,
                    close: true,
                    style: {
                        background: "#f44336",
                    }
                }).showToast();
            };

            // Validation
            if (!username && !password && !fullname && !email && (!roleId || roleId == "")) {
                showToast("Please fill in all required fields");
                return;
            }

            if (!username) {
                showToast("Please enter username");
                return;
            }

            if (!password) {
                showToast("Please enter password");
                return;
            }

            if (password.length < 6) {
                showToast("Password must be at least 6 characters long");
                return;
            }

            if (!fullname) {
                showToast("Please enter full name");
                return;
            }

            if (!email) {
                showToast("Please enter email address");
                return;
            }

            // Email validation
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(email)) {
                showToast("Please enter a valid email address");
                return;
            }

            if (!roleId || roleId == "") {
                showToast("Please select a role");
                return;
            }

            this.submit();
        });
    }

    // Update User Form Validation
    const updateUserForm = document.getElementById("updateUserForm");
    if (updateUserForm) {
        updateUserForm.addEventListener("submit", function (e) {
            e.preventDefault();

            const username = this.username.value.trim();
            const fullname = this.fullname.value.trim();
            const email = this.email.value.trim();
            const password = this.password.value.trim();
            const roleId = this.roleId.value;

            const showToast = (msg) => {
                Toastify({
                    text: msg,
                    duration: 3000,
                    gravity: "top",
                    position: "right",
                    newWindow: true,
                    close: true,
                    style: {
                        background: "#f44336",
                    }
                }).showToast();
            };

            // Validation
            if (!username) {
                showToast("Please enter username");
                return;
            }

            if (!fullname) {
                showToast("Please enter full name");
                return;
            }

            if (!email) {
                showToast("Please enter email address");
                return;
            }

            // Email validation
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(email)) {
                showToast("Please enter a valid email address");
                return;
            }

            // Password validation (only if password is provided)
            if (password && password.length < 6) {
                showToast("Password must be at least 6 characters long");
                return;
            }

            if (!roleId || roleId == "") {
                showToast("Please select a role");
                return;
            }

            this.submit();
        });
    }
}

// Enhanced showToast function with different styles for different message types
function showToast(message, type = 'info', duration = 3000) {
    let backgroundColor, textColor;

    switch(type) {
        case 'success':
        case 'promotion':
            backgroundColor = 'linear-gradient(to right, #00b09b, #96c93d)';
            textColor = '#ffffff';
            break;
        case 'error':
            backgroundColor = 'linear-gradient(to right, #ff5f6d, #ffc371)';
            textColor = '#ffffff';
            break;
        case 'warning':
        case 'demotion':
            backgroundColor = 'linear-gradient(to right, #f093fb, #f5576c)';
            textColor = '#ffffff';
            break;
        case 'info':
        default:
            backgroundColor = 'linear-gradient(to right, #4facfe, #00f2fe)';
            textColor = '#ffffff';
            break;
    }

    Toastify({
        text: message,
        duration: duration,
        close: true,
        gravity: "top", // top or bottom
        position: "right", // left, center or right
        stopOnFocus: true, // Prevents dismissing of toast on hover
        style: {
            background: backgroundColor,
            color: textColor,
            borderRadius: "8px",
            fontSize: "14px",
            fontWeight: "500",
            padding: "12px 16px",
            boxShadow: "0 4px 12px rgba(0,0,0,0.15)"
        },
        onClick: function(){} // Callback after click
    }).showToast();
}


// Show confirmation modal for role management
function showConfirmModal(action, userId, userName) {
    currentUserId = userId;
    currentAction = action;

    // Create modal if it doesn't exist
    let modal = document.getElementById('confirmModal');
    if (!modal) {
        createConfirmModal();
        modal = document.getElementById('confirmModal');
    }

    const message = document.getElementById('confirmMessage');
    const confirmBtn = document.getElementById('confirmBtn');

    if (action === 'promote') {
        message.textContent = `Are you sure you want to promote ${userName} to Admin?`;
        confirmBtn.textContent = 'Promote';
        confirmBtn.className = 'btn btn-success';
    } else {
        message.textContent = `Are you sure you want to demote ${userName} to User?`;
        confirmBtn.textContent = 'Demote';
        confirmBtn.className = 'btn btn-warning';
    }

    const confirmModal = new bootstrap.Modal(modal);
    confirmModal.show();
}

// Create the confirmation modal dynamically
function createConfirmModal() {
    const modalHTML = `
        <div class="modal fade" id="confirmModal" tabindex="-1" aria-labelledby="confirmModalLabel" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="confirmModalLabel">Confirm Action</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <p id="confirmMessage"></p>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                        <button type="button" class="btn" id="confirmBtn" onclick="executeAction()">Confirm</button>
                    </div>
                </div>
            </div>
        </div>
    `;

    document.body.insertAdjacentHTML('beforeend', modalHTML);
}

// Execute the confirmed action
function executeAction() {
    const confirmBtn = document.getElementById('confirmBtn');
    const originalContent = confirmBtn.textContent;

    confirmBtn.innerHTML = '<i data-feather="loader" class="feather-rotate me-1"></i>Processing...';
    confirmBtn.disabled = true;

    // Close modal
    const confirmModal = bootstrap.Modal.getInstance(document.getElementById('confirmModal'));
    confirmModal.hide();

    if (currentAction === 'promote') {
        promoteUser(currentUserId);
    } else {
        demoteUser(currentUserId);
    }

    // Reset button state after a delay
    setTimeout(() => {
        confirmBtn.textContent = originalContent;
        confirmBtn.disabled = false;
        if (typeof feather !== 'undefined') {
            feather.replace();
        }
    }, 1000);
}
function openConfirmFromElement(btn, action) {
    const userId = btn.getAttribute('data-id');
    const userName = btn.getAttribute('data-name');
    showConfirmModal(action, userId, userName);
}

// Updated role management functions with modal confirmation
function promoteUser(userId) {
    showToast('🔄 Processing promotion request...', 'info', 2000);

    fetch(`/admin/promote-user/${userId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => {
        if (response.ok) {
            return response.json();
        } else {
            throw new Error('Failed to promote user');
        }
    })
    .then(data => {
        if (data.success) {
            showToast('✅ User promoted to Admin successfully!', 'promotion', 4000);
            setTimeout(() => {
                location.reload();
            }, 1500);
        } else {
            throw new Error(data.message || 'Failed to promote user');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showToast('❌ Failed to promote user: ' + error.message, 'error', 5000);
    });
}

function demoteUser(userId) {
    showToast('🔄 Processing demotion request...', 'info', 2000);

    fetch(`/admin/demote-user/${userId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => {
        if (response.ok) {
            return response.json();
        } else {
            throw new Error('Failed to demote user');
        }
    })
    .then(data => {
        if (data.success) {
            showToast('⬇️ Admin demoted to User successfully!', 'demotion', 4000);
            setTimeout(() => {
                location.reload();
            }, 1500);
        } else {
            throw new Error(data.message || 'Failed to demote user');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showToast('❌ Failed to demote user: ' + error.message, 'error', 5000);
    });
}