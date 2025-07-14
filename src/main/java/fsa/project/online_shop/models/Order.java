package fsa.project.online_shop.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receiver_name")
    private String receiverName;

    @Column(name = "receiver_phone")
    private String receiverPhone;

    @Column(name = "receiver_email")
    private String receiverEmail;

    @Column(name = "receiver_address")
    private String receiverAddress;

    private String status;
    private Double sum;

    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    @Column(name = "transit_time")
    private LocalDateTime transitTime;

    @Column(name = "delivery_time")
    private LocalDateTime deliveryTime;

    @Column(name = "cancellation_time")
    private LocalDateTime cancellationTime;

    @Column(columnDefinition = "TEXT")
    private String note;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order")
    private Set<OrderItem> orderItems;
}
