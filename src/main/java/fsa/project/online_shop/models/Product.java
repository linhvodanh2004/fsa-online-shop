package fsa.project.online_shop.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Column(unique = true)
    private String slug;

    private Double price;
    private String image;

    @Column(columnDefinition = "TEXT")
    @JsonIgnore // Don't include full description in search results
    private String description;

    @ManyToOne(fetch = FetchType.EAGER) // Use EAGER to avoid lazy loading issues
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({"products"}) // Prevent circular reference
    private Category category;

    private Boolean status;
    private Integer quantity;

    @JsonIgnore // Don't expose sold count in search results
    private Integer sold;

    // Add computed property for category name
    @JsonProperty("categoryName")
    public String getCategoryName() {
        return category != null ? category.getName() : null;
    }
}