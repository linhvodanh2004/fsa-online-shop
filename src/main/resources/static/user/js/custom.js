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