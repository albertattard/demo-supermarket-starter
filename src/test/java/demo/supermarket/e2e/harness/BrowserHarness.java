package demo.supermarket.e2e.harness;

import java.util.Map;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

final class BrowserHarness implements AutoCloseable {

    private final Playwright playwright;
    private final Browser browser;

    private BrowserHarness(final Playwright playwright, final Browser browser) {
        this.playwright = playwright;
        this.browser = browser;
    }

    static BrowserHarness start() {
        final Playwright playwright = Playwright.create(new Playwright.CreateOptions()
                .setEnv(Map.of("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1")));
        final LaunchOptions options = new LaunchOptions()
                .setHeadless(Configuration.configuredBoolean("e2e.headless", "E2E_HEADLESS", true));
        Configuration.configured("e2e.browser.channel", "E2E_BROWSER_CHANNEL")
                .ifPresent(options::setChannel);

        final Browser browser = playwright.chromium().launch(options);
        return new BrowserHarness(playwright, browser);
    }

    void scenario(final PageScenario scenario) {
        try (BrowserContext context = browser.newContext(); Page page = context.newPage()) {
            scenario.run(page);
        }
    }

    @Override
    public void close() {
        Closeables.closeAll(browser, playwright);
    }

    @FunctionalInterface
    interface PageScenario {

        void run(Page page);
    }
}
