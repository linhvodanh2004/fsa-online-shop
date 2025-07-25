// Search functionality
document.addEventListener("DOMContentLoaded", function () {
    const searchBox = document.getElementById('userSearchBox');
    const table = document.getElementById('datatables-column-search-text-inputs');

    function applyFilters() {
        const filter = searchBox.value.toLowerCase();
        const roleFilter = document.getElementById('roleFilter').value;
        const providerFilter = document.getElementById('providerFilter').value;
        const rows = table.querySelectorAll('tbody tr:not(:last-child)'); // Exclude the add row

        rows.forEach(row => {
            const text = row.textContent.toLowerCase();
            const roleText = row.children[5].textContent.trim();
            const providerText = row.children[6].textContent.trim();

            const matchesSearch = text.includes(filter);
            const matchesRole = !roleFilter || roleText === roleFilter;
            const matchesProvider = !providerFilter || providerText === providerFilter;

            row.style.display = (matchesSearch && matchesRole && matchesProvider) ? '' : 'none';
        });
    }

    searchBox.addEventListener('keyup', applyFilters);
    document.getElementById('roleFilter').addEventListener('change', applyFilters);
    document.getElementById('providerFilter').addEventListener('change', applyFilters);
});
document.addEventListener('DOMContentLoaded', function () {
    const currentPath = window.location.pathname;
    const sidebarItems = document.querySelectorAll('.sidebar-item');
    let activeSet = false;
    console.log(currentPath);
    console.log(sidebarItems);

    sidebarItems.forEach(item => {
        item.classList.remove('active');
        const link = item.querySelector('.sidebar-link');
        const href = link.getAttribute('href');
        if (href && href !== '#' && (currentPath.startsWith(href) || currentPath.includes(href))) {
            item.classList.add('active');
            activeSet = true;
        }
    });
    if (!activeSet) {
        sidebarItems[0].classList.add('active');
    }
});
// Sort functionality
document.addEventListener("DOMContentLoaded", function () {
    const sortSelect = document.getElementById('sortSelect');
    const table = document.getElementById('datatables-column-search-text-inputs');

    sortSelect.addEventListener('change', function () {
        const rows = Array.from(table.querySelectorAll('tbody tr:not(:last-child)')); // Exclude the add row
        let compare;

        switch (sortSelect.value) {
            case 'username-asc':
                compare = (a, b) => a.children[1].textContent.localeCompare(b.children[1].textContent);
                break;
            case 'username-desc':
                compare = (a, b) => b.children[1].textContent.localeCompare(a.children[1].textContent);
                break;
            case 'fullname-asc':
                compare = (a, b) => a.children[2].textContent.localeCompare(b.children[2].textContent);
                break;
            case 'fullname-desc':
                compare = (a, b) => b.children[2].textContent.localeCompare(a.children[2].textContent);
                break;
            case 'email-asc':
                compare = (a, b) => a.children[3].textContent.localeCompare(b.children[3].textContent);
                break;
            case 'email-desc':
                compare = (a, b) => b.children[3].textContent.localeCompare(a.children[3].textContent);
                break;
            case 'role-asc':
                compare = (a, b) => a.children[5].textContent.localeCompare(b.children[5].textContent);
                break;
            case 'role-desc':
                compare = (a, b) => b.children[5].textContent.localeCompare(a.children[5].textContent);
                break;
            default:
                compare = null;
        }

        if (compare) {
            rows.sort(compare);
            const tbody = table.querySelector('tbody');
            const addRow = tbody.querySelector('tr:last-child'); // Keep the add row at the end
            rows.forEach(row => tbody.appendChild(row));
            tbody.appendChild(addRow);
        }
    });
});

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
        body: JSON.stringify({status: newStatus})
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

document.getElementById("addUserForm").addEventListener("submit", function (e) {
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


document.getElementById("updateUserForm").addEventListener("submit", function (e) {
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

