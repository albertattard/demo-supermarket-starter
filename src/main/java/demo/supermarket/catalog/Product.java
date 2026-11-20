package demo.supermarket.catalog;

import module java.base;
import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "unit_label", nullable = false)
    private String unitLabel;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "image_path")
    private String imagePath;

    protected Product() {}

    Long getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    Category getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    String getDescription() {
        return description;
    }

    public String getUnitLabel() {
        return unitLabel;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    boolean isActive() {
        return active;
    }

    String getImagePath() {
        return imagePath;
    }
}
