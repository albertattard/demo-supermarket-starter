package demo.supermarket.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void rendersCatalogAtHomeAndProductsRoutes() throws Exception {
        mvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Demo Supermarket")))
            .andExpect(content().string(containsString("Sourdough Country Loaf")));

        mvc.perform(get("/products"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Demo Supermarket")))
            .andExpect(content().string(containsString("Sourdough Country Loaf")));
    }

    @Test
    void onlyShowsActiveProductsInActiveCategories() throws Exception {
        mvc.perform(get("/products"))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("Discontinued Breakfast Cereal"))))
            .andExpect(content().string(not(containsString("Mulled Apple Punch"))))
            .andExpect(content().string(not(containsString("Seasonal"))));
    }

    @Test
    void filtersByCategory() throws Exception {
        mvc.perform(get("/products").param("category", "2"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Eggs")))
            .andExpect(content().string(not(containsString("Cod fillets"))));
    }

    @Test
    void searchesByNameOrDescriptionCaseInsensitivelyAfterTrimming() throws Exception {
        mvc.perform(get("/products").param("q", "  TOMATOES  "))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Cherry tomatoes")))
            .andExpect(content().string(containsString("Chopped tomatoes")))
            .andExpect(content().string(containsString("value=\"TOMATOES\"")))
            .andExpect(content().string(not(containsString("Baby Spinach"))));
    }

    @Test
    void combinesCategoryAndSearchFilters() throws Exception {
        mvc.perform(get("/products")
                .param("category", "6")
                .param("q", "tomatoes"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Chopped tomatoes")))
            .andExpect(content().string(not(containsString("Cherry tomatoes"))));
    }

    @Test
    void sortsCategoriesByNameAndProductsByCategoryThenName() throws Exception {
        final MvcResult result = mvc.perform(get("/products"))
            .andExpect(status().isOk())
            .andReturn();

        final String html = result.getResponse().getContentAsString();
        assertThat(html.indexOf(">Chipotle paste<")).isLessThan(html.indexOf(">Chopped tomatoes<"));
        assertThat(html.indexOf(">Frozen<")).isLessThan(html.indexOf(">Fruit and Vegetables<"));
    }

    @Test
    void rendersEmptyStateWhenNoProductsMatch() throws Exception {
        mvc.perform(get("/products").param("q", "zzzz"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("No products found")))
            .andExpect(content().string(containsString("Try a different search term or category.")));
    }

    @Test
    void rendersEuroPricesAndPlaceholderImages() throws Exception {
        mvc.perform(get("/products").param("q", "sourdough"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("2,95")))
            .andExpect(content().string(containsString("€")))
            .andExpect(content().string(containsString("/images/products/sourdough-country-loaf-500g.png")));
    }

    @Test
    void catalogAddToCartStartsCartThroughOnlyStartRoute() throws Exception {
        mvc.perform(get("/products").param("q", "sourdough"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("action=\"/cart/start\"")))
            .andExpect(content().string(containsString("name=\"productSlug\" value=\"sourdough-country-loaf-500g\"")))
            .andExpect(content().string(containsString("name=\"returnTo\" value=\"catalog\"")))
            .andExpect(content().string(not(containsString("/cart/catalog/"))))
            .andExpect(content().string(not(containsString("guest_cart_token"))));
    }

    @Test
    void unknownCartScopedCatalogTokenReturnsCustomerFacingNotFound() throws Exception {
        mvc.perform(get("/cart/not-a-real-token/products"))
            .andExpect(status().isNotFound())
            .andExpect(content().string(containsString("Cart not found")))
            .andExpect(content().string(containsString("Back to products")));
    }
}
