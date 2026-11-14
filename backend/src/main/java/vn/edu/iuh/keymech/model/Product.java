package vn.edu.iuh.keymech.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.edu.iuh.keymech.model.enums.VisibilityStatus;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 100)
    private String brand;

    @DecimalMin("1")
    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal price;

    @Min(0)
    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(length = 20)
    private String layout;

    @Column(name = "switch_type", length = 30)
    private String switchType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private VisibilityStatus status = VisibilityStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String specs;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
