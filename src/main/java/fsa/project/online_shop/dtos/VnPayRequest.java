package fsa.project.online_shop.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VnPayRequest {
    private Double amount;
//    private String orderInfo;
    private String receiverName;
    private String receiverPhone;
    private String receiverEmail;
    private String receiverAddress;
    private String note;
}
