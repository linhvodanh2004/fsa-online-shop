document.addEventListener("DOMContentLoaded", function () {
    const currentPath = window.location.pathname;
    const sidebarItems = document.querySelectorAll(".sidebar-item");
    let matched = false;

    sidebarItems.forEach(item => {
        const link = item.querySelector("a.sidebar-link");
        if (!link) return;

        const href = link.getAttribute("href");

        if (currentPath.startsWith(href) && href !== "/admin/dashboard") {
            // Remove all active classes first
            document.querySelectorAll(".sidebar-item.active").forEach(el => el.classList.remove("active"));

            item.classList.add("active");
            matched = true;
        }
    });

    if (!matched) {
        sidebarItems[0].classList.add("active");
    }
    // If no match found, do nothing (Dashboard remains active from HTML)
});