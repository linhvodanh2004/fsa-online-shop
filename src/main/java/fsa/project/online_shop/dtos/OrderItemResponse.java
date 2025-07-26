package fsa.project.online_shop.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
    private Long productId;
    private String productName;
    private String productImage;
    private Double price;
    private Double unitPrice;
    private Integer quantity;
}
