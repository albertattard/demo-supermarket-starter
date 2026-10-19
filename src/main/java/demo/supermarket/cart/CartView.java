package demo.supermarket.cart;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public record CartView(String token, List<CartLineView> lines, BigDecimal subtotal) {

    public boolean empty() {
        return lines.isEmpty();
    }

    public int totalQuantity() {
        return lines.stream()
                .mapToInt(CartLineView::quantity)
                .sum();
    }

    public String displaySubtotal() {
        return NumberFormat.getCurrencyInstance(Locale.GERMANY).format(subtotal);
    }

    public int quantityFor(final String productSlug) {
        return lines.stream()
                .filter(line -> line.productSlug().equals(productSlug))
                .mapToInt(CartLineView::quantity)
                .findFirst()
                .orElse(0);
    }
}
