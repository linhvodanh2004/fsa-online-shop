package fsa.project.online_shop.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VnPayRequest {
    private Long amount;
    private String orderInfo;
}
