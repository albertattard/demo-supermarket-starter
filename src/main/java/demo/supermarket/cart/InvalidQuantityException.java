package demo.supermarket.cart;

class InvalidQuantityException extends RuntimeException {

    InvalidQuantityException(final String message) {
        super(message);
    }
}
