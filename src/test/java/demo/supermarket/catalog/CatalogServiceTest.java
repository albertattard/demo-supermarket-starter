package demo.supermarket.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CatalogServiceTest {

    @Autowired
    private CatalogService catalogService;

    @Test
    void treatsPercentageAsLiteralSearchText() {
        final CatalogView catalog = catalogService.findCatalog(null, "%");

        assertThat(catalog.getSearch()).isEqualTo("%");
        assertThat(catalog.getProducts()).isEmpty();
    }

    @Test
    void treatsUnderscoreAsLiteralSearchText() {
        final CatalogView catalog = catalogService.findCatalog(null, "_");

        assertThat(catalog.getSearch()).isEqualTo("_");
        assertThat(catalog.getProducts()).isEmpty();
    }

    @Test
    void treatsBackslashAsLiteralSearchText() {
        final CatalogView catalog = catalogService.findCatalog(null, "\\");

        assertThat(catalog.getSearch()).isEqualTo("\\");
        assertThat(catalog.getProducts()).isEmpty();
    }

    @Test
    void searchesByNameOrDescriptionCaseInsensitivelyAfterTrimming() {
        final CatalogView catalog = catalogService.findCatalog(null, "  TOMATOES  ");

        assertThat(catalog.getSearch()).isEqualTo("TOMATOES");
        assertThat(catalog.getProducts())
            .extracting(CatalogProduct::name)
            .containsExactly("Cherry tomatoes", "Chopped tomatoes");
    }

    @Test
    void combinesCategoryAndSearchFilters() {
        final CatalogView catalog = catalogService.findCatalog(6L, "tomatoes");

        assertThat(catalog.getProducts())
            .extracting(CatalogProduct::name)
            .containsExactly("Chopped tomatoes");
    }
}
