package demo.supermarket.e2e.harness;

import java.util.function.Consumer;

public class E2eHarness implements AutoCloseable {

    private final ApplicationHarness application;
    private final BrowserHarness browser;

    public static E2eHarness start() {
        ApplicationHarness application = null;
        BrowserHarness browser = null;

        try {
            application = ApplicationHarness.start();
            browser = BrowserHarness.start();

            return new E2eHarness(application, browser);
        } catch (final RuntimeException e) {
            Closeables.closeAllSuppressing(e, browser, application);
            throw e;
        }
    }

    private E2eHarness(final ApplicationHarness application, final BrowserHarness browser) {
        this.application = application;
        this.browser = browser;
    }

    @Override
    public void close() {
        Closeables.closeAll(browser, application);
    }

    public void homePage(final Consumer<HomePage> scenario) {
        browser.scenario(page -> scenario.accept(new HomePage(page, application.baseUrl())));
    }
}
