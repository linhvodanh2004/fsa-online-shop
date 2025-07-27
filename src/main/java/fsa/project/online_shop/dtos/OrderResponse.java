package fsa.project.online_shop.dtos;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long orderId;
    private String username;
    private String receiverName;
    private String receiverPhone;
    private String receiverEmail;
    private String receiverAddress;
    private String note;
    private String status;
    private Double sum;
    private LocalDateTime creationTime;
    private LocalDateTime transitTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime cancellationTime;
    private String paymentMethod;
    private Boolean paymentStatus;
    private String paymentTransactionId;
    private LocalDateTime paymentDate;
    private List<OrderItemResponse> orderItems;
}
