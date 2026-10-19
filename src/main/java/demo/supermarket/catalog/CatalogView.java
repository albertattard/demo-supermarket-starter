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

    @Override
    public int hashCode() {
        return Objects.hash(categories, products, selectedCategoryId, search);
    }

    @Override
    public String toString() {
        return "CatalogView{" +
            "categories=" + categories +
            ", products=" + products +
            ", selectedCategoryId=" + selectedCategoryId +
            ", search='" + search + '\'' +
            '}';
    }
}
