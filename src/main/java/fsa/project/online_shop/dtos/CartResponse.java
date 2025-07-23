package fsa.project.online_shop.dtos;

import fsa.project.online_shop.models.Cart;
import fsa.project.online_shop.utils.CartMapper;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartResponse {
    private Long cartId;
    private Double sum;
    private List<CartItemResponse> cartItems;
}
