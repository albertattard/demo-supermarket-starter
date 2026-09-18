package demo.supermarket.catalog;

import demo.supermarket.cart.CartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
class CatalogController {

    private final CatalogService catalogService;
    private final CartService cartService;

    CatalogController(final CatalogService catalogService, final CartService cartService) {
        this.catalogService = catalogService;
        this.cartService = cartService;
    }

    @GetMapping({"/", "/products"})
    String catalog(@RequestParam(name = "category", required = false) final Long categoryId,
                   @RequestParam(name = "q", required = false) final String search,
                   final Model model) {
        model.addAttribute("catalog", catalogService.findCatalog(categoryId, search));
        return "catalog";
    }

    @GetMapping("/cart/{cartToken}/products")
    String cartCatalog(@PathVariable("cartToken") final String cartToken,
                       @RequestParam(name = "category", required = false) final Long categoryId,
                       @RequestParam(name = "q", required = false) final String search,
                       final Model model) {
        model.addAttribute("catalog", catalogService.findCatalog(categoryId, search));
        model.addAttribute("cart", cartService.getActiveCart(cartToken));
        return "catalog";
    }
}
