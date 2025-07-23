package fsa.project.online_shop.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemResponse {
    private Long productId;
    private String productName;
    private String productImage;
    private Boolean productStatus;
    private Integer productQuantity;
    private Double price;
    private Double unitPrice;
    private Integer quantity;
}
