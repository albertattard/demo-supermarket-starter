package demo.supermarket.cart;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.Objects;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import demo.supermarket.catalog.Product;
import demo.supermarket.catalog.ProductRepository;

@Service
public class CartService {

    static final int MIN_QUANTITY = 1;
    static final int MAX_QUANTITY = 99;
    private static final int TOKEN_CREATION_ATTEMPTS = 5;

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartTokenGenerator tokenGenerator;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    CartService(
            final CartRepository cartRepository,
            final ProductRepository productRepository,
            final CartTokenGenerator tokenGenerator,
            final PlatformTransactionManager transactionManager,
            final Clock clock) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.tokenGenerator = tokenGenerator;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.clock = clock;
    }

    public CartView startCart(final String productSlug) {
        final Product initialProduct = hasText(productSlug) ? findActiveProduct(productSlug.trim()) : null;
        final CartView emptyCart = createEmptyCartWithUniqueToken();
        if (initialProduct == null) {
            return emptyCart;
        }
        return addInitialProduct(emptyCart.token(), initialProduct);
    }

    private CartView createEmptyCartWithUniqueToken() {
        for (int attempt = 1; attempt <= TOKEN_CREATION_ATTEMPTS; attempt++) {
            try {
                return Objects.requireNonNull(transactionTemplate.execute(status -> {
                    final Cart cart = cartRepository.saveAndFlush(new Cart(tokenGenerator.generate(), now()));
                    return toView(cart);
                }));
            } catch (final DataIntegrityViolationException ex) {
                if (attempt == TOKEN_CREATION_ATTEMPTS) {
                    throw new CartTokenCreationException("Unable to create a unique cart token.", ex);
                }
            }
        }
        throw new CartTokenCreationException("Unable to create a unique cart token.");
    }

    @Transactional(readOnly = true)
    public CartView getActiveCart(final String token) {
        return toView(findActiveCart(token));
    }

    @Transactional
    public CartView addProduct(final String token, final String productSlug) {
        final Cart cart = findActiveCartForMutation(token);
        addProduct(cart, findActiveProduct(productSlug));
        return toView(cart);
    }

    @Transactional
    public CartView updateQuantity(final String token, final String productSlug, final String submittedQuantity) {
        final Cart cart = findActiveCartForMutation(token);
        final Product product = findActiveProduct(productSlug);
        final int quantity = parseQuantity(submittedQuantity);
        cart.findItemByProductSlug(product.getSlug())
                .ifPresent(item -> item.setQuantity(quantity));
        cart.touch(now());
        return toView(cart);
    }

    @Transactional
    public CartView removeProduct(final String token, final String productSlug) {
        final Cart cart = findActiveCartForMutation(token);
        findActiveProduct(productSlug);
        cart.findItemByProductSlug(productSlug).ifPresent(cart::removeItem);
        cart.touch(now());
        return toView(cart);
    }

    private void addProduct(final Cart cart, final Product product) {
        final CartItem item = cart.findItemByProductSlug(product.getSlug()).orElse(null);
        if (item == null) {
            cart.addItem(new CartItem(product, MIN_QUANTITY));
        } else if (item.getQuantity() < MAX_QUANTITY) {
            item.setQuantity(item.getQuantity() + 1);
        } else {
            throw new InvalidQuantityException("Quantity must be between 1 and 99.");
        }
        cart.touch(now());
    }

    private CartView addInitialProduct(final String token, final Product product) {
        return Objects.requireNonNull(transactionTemplate.execute(status -> {
            final Cart cart = findActiveCartForMutation(token);
            addProduct(cart, product);
            return toView(cart);
        }));
    }

    private Cart findActiveCart(final String token) {
        return cartRepository.findByTokenAndState(token, CartState.ACTIVE)
                .orElseThrow(CartNotFoundException::new);
    }

    private Cart findActiveCartForMutation(final String token) {
        return cartRepository.findWithLockingByTokenAndState(token, CartState.ACTIVE)
                .orElseThrow(CartNotFoundException::new);
    }

    private Product findActiveProduct(final String productSlug) {
        if (!hasText(productSlug)) {
            throw new ProductNotFoundException();
        }
        return productRepository.findBySlugAndActiveTrue(productSlug)
                .orElseThrow(ProductNotFoundException::new);
    }

    private int parseQuantity(final String submittedQuantity) {
        try {
            final int quantity = Integer.parseInt(submittedQuantity);
            if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
                throw new InvalidQuantityException("Quantity must be between 1 and 99.");
            }
            return quantity;
        } catch (final NumberFormatException ex) {
            throw new InvalidQuantityException("Quantity must be a whole number between 1 and 99.");
        }
    }

    private CartView toView(final Cart cart) {
        final var lines = cart.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getProduct().getName()))
                .map(item -> {
                    final Product product = item.getProduct();
                    final BigDecimal lineTotal = product.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    return new CartLineView(
                            product.getSlug(),
                            product.getName(),
                            product.getUnitLabel(),
                            product.getUnitPrice(),
                            item.getQuantity(),
                            lineTotal);
                })
                .toList();
        final BigDecimal subtotal = lines.stream()
                .map(CartLineView::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartView(cart.getToken(), lines, subtotal);
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(clock);
    }

    private static boolean hasText(final String value) {
        return value != null && !value.isBlank();
    }
}
