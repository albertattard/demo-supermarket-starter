package demo.supermarket.cart;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

interface CartRepository extends JpaRepository<Cart, Long> {

    @EntityGraph(attributePaths = { "items", "items.product" })
    Optional<Cart> findByTokenAndState(String token, CartState state);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Cart> findWithLockingByTokenAndState(String token, CartState state);
}
