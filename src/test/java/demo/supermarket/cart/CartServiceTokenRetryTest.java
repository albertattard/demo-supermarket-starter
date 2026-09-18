package demo.supermarket.cart;

import module java.base;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class CartServiceTokenRetryTest {

    private static final String RECOVERED_DUPLICATE_TOKEN = "duplicate-token-1234567890123456";
    private static final String RECOVERED_FRESH_TOKEN = "fresh-token-12345678901234567890";
    private static final String EXHAUSTED_DUPLICATE_TOKEN = "duplicate-token-abcdefghijklmno";

    @Autowired
    private CartService cartService;

    @Autowired
    private ProgrammableCartTokenGenerator tokenGenerator;

    @Test
    void startCartRetriesWhenGeneratedTokenAlreadyExists() {
        tokenGenerator.useTokens(
            RECOVERED_DUPLICATE_TOKEN,
            RECOVERED_DUPLICATE_TOKEN,
            RECOVERED_FRESH_TOKEN);

        final CartView existingCart = cartService.startCart("sourdough-country-loaf-500g");
        assertThat(existingCart.token()).isEqualTo(RECOVERED_DUPLICATE_TOKEN);

        final CartView retriedCart = cartService.startCart("sourdough-country-loaf-500g");

        assertThat(retriedCart.token()).isEqualTo(RECOVERED_FRESH_TOKEN);
        assertThat(retriedCart.quantityFor("sourdough-country-loaf-500g")).isEqualTo(1);
        assertThat(cartService.getActiveCart(RECOVERED_FRESH_TOKEN).quantityFor("sourdough-country-loaf-500g")).isEqualTo(1);
    }

    @Test
    void startCartFailsWhenGeneratedTokensKeepColliding() {
        tokenGenerator.useTokens(EXHAUSTED_DUPLICATE_TOKEN);
        cartService.startCart("sourdough-country-loaf-500g");

        tokenGenerator.useTokens(
            EXHAUSTED_DUPLICATE_TOKEN,
            EXHAUSTED_DUPLICATE_TOKEN,
            EXHAUSTED_DUPLICATE_TOKEN,
            EXHAUSTED_DUPLICATE_TOKEN,
            EXHAUSTED_DUPLICATE_TOKEN);

        assertThatThrownBy(() -> cartService.startCart("sourdough-country-loaf-500g"))
            .isInstanceOf(CartTokenCreationException.class)
            .hasMessage("Unable to create a unique cart token.");
    }

    @TestConfiguration
    static class TokenGeneratorConfiguration {

        @Bean
        @Primary
        ProgrammableCartTokenGenerator programmableCartTokenGenerator() {
            return new ProgrammableCartTokenGenerator();
        }
    }

    static final class ProgrammableCartTokenGenerator extends CartTokenGenerator {

        private final Queue<String> tokens = new ArrayDeque<>();
        private int fallbackToken;

        synchronized void useTokens(final String... tokens) {
            this.tokens.clear();
            this.tokens.addAll(List.of(tokens));
        }

        @Override
        synchronized String generate() {
            if (!tokens.isEmpty()) {
                return tokens.remove();
            }
            return "fallback-token-123456789012345" + fallbackToken++;
        }
    }
}
