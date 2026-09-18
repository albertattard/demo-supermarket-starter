package demo.supermarket.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PublicRoutesTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void exposesHomePageWithoutAuthentication() throws Exception {
        mvc.perform(get("/"))
            .andExpect(status().isOk());
    }

    @Test
    void exposesProductsPageWithoutAuthentication() throws Exception {
        mvc.perform(get("/products"))
            .andExpect(status().isOk());
    }

    @Test
    void exposesHealthWithoutAuthentication() throws Exception {
        mvc.perform(get("/actuator/health"))
            .andExpect(status().isOk());
    }

    @Test
    void exposesCssAndHtmxWebJarAssetsWithoutAuthentication() throws Exception {
        mvc.perform(get("/css/application.css"))
            .andExpect(status().isOk());

        mvc.perform(get("/webjars/htmx.org/dist/htmx.min.js"))
            .andExpect(status().isOk());
    }

    @Test
    void requiresAuthenticationForOtherRoutes() throws Exception {
        mvc.perform(get("/private"))
            .andExpect(status().isUnauthorized());
    }
}
