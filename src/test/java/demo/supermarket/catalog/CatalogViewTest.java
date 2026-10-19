package demo.supermarket.catalog;

import module java.base;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogViewTest {

    @Test
    void retainsBeanAccessorsAndProductPresence() {
        final var categories = List.of(new CatalogCategory(1L, "Bakery"));
        final var products = new ArrayList<CatalogProduct>();
        final var view = new CatalogView(categories, products, 1L, "bread");

        assertThat(view.getCategories()).isSameAs(categories);
        assertThat(view.getProducts()).isSameAs(products);
        assertThat(view.getSelectedCategoryId()).isEqualTo(1L);
        assertThat(view.getSearch()).isEqualTo("bread");
        assertThat(view.hasProducts()).isFalse();

        products.add(new CatalogProduct("bread", "Bread", "Loaf", "Bakery", "each",
            new BigDecimal("2.95"), null));

        assertThat(view.hasProducts()).isTrue();
    }

    @Test
    void comparesAllComponentsByValue() {
        final var view = new CatalogView(List.of(new CatalogCategory(1L, "Bakery")), List.of(), 1L, "bread");
        final var equalView = new CatalogView(List.of(new CatalogCategory(1L, "Bakery")), List.of(), 1L, "bread");

        assertThat(view).isEqualTo(equalView);
        assertThat(view.hashCode()).isEqualTo(equalView.hashCode());
        assertThat(view).isNotEqualTo(new CatalogView(List.of(), List.of(), 1L, "bread"));
        assertThat(view).isNotEqualTo(new CatalogView(view.getCategories(), List.of(), 2L, "bread"));
        assertThat(view).isNotEqualTo(new CatalogView(view.getCategories(), List.of(), 1L, "milk"));
        assertThat(view).isNotEqualTo(new CatalogView(view.getCategories(), List.of(
            new CatalogProduct("bread", "Bread", "Loaf", "Bakery", "each", BigDecimal.ONE, null)),
            1L, "bread"));
        assertThat(view).isNotEqualTo(null).isNotEqualTo("bread");
    }

    @Test
    void retainsNullHandlingHashCalculationAndStringFormat() {
        final var view = new CatalogView(List.of(), List.of(), null, "");

        assertThat(view.hashCode()).isEqualTo(Objects.hash(List.of(), List.of(), null, ""));
        assertThat(view.toString()).isEqualTo("CatalogView{categories=[], products=[], selectedCategoryId=null, search=''}");

        final var nullView = new CatalogView(null, null, null, null);
        assertThat(nullView).isEqualTo(new CatalogView(null, null, null, null));
        assertThat(nullView.hashCode()).isEqualTo(Objects.hash(null, null, null, null));
        assertThat(nullView.toString()).isEqualTo("CatalogView{categories=null, products=null, selectedCategoryId=null, search='null'}");
    }
}
