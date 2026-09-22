package demo.supermarket.catalog;

import java.util.List;

public record CatalogView(
    List<CatalogCategory> categories,
    List<CatalogProduct> products,
    Long selectedCategoryId,
    String search) {

    public boolean hasProducts() {
        return !products.isEmpty();
    }

}
