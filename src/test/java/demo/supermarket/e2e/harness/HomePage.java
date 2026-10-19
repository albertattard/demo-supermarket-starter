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

    public HomePage shouldShowCatalogIsNotImplementedYet() {
        helper.shouldShowText("Catalog functionality is not implemented yet.");
        return this;
    }
}
