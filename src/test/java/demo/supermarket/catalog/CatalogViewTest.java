package demo.supermarket.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogViewTest {

    private final List<CatalogCategory> categories = List.of(new CatalogCategory(1L, "Bakery"));
    private final List<CatalogProduct> products = List.of(new CatalogProduct(
        "bread", "Bread", "Fresh bread", "Bakery", "loaf", new BigDecimal("2.95"), null));

    @Test
    void preservesBeanPropertiesAndProductPresence() {
        final CatalogView catalog = new CatalogView(categories, products, 1L, "bread");

        assertThat(catalog.getCategories()).isSameAs(categories);
        assertThat(catalog.getProducts()).isSameAs(products);
        assertThat(catalog.getSelectedCategoryId()).isEqualTo(1L);
        assertThat(catalog.getSearch()).isEqualTo("bread");
        assertThat(catalog.hasProducts()).isTrue();
        assertThat(new CatalogView(categories, List.of(), null, "").hasProducts()).isFalse();
    }

    @Test
    void preservesValueEqualityAcrossAllProperties() {
        final CatalogView catalog = new CatalogView(categories, products, 1L, "bread");

        assertThat(catalog).isEqualTo(new CatalogView(List.copyOf(categories), List.copyOf(products), 1L, "bread"));
        assertThat(catalog).isNotEqualTo(new CatalogView(List.of(), products, 1L, "bread"));
        assertThat(catalog).isNotEqualTo(new CatalogView(categories, List.of(), 1L, "bread"));
        assertThat(catalog).isNotEqualTo(new CatalogView(categories, products, null, "bread"));
        assertThat(catalog).isNotEqualTo(new CatalogView(categories, products, 1L, ""));
        assertThat(catalog).isNotEqualTo(null).isNotEqualTo("bread");
        assertThat(catalog.hashCode()).isEqualTo(Objects.hash(categories, products, 1L, "bread"));
    }

    @Test
    void preservesNullValuesAndDiagnosticFormat() {
        final CatalogView catalog = new CatalogView(null, null, null, null);

        assertThat(catalog.getCategories()).isNull();
        assertThat(catalog.getProducts()).isNull();
        assertThat(catalog.getSelectedCategoryId()).isNull();
        assertThat(catalog.getSearch()).isNull();
        assertThat(catalog).isEqualTo(new CatalogView(null, null, null, null));
        assertThat(catalog.hashCode()).isEqualTo(Objects.hash(null, null, null, null));
        assertThat(catalog.toString())
            .isEqualTo("CatalogView{categories=null, products=null, selectedCategoryId=null, search='null'}");
        assertThat(new CatalogView(categories, products, 1L, "bread").toString())
            .isEqualTo("CatalogView{categories=" + categories + ", products=" + products
                + ", selectedCategoryId=1, search='bread'}");
    }
}
