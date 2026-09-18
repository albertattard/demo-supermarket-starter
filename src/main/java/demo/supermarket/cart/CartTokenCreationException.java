package demo.supermarket.cart;

class CartTokenCreationException extends RuntimeException {

    CartTokenCreationException(final String message) {
        super(message);
    }

    CartTokenCreationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
