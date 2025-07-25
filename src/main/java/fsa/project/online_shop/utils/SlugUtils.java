package fsa.project.online_shop.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class SlugUtils {
    
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern EDGESDHASHES = Pattern.compile("(^-|-$)");

    public static String generateSlug(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        slug = EDGESDHASHES.matcher(slug).replaceAll("");
        return slug.toLowerCase();
    }
    
    public static String generateUniqueSlug(String input, Long productId) {
        String baseSlug = generateSlug(input);
        // Don't append ID for cleaner URLs
        return baseSlug;
    }

    public static String generateSlugWithId(String input, Long productId) {
        String baseSlug = generateSlug(input);
        if (productId != null) {
            return baseSlug + "-" + productId;
        }
        return baseSlug;
    }
}
