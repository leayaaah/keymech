package vn.edu.iuh.keymech.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.edu.iuh.keymech.model.enums.CustomStatus;

@Entity
@Table(name = "custom_requests")
@Getter
@Setter
@NoArgsConstructor
public class CustomRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_code", nullable = false, length = 30)
    private String requestCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String layout;

    @Column(name = "case_material", length = 20)
    private String caseMaterial;

    @Column(name = "switch_type", nullable = false, length = 80)
    private String switchType;

    @Column(name = "keycap_set", length = 120)
    private String keycapSet;

    @Column(nullable = false)
    private boolean lubing;

    @Column(name = "hot_swap", nullable = false)
    private boolean hotSwap = true;

    @Column(name = "rgb_option", length = 20)
    private String rgbOption;

    @Column(precision = 12, scale = 0)
    private BigDecimal budget;

    @Column(length = 1000)
    private String note;

    @Column(name = "quoted_price", precision = 12, scale = 0)
    private BigDecimal quotedPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private CustomStatus status = CustomStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
