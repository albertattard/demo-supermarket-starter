package demo.supermarket.cart;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class CartConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
