package demo.supermarket.cart;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "carts")
class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CartState state;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CartItem> items = new ArrayList<>();

    protected Cart() {
    }

    Cart(final String token, final OffsetDateTime now) {
        this.token = token;
        this.state = CartState.ACTIVE;
        this.createdAt = now;
        this.updatedAt = now;
    }

    String getToken() {
        return token;
    }

    List<CartItem> getItems() {
        return items;
    }

    void touch(final OffsetDateTime now) {
        this.updatedAt = now;
    }

    Optional<CartItem> findItemByProductSlug(final String productSlug) {
        return items.stream()
                .filter(item -> item.getProduct().getSlug().equals(productSlug))
                .findFirst();
    }

    void addItem(final CartItem item) {
        items.add(item);
        item.attachTo(this);
    }

    void removeItem(final CartItem item) {
        items.remove(item);
        item.attachTo(null);
    }
}
