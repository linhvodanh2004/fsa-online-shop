// Horizontal Collection List Functionality
$(document).ready(function() {
    var $scrollArea = $('#collectionScroll');
    var $prevBtn = $('#prevBtn');
    var $nextBtn = $('#nextBtn');
    var autoScrollInterval;

    function scrollCollection(direction) {
        var cardWidth = 280 + 24; // card width + gap
        var scrollAmount = cardWidth * 2; // scroll 2 cards at a time

        $scrollArea.animate({
            scrollLeft: $scrollArea.scrollLeft() + (direction * scrollAmount)
        }, 500);
    }

    function updateButtonStates() {
        var isAtStart = $scrollArea.scrollLeft() <= 0;
        var isAtEnd = $scrollArea.scrollLeft() >= $scrollArea[0].scrollWidth - $scrollArea.width();

        $prevBtn.prop('disabled', isAtStart);
        $nextBtn.prop('disabled', isAtEnd);
    }

    // Update button states on scroll
    $scrollArea.on('scroll', updateButtonStates);

    // Initialize button states
    updateButtonStates();

    // Auto-scroll functionality
    function startAutoScroll() {
        autoScrollInterval = setInterval(function() {
            if ($scrollArea.scrollLeft() >= $scrollArea[0].scrollWidth - $scrollArea.width()) {
                $scrollArea.animate({ scrollLeft: 0 }, 500);
            } else {
                scrollCollection(1);
            }
        }, 5000);
    }

    function stopAutoScroll() {
        clearInterval(autoScrollInterval);
    }

    // Start auto-scroll on page load
    startAutoScroll();

    // Pause auto-scroll on hover
    $scrollArea.on('mouseenter', stopAutoScroll);
    $scrollArea.on('mouseleave', startAutoScroll);

    // Button click handlers
    $prevBtn.on('click', function() {
        scrollCollection(-1);
    });

    $nextBtn.on('click', function() {
        scrollCollection(1);
    });

    // Handle touch/swipe on mobile
    var startX, scrollLeft;

    $scrollArea.on('touchstart', function(e) {
        startX = e.originalEvent.touches[0].pageX - $scrollArea.offset().left;
        scrollLeft = $scrollArea.scrollLeft();
    });

    $scrollArea.on('touchmove', function(e) {
        if (!startX) return;

        var x = e.originalEvent.touches[0].pageX - $scrollArea.offset().left;
        var walk = (x - startX) * 2;
        $scrollArea.scrollLeft(scrollLeft - walk);
    });

    $scrollArea.on('touchend', function() {
        startX = null;
    });
});

// Enhanced Search Modal JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // DOM Elements
    const searchModal = document.getElementById('templatemo_search');
    const searchInput = document.getElementById('inputModalSearch');
    const searchForm = document.getElementById('searchForm');
    const searchResults = document.getElementById('searchResults');
    const loadingSpinner = document.querySelector('.loading-spinner');
    const categoriesSection = document.getElementById('categoriesSection');
    const productsSection = document.getElementById('productsSection');
    const categoriesList = document.getElementById('categoriesList');
    const productsList = document.getElementById('productsList');
    const noResults = document.getElementById('noResults');
    const initialState = document.getElementById('initialState');

    // Search state
    let searchTimeout;
    let currentQuery = '';

    // Search icon element (for opening modal)
    const searchIcon = document.getElementById('searchIcon');

    // Initialize
    init();

    function init() {
        hideAllSections();
        showInitialState();
        bindEvents();
        bindSearchIcon();
    }

    function bindSearchIcon() {
        // Handle search icon click to open modal
        if (searchIcon) {
            searchIcon.addEventListener('click', function(e) {
                e.preventDefault();
                openSearchModal();
            });
        }
    }

    function openSearchModal() {
        const modal = new bootstrap.Modal(searchModal);
        modal.show();
    }

    function bindEvents() {
        // Real-time search on input
        searchInput.addEventListener('input', handleSearchInput);

        // Handle form submission
        searchForm.addEventListener('submit', handleFormSubmit);

        // Clear search when modal is closed
        searchModal.addEventListener('hidden.bs.modal', clearSearch);

        // Focus input when modal opens
        searchModal.addEventListener('shown.bs.modal', function() {
            searchInput.focus();
        });
    }

    function handleSearchInput(e) {
        const query = e.target.value.trim();

        // Clear previous timeout
        if (searchTimeout) {
            clearTimeout(searchTimeout);
        }

        // If query is empty, show initial state
        if (query === '') {
            hideAllSections();
            showInitialState();
            currentQuery = '';
            return;
        }

        // Debounce search requests
        searchTimeout = setTimeout(() => {
            if (query !== currentQuery) {
                currentQuery = query;
                performSearch(query);
            }
        }, 300);
    }

    function handleFormSubmit(e) {
        e.preventDefault();
        const query = searchInput.value.trim();

        if (query) {
            // Close modal and redirect to shop page with search query
            const modal = bootstrap.Modal.getInstance(searchModal);
            modal.hide();
            window.location.href = `/shop?q=${encodeURIComponent(query)}`;
        }
    }

    async function performSearch(query) {
        try {
            showLoading();

            const response = await fetch(`/search?q=${encodeURIComponent(query)}`);

            if (!response.ok) {
                throw new Error('Search request failed');
            }

            const data = await response.json();
            displayResults(data, query);

        } catch (error) {
            console.error('Search error:', error);
            showError();
        } finally {
            hideLoading();
        }
    }

    function displayResults(data, query) {
        hideAllSections();

        const hasCategories = data.categories && data.categories.length > 0;
        const hasProducts = data.products && data.products.length > 0;

        if (!hasCategories && !hasProducts) {
            showNoResults();
            return;
        }

        if (hasCategories) {
            displayCategories(data.categories, query);
        }

        if (hasProducts) {
            displayProducts(data.products, query);
        }
    }

    function displayCategories(categories, query) {
        categoriesList.innerHTML = '';

        categories.forEach(category => {
            const categoryElement = createCategoryElement(category, query);
            categoriesList.appendChild(categoryElement);
        });

        categoriesSection.style.display = 'block';
    }

    function displayProducts(products, query) {
        productsList.innerHTML = '';

        products.forEach(product => {
            const productElement = createProductElement(product, query);
            productsList.appendChild(productElement);
        });

        productsSection.style.display = 'block';
    }

    function createCategoryElement(category, query) {
        const div = document.createElement('div');
        div.className = 'search-result-item category-item p-2 mb-2 border rounded cursor-pointer';
        div.setAttribute('data-category-id', category.id);

        const highlightedName = highlightText(category.name, query);

        div.innerHTML = `
            <div class="d-flex align-items-center">
                <i class="fas fa-folder text-success me-2"></i>
                <div class="flex-grow-1">
                    <div class="fw-semibold">${highlightedName}</div>
                    ${category.description ? `<small class="text-muted">${escapeHtml(category.description)}</small>` : ''}
                </div>
                <small class="text-muted ms-2">Category</small>
            </div>
        `;

        // Add click handler
        div.addEventListener('click', () => {
            const modal = bootstrap.Modal.getInstance(searchModal);
            modal.hide();
            window.location.href = `/shop?category=${category.id}`;
        });

        // Add hover effects
        div.addEventListener('mouseenter', () => {
            div.classList.add('bg-light');
        });

        div.addEventListener('mouseleave', () => {
            div.classList.remove('bg-light');
        });

        return div;
    }

    function createProductElement(product, query) {
        const div = document.createElement('div');
        div.className = 'search-result-item product-item p-2 mb-2 border rounded cursor-pointer';
        div.setAttribute('data-product-id', product.id);

        const highlightedName = highlightText(product.name, query);
        const price = product.price ? `$${parseFloat(product.price).toFixed(2)}` : '';

        div.innerHTML = `
            <div class="d-flex align-items-center">
                <div class="me-3">
                    ${product.imageUrl ?
                        `<img src="${escapeHtml(product.imageUrl)}" alt="${escapeHtml(product.name)}"
                             class="rounded" style="width: 40px; height: 40px; object-fit: cover;">` :
                        `<div class="bg-light rounded d-flex align-items-center justify-content-center"
                             style="width: 40px; height: 40px;">
                            <i class="fas fa-image text-muted"></i>
                         </div>`
                    }
                </div>
                <div class="flex-grow-1">
                    <div class="fw-semibold">${highlightedName}</div>
                    ${product.description ? `<small class="text-muted">${escapeHtml(truncateText(product.description, 60))}</small>` : ''}
                </div>
                <div class="text-end ms-2">
                    ${price ? `<div class="fw-bold text-success">${price}</div>` : ''}
                    <small class="text-muted">Product</small>
                </div>
            </div>
        `;

        // Add click handler
        div.addEventListener('click', () => {
            const modal = bootstrap.Modal.getInstance(searchModal);
            modal.hide();
            // Use slug if available, otherwise fall back to ID
            const productPath = product.slug ? `/product/${product.slug}` : `/shop/product/${product.id}`;
            window.location.href = productPath;
        });

        // Add hover effects
        div.addEventListener('mouseenter', () => {
            div.classList.add('bg-light');
        });

        div.addEventListener('mouseleave', () => {
            div.classList.remove('bg-light');
        });

        return div;
    }

    function highlightText(text, query) {
        if (!text || !query) return escapeHtml(text || '');

        const escapedText = escapeHtml(text);
        const escapedQuery = escapeHtml(query);
        const regex = new RegExp(`(${escapedQuery.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi');

        return escapedText.replace(regex, '<mark class="bg-warning">$1</mark>');
    }

    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function truncateText(text, maxLength) {
        if (!text || text.length <= maxLength) return text;
        return text.substring(0, maxLength) + '...';
    }

    function showLoading() {
        hideAllSections();
        loadingSpinner.style.display = 'block';
    }

    function hideLoading() {
        loadingSpinner.style.display = 'none';
    }

    function showInitialState() {
        initialState.style.display = 'block';
    }

    function showNoResults() {
        noResults.style.display = 'block';
    }

    function showError() {
        hideAllSections();
        noResults.innerHTML = `
            <i class="fas fa-exclamation-triangle fa-2x mb-2 text-warning"></i>
            <p>Something went wrong. Please try again.</p>
        `;
        noResults.style.display = 'block';
    }

    function hideAllSections() {
        loadingSpinner.style.display = 'none';
        categoriesSection.style.display = 'none';
        productsSection.style.display = 'none';
        noResults.style.display = 'none';
        initialState.style.display = 'none';
    }

    function clearSearch() {
        searchInput.value = '';
        currentQuery = '';
        hideAllSections();
        showInitialState();

        if (searchTimeout) {
            clearTimeout(searchTimeout);
        }
    }
});
