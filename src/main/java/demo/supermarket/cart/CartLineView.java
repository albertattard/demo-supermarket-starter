package demo.supermarket.cart;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public record CartLineView(
        String productSlug,
        String productName,
        String unitLabel,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal) {

    public String displayUnitPrice() {
        return currency(unitPrice);
    }

    public String displayLineTotal() {
        return currency(lineTotal);
    }

    private static String currency(final BigDecimal amount) {
        return NumberFormat.getCurrencyInstance(Locale.GERMANY).format(amount);
    }
}
