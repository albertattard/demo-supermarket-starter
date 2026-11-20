package demo.supermarket.e2e.harness;

import module java.base;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.options.AriaRole;

public final class HomePage {

    private static final Pattern CART_PATH = Pattern.compile("/cart/[A-Za-z0-9_-]{32,}");
    private static final Pattern CART_CATALOG_PATH = Pattern.compile("/cart/[A-Za-z0-9_-]{32,}/products");

    private final Page page;
    private final PageHelper helper;

    HomePage(final Page page, final String baseUrl) {
        this.page = page;
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

    public HomePage addProductToCart(final String product) {
        page.locator(".product-card")
            .filter(new Locator.FilterOptions().setHasText(product))
            .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Add to cart"))
            .click();
        return this;
    }

    public HomePage shouldBeOnOpaqueCartUrl() {
        helper.shouldHavePathMatching(CART_PATH);
        return this;
    }

    public HomePage shouldBeOnCartScopedCatalogUrl() {
        helper.shouldHavePathMatching(CART_CATALOG_PATH);
        return this;
    }

    public HomePage increaseProductQuantity(final String product) {
        productCard(product)
            .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Increase " + product))
            .click();
        return this;
    }

    public HomePage decreaseProductQuantity(final String product) {
        productCard(product)
            .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Decrease " + product))
            .click();
        return this;
    }

    public HomePage removeProductFromCatalog(final String product) {
        productCard(product)
            .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Remove " + product))
            .click();
        return this;
    }

    public String currentPath() {
        return helper.currentPath();
    }

    public HomePage openPath(final String path) {
        helper.navigateTo(path);
        return this;
    }

    public HomePage updateQuantity(final String product, final String quantity) {
        final Locator line = cartLine(product);
        line.getByLabel("Quantity").fill(quantity);
        line.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Update")).click();
        return this;
    }

    public HomePage removeLine(final String product) {
        cartLine(product)
            .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Remove"))
            .click();
        return this;
    }

    public HomePage shouldShowCartLine(final String product) {
        helper.shouldShowHeading(product);
        return this;
    }

    public HomePage shouldNotShowCartLine(final String product) {
        assertThat(cartLine(product)).not().isVisible();
        return this;
    }

    public HomePage shouldShowQuantity(final String product, final String quantity) {
        assertThat(cartLine(product).getByLabel("Quantity")).hasValue(quantity);
        return this;
    }

    public HomePage shouldShowCatalogQuantity(final String product, final String quantity) {
        final Locator quantityInput = productCard(product).getByLabel("Quantity");
        assertThat(quantityInput).hasValue(quantity);
        assertThat(quantityInput).hasAttribute("readonly", "");
        return this;
    }

    public HomePage shouldShowAddToCart(final String product) {
        assertThat(productCard(product).getByRole(AriaRole.BUTTON,
            new Locator.GetByRoleOptions().setName("Add to cart"))).isVisible();
        return this;
    }

    public HomePage openCurrentCart() {
        page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("View cart")).click();
        return this;
    }

    public HomePage shouldShowCartSubtotal(final String subtotal) {
        assertThat(page.locator(".cart-total")).containsText(subtotal);
        return this;
    }

    public HomePage shouldShowEmptyCart() {
        helper.shouldShowHeading("Your cart is empty");
        return this;
    }

    public HomePage shouldShowCartNotFound() {
        helper.shouldShowHeading("Cart not found");
        helper.shouldShowText("Back to products");
        return this;
    }

    private Locator productCard(final String product) {
        return page.locator(".product-card")
            .filter(new Locator.FilterOptions().setHasText(product));
    }

    private Locator cartLine(final String product) {
        return page.locator(".cart-line")
            .filter(new Locator.FilterOptions().setHasText(product));
    }
}
