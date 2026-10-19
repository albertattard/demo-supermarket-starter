package demo.supermarket.e2e.harness;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

final class PageHelper {

    private final Page page;
    private final String baseUrl;

    PageHelper(final Page page, final String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    void navigateTo(final String path) {
        page.navigate(baseUrl + path);
    }

    void shouldShowText(final String text) {
        assertThat(page.getByText(text)).isVisible();
    }

    void shouldShowHeading(final String heading) {
        assertThat(page.getByRole(AriaRole.HEADING,
            new Page.GetByRoleOptions().setName(heading))).isVisible();
    }

    void clickLink(final String name) {
        page.getByRole(AriaRole.LINK,
            new Page.GetByRoleOptions().setName(name)).click();
    }

    void clickButton(final String name) {
        page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName(name)).click();
    }

    void fillInput(final String label, final String value) {
        page.getByLabel(label).fill(value);
    }

    void shouldShowVisible(final Locator locator) {
        assertThat(locator).isVisible();
    }
}
