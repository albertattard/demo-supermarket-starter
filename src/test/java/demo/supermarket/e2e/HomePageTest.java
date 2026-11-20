package demo.supermarket.e2e;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import demo.supermarket.e2e.harness.E2eHarness;

@Tag("e2e")
class HomePageTest {

    private static E2eHarness harness;

    @BeforeAll
    static void startHarnesses() {
        harness = E2eHarness.start();
    }

    @AfterAll
    static void stopHarnesses() {
        if (harness != null) {
            harness.close();
        }
    }

    @Test
    void opensTheInitialHomePage() {
        harness.homePage(homePage -> homePage
            .openCatalog()
            .shouldShowApplicationName()
            .shouldShowSeededCatalogProduct());
    }

    @Test
    void filtersCatalogBySearchAndCategory() {
        harness.homePage(homePage -> homePage
            .openCatalog()
            .search("tomatoes")
            .shouldPreserveSearch("tomatoes")
            .shouldShowProduct("Cherry tomatoes")
            .shouldShowProduct("Chopped tomatoes")
            .shouldNotShowProduct("Baby Spinach")
            .selectCategory("Pantry")
            .search("tomatoes")
            .shouldPreserveSearch("tomatoes")
            .shouldShowProduct("Chopped tomatoes")
            .shouldNotShowProduct("Cherry tomatoes"));
    }
}
