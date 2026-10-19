package demo.supermarket.catalog;

import module java.base;

public class CatalogView {

    private final List<CatalogCategory> categories;
    private final List<CatalogProduct> products;
    private final Long selectedCategoryId;
    private final String search;

    public CatalogView(final List<CatalogCategory> categories, final List<CatalogProduct> products, final Long selectedCategoryId, final String search) {
        this.categories = categories;
        this.products = products;
        this.selectedCategoryId = selectedCategoryId;
        this.search = search;
    }

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
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        final CatalogView that = (CatalogView) object;
        return Objects.equals(categories, that.categories)
            && Objects.equals(products, that.products)
            && Objects.equals(selectedCategoryId, that.selectedCategoryId)
            && Objects.equals(search, that.search);
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
