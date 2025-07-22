package fsa.project.online_shop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class DotenvTest {

    @Value("${DB_URL:}")
    private String dbUrl;

    @Value("${GOOGLE_CLIENT_ID:}")
    private String googleClientId;

    @Test
    public void testDotenvVariablesAreLoaded() {
        System.out.println("DB_URL: " + dbUrl);
        System.out.println("GOOGLE_CLIENT_ID: " + googleClientId);
        
        // Kiểm tra xem các biến từ .env có được load không
        assertNotNull(dbUrl, "DB_URL should be loaded from .env file");
        assertNotNull(googleClientId, "GOOGLE_CLIENT_ID should be loaded from .env file");
    }
}
