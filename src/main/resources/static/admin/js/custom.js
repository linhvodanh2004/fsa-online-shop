// Pagination functionality
let currentPage = 1;
let itemsPerPage = 5;
let allUsers = [];
let filteredUsers = [];

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
        searchBox.addEventListener('keyup', applyFilters); // Keep both for compatibility
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
    if (!tableBody) return;
    
    const rows = tableBody.querySelectorAll('tr:not(:last-child)'); // Exclude the "add user" row
    
    allUsers = [];
    rows.forEach(row => {
        if (row.cells.length > 0) {
            allUsers.push({
                element: row.cloneNode(true),
                username: row.cells[1].textContent.trim(),
                fullname: row.cells[2].textContent.trim(),
                email: row.cells[3].textContent.trim(),
                phone: row.cells[4].textContent.trim(),
                role: row.cells[5].textContent.trim(),
                provider: row.cells[6].textContent.trim(),
                status: row.cells[7].querySelector('i') ? row.cells[7].querySelector('i').getAttribute('data-status') === 'true' : false
            });
        }
    });
}

function applyFilters() {
    const searchBox = document.getElementById('userSearchBox');
    const sortSelect = document.getElementById('sortSelect');
    const roleFilter = document.getElementById('roleFilter');
    const providerFilter = document.getElementById('providerFilter');
    
    const searchTerm = searchBox ? searchBox.value.toLowerCase() : '';
    const sortValue = sortSelect ? sortSelect.value : '';
    const roleFilterValue = roleFilter ? roleFilter.value : '';
    const providerFilterValue = providerFilter ? providerFilter.value : '';
    
    // Filter users
    filteredUsers = allUsers.filter(user => {
        const matchesSearch = user.username.toLowerCase().includes(searchTerm) ||
                            user.fullname.toLowerCase().includes(searchTerm) ||
                            user.email.toLowerCase().includes(searchTerm);
        
        const matchesRole = !roleFilterValue || user.role === roleFilterValue;
        const matchesProvider = !providerFilterValue || user.provider === providerFilterValue;
        
        return matchesSearch && matchesRole && matchesProvider;
    });
    
    // Sort users
    if (sortValue) {
        const [field, direction] = sortValue.split('-');
        filteredUsers.sort((a, b) => {
            let aValue = a[field].toLowerCase();
            let bValue = b[field].toLowerCase();
            
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
    if (!tableBody) return;
    
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const pageUsers = filteredUsers.slice(startIndex, endIndex);
    
    // Clear table body except for the "add user" row
    const addUserRow = tableBody.querySelector('tr:last-child');
    tableBody.innerHTML = '';
    
    // Add filtered users for current page
    pageUsers.forEach(user => {
        tableBody.appendChild(user.element.cloneNode(true));
    });
    
    // Re-add the "add user" row if it exists
    if (addUserRow) {
        tableBody.appendChild(addUserRow);
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

    fetch(`/admin/user/${userId}/status`, {
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

        fetch(`/admin/user/${userId}/delete`, {
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