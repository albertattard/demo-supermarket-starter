package demo.supermarket.catalog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CatalogServiceTest {

    @Autowired
    private CatalogService catalogService;

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   ", "\t\r\n", "\u001f" })
    void absentOrTrimmedEmptySearchLeavesOnlyCategoryFilter(final String search) {
        final CatalogView unfiltered = catalogService.findCatalog(null, null);
        final CatalogView categoryOnly = catalogService.findCatalog(6L, null);
        final CatalogView catalog = catalogService.findCatalog(6L, search);

        assertThat(catalog.getSearch()).isEmpty();
        assertThat(catalog.getSelectedCategoryId()).isEqualTo(6L);
        assertThat(catalog.getProducts()).isNotEmpty().isEqualTo(categoryOnly.getProducts());
        assertThat(catalog.getProducts().size()).isLessThan(unfiltered.getProducts().size());
    }

    @Test
    void preservesWhitespaceOutsideStringTrimRangeAsSearchText() {
        final CatalogView catalog = catalogService.findCatalog(null, " \u2003 ");

        assertThat(catalog.getSearch()).isEqualTo("\u2003");
        assertThat(catalog.getProducts()).isEmpty();
    }

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
