package fsa.project.online_shop.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(nullable = true) // OAuth2 users don't have password
    private String password;

    @Column(length = 255)
    private String fullname;

    @Column(unique = true)
    private String email;

    private String phone;
    private Boolean status;
    private String provider;

    // Default delivery address fields
    @Column(name = "receiver_name", length = 255)
    private String receiverName;

    @Column(name = "receiver_phone")
    private String receiverPhone;

    @Column(columnDefinition = "TEXT")
    private String address;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToOne(mappedBy = "user")
    private Cart cart;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Order> orders;

    @PrePersist
    private void onCreate(){
        if(this.provider == null){
            this.provider = "LOCAL";
        }
    }
}
