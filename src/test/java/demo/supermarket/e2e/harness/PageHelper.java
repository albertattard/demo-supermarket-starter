package demo.supermarket.e2e.harness;

import module java.base;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.options.AriaRole;

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

    void shouldNotShowHeading(final String heading) {
        assertThat(page.getByRole(AriaRole.HEADING,
            new Page.GetByRoleOptions().setName(heading))).not().isVisible();
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

    void selectOptionByLabel(final String label, final String optionLabel) {
        page.getByLabel(label).selectOption(new String[]{optionLabel});
    }

    void shouldHaveInputValue(final String label, final String value) {
        assertThat(page.getByLabel(label)).hasValue(value);
    }

    void shouldShowVisible(final Locator locator) {
        assertThat(locator).isVisible();
    }

    String currentPath() {
        return URI.create(page.url()).getPath();
    }

    void shouldHavePathMatching(final Pattern pattern) {
        org.assertj.core.api.Assertions.assertThat(currentPath()).matches(pattern);
    }
}
