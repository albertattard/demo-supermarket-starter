package demo.supermarket.catalog;

import module java.base;

public record CatalogView(
    List<CatalogCategory> categories,
    List<CatalogProduct> products,
    Long selectedCategoryId,
    String search) {

    public List<CatalogCategory> getCategories() {
        return categories;
    }

    public List<CatalogProduct> getProducts() {
        return products;
    }

    public boolean hasProducts() {
        return !products.isEmpty();
    }

    public Long getSelectedCategoryId() {
        return selectedCategoryId;
    }

    public String getSearch() {
        return search;
    }

}
