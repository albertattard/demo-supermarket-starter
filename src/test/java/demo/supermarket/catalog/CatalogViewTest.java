package demo.supermarket.catalog;

import module java.base;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogViewTest {

    @Test
    void retainsBeanAccessorsAndProductPresenceForTemplateRendering() {
        final List<CatalogCategory> categories = List.of(new CatalogCategory(1L, "Bakery"));
        final List<CatalogProduct> products = new ArrayList<>();
        final CatalogView catalog = new CatalogView(categories, products, 1L, "bread");

        assertThat(catalog.getCategories()).isSameAs(categories);
        assertThat(catalog.getProducts()).isSameAs(products);
        assertThat(catalog.getSelectedCategoryId()).isEqualTo(1L);
        assertThat(catalog.getSearch()).isEqualTo("bread");
        assertThat(catalog.hasProducts()).isFalse();

        products.add(new CatalogProduct("bread", "Bread", "Loaf", "Bakery", "500g",
            new BigDecimal("2.95"), null));

        assertThat(catalog.hasProducts()).isTrue();
    }

    @Test
    void comparesAllComponentsByValue() {
        final CatalogView catalog = new CatalogView(List.of(), List.of(), 1L, "bread");

        assertThat(catalog).isEqualTo(new CatalogView(List.of(), List.of(), 1L, "bread"));
        assertThat(catalog).isNotEqualTo(new CatalogView(null, List.of(), 1L, "bread"));
        assertThat(catalog).isNotEqualTo(new CatalogView(List.of(), null, 1L, "bread"));
        assertThat(catalog).isNotEqualTo(new CatalogView(List.of(), List.of(), 2L, "bread"));
        assertThat(catalog).isNotEqualTo(new CatalogView(List.of(), List.of(), 1L, "milk"));
        assertThat(catalog).isNotEqualTo(null);
        assertThat(catalog).isNotEqualTo("bread");
        assertThat(new CatalogView(null, null, null, null))
            .isEqualTo(new CatalogView(null, null, null, null));
    }

    @Test
    void preservesHashCalculationAndStringRepresentation() {
        final CatalogView catalog = new CatalogView(List.of(), List.of(), 1L, "bread");

        assertThat(catalog.hashCode()).isEqualTo(Objects.hash(List.of(), List.of(), 1L, "bread"));
        assertThat(catalog.toString()).isEqualTo(
            "CatalogView{categories=[], products=[], selectedCategoryId=1, search='bread'}");

        final CatalogView nullableCatalog = new CatalogView(null, null, null, null);

        assertThat(nullableCatalog.hashCode()).isEqualTo(Objects.hash(null, null, null, null));
        assertThat(nullableCatalog.toString()).isEqualTo(
            "CatalogView{categories=null, products=null, selectedCategoryId=null, search='null'}");
    }
}
