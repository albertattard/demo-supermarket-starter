package demo.supermarket.e2e;

import demo.supermarket.e2e.harness.E2eHarness;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

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

    @Test
    void managesPersistedGuestCartFromCatalog() {
        harness.homePage(homePage -> {
            homePage.openCatalog()
                .addProductToCart("Sourdough Country Loaf")
                .shouldBeOnCartScopedCatalogUrl()
                .shouldShowApplicationName()
                .shouldShowCatalogQuantity("Sourdough Country Loaf", "1")
                .increaseProductQuantity("Sourdough Country Loaf")
                .shouldShowCatalogQuantity("Sourdough Country Loaf", "2")
                .decreaseProductQuantity("Sourdough Country Loaf")
                .shouldShowCatalogQuantity("Sourdough Country Loaf", "1")
                .addProductToCart("Burger buns")
                .shouldBeOnCartScopedCatalogUrl()
                .shouldShowCatalogQuantity("Burger buns", "1")
                .openCurrentCart()
                .shouldBeOnOpaqueCartUrl()
                .shouldShowCartLine("Burger buns")
                .shouldShowQuantity("Burger buns", "1")
                .shouldShowCartLine("Sourdough Country Loaf")
                .shouldShowQuantity("Sourdough Country Loaf", "1")
                .shouldShowCartSubtotal("4,74");

            final String cartPath = homePage.currentPath();

            homePage.openPath(cartPath)
                .shouldShowCartLine("Sourdough Country Loaf")
                .updateQuantity("Sourdough Country Loaf", "3")
                .shouldShowQuantity("Sourdough Country Loaf", "3")
                .shouldShowQuantity("Burger buns", "1")
                .shouldShowCartSubtotal("10,64")
                .removeLine("Burger buns")
                .shouldShowCartLine("Sourdough Country Loaf")
                .shouldNotShowCartLine("Burger buns")
                .shouldShowCartSubtotal("8,85")
                .removeLine("Sourdough Country Loaf")
                .shouldShowEmptyCart()
                .openPath("/cart/not-a-real-token")
                .shouldShowCartNotFound();
        });
    }
}
