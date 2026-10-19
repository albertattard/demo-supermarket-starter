package demo.supermarket.e2e.harness;

import com.microsoft.playwright.Page;

public final class HomePage {

    private final PageHelper helper;

    HomePage(final Page page, final String baseUrl) {
        this.helper = new PageHelper(page, baseUrl);
    }

    public HomePage openCatalog() {
        helper.navigateTo("/");
        return this;
    }

    public HomePage shouldShowApplicationName() {
        helper.shouldShowHeading("Demo Supermarket");
        return this;
    }

    public HomePage shouldShowSeededCatalogProduct() {
        helper.shouldShowHeading("Burger buns");
        helper.shouldShowText("Burger buns in a convenient 300 g pack.");
        return this;
    }

    public HomePage search(final String search) {
        helper.fillInput("Search", search);
        helper.clickButton("Apply filters");
        return this;
    }

    public HomePage selectCategory(final String categoryName) {
        helper.selectOptionByLabel("Category", categoryName);
        return this;
    }

    public HomePage shouldPreserveSearch(final String search) {
        helper.shouldHaveInputValue("Search", search);
        return this;
    }

    public HomePage shouldShowProduct(final String product) {
        helper.shouldShowHeading(product);
        return this;
    }

    public HomePage shouldNotShowProduct(final String product) {
        helper.shouldNotShowHeading(product);
        return this;
    }
}
