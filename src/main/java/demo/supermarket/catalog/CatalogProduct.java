package demo.supermarket.catalog;

import module java.base;

public record CatalogProduct(
    String slug,
    String name,
    String description,
    String categoryName,
    String unitLabel,
    BigDecimal unitPrice,
    String imagePath) {

    public String displayImagePath() {
        return imagePath == null || imagePath.isBlank()
            ? "/images/product-placeholder.svg"
            : imagePath;
    }

    public String displayUnitPrice() {
        return NumberFormat.getCurrencyInstance(Locale.GERMANY).format(unitPrice);
    }
}
