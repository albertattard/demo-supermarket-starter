package demo.supermarket.cart;

import module java.base;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CartServiceConcurrencyTest {

    @Autowired
    private CartService cartService;

    @Test
    void concurrentAddsIncrementFromLatestPersistedQuantity() throws Exception {
        final CartView startedCart = cartService.startCart("sourdough-country-loaf-500g");
        final CountDownLatch ready = new CountDownLatch(2);
        final CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            final Future<?> first = executor.submit(() -> addProductWhenReleased(startedCart.token(), ready, start));
            final Future<?> second = executor.submit(() -> addProductWhenReleased(startedCart.token(), ready, start));

            ready.await();
            start.countDown();
            first.get();
            second.get();
        }

        final CartView cart = cartService.getActiveCart(startedCart.token());
        assertThat(cart.quantityFor("sourdough-country-loaf-500g")).isEqualTo(3);
        assertThat(cart.subtotal()).isEqualByComparingTo("8.85");
    }

    private void addProductWhenReleased(
        final String cartToken,
        final CountDownLatch ready,
        final CountDownLatch start) {
        try {
            ready.countDown();
            start.await();
            cartService.addProduct(cartToken, "sourdough-country-loaf-500g");
        } catch (final Exception ex) {
            throw new AssertionError(ex);
        }
    }
}
