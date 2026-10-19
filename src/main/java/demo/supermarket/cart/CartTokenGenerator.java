package demo.supermarket.cart;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;

@Component
class CartTokenGenerator {

    private static final int TOKEN_BYTES = 24;

    private final SecureRandom secureRandom = new SecureRandom();

    String generate() {
        final byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
