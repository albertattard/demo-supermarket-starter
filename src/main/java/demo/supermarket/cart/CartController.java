package demo.supermarket.cart;

import demo.supermarket.catalog.CatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

@Controller
class CartController {

    private final CartService cartService;
    private final CatalogService catalogService;

    CartController(final CartService cartService, final CatalogService catalogService) {
        this.cartService = cartService;
        this.catalogService = catalogService;
    }

    @PostMapping("/cart/start")
    View startCart(
        @RequestParam(name = "productSlug", required = false) final String productSlug,
        @RequestParam(name = "returnTo", required = false) final String returnTo) {
        final CartView cart = cartService.startCart(productSlug);
        if (ReturnTarget.from(returnTo).isCatalog()) {
            return redirectToCartCatalog(cart.token());
        }
        return redirectToCart(cart.token());
    }

    @GetMapping("/cart/{cartToken}")
    String showCart(
        @PathVariable("cartToken") final String cartToken,
        @RequestHeader(name = "HX-Request", required = false) final String hxRequest,
        final Model model) {
        model.addAttribute("cart", cartService.getActiveCart(cartToken));
        return cartView(hxRequest);
    }

    @PostMapping("/cart/{cartToken}/items")
    Object addItem(
        @PathVariable("cartToken") final String cartToken,
        @RequestParam("productSlug") final String productSlug,
        @RequestParam(name = "returnTo", required = false) final String returnTo,
        @RequestHeader(name = "HX-Request", required = false) final String hxRequest,
        final Model model) {
        return quantityMutationResponse(
            () -> cartService.addProduct(cartToken, productSlug),
            cartToken,
            productSlug,
            ReturnTarget.from(returnTo),
            hxRequest,
            model);
    }

    @PostMapping("/cart/{cartToken}/items/{productSlug}")
    Object updateItem(
        @PathVariable("cartToken") final String cartToken,
        @PathVariable("productSlug") final String productSlug,
        @RequestParam("quantity") final String quantity,
        @RequestParam(name = "returnTo", required = false) final String returnTo,
        @RequestHeader(name = "HX-Request", required = false) final String hxRequest,
        final Model model) {
        return quantityMutationResponse(
            () -> cartService.updateQuantity(cartToken, productSlug, quantity),
            cartToken,
            productSlug,
            ReturnTarget.from(returnTo),
            hxRequest,
            model);
    }

    @PostMapping("/cart/{cartToken}/items/{productSlug}/remove")
    Object removeItem(
        @PathVariable("cartToken") final String cartToken,
        @PathVariable("productSlug") final String productSlug,
        @RequestParam(name = "returnTo", required = false) final String returnTo,
        @RequestHeader(name = "HX-Request", required = false) final String hxRequest,
        final Model model) {
        final CartView cart = cartService.removeProduct(cartToken, productSlug);
        return mutationResponse(cart, cartToken, productSlug, ReturnTarget.from(returnTo), hxRequest, model);
    }

    private static RedirectView redirectToCart(final String cartToken) {
        final RedirectView redirectView = new RedirectView("/cart/" + cartToken);
        redirectView.setStatusCode(HttpStatus.SEE_OTHER);
        return redirectView;
    }

    private static RedirectView redirectToCartCatalog(final String cartToken) {
        final RedirectView redirectView = new RedirectView("/cart/" + cartToken + "/products");
        redirectView.setStatusCode(HttpStatus.SEE_OTHER);
        return redirectView;
    }

    private static String cartView(final String hxRequest) {
        if (isHtmx(hxRequest)) {
            return "cart :: cartContent";
        }
        return "cart";
    }

    private static String cartFragment(final CartView cart, final Model model) {
        model.addAttribute("cart", cart);
        return "cart :: cartContent";
    }

    private Object mutationResponse(
        final CartView cart,
        final String cartToken,
        final String productSlug,
        final ReturnTarget returnTarget,
        final String hxRequest,
        final Model model) {
        if (returnTarget.isCatalog()) {
            if (isHtmx(hxRequest)) {
                return catalogCartMutation(cart, productSlug, model);
            }
            return redirectToCartCatalog(cartToken);
        }
        if (isHtmx(hxRequest)) {
            return cartFragment(cart, model);
        }
        return redirectToCart(cartToken);
    }

    private Object quantityMutationResponse(
        final QuantityMutation mutation,
        final String cartToken,
        final String productSlug,
        final ReturnTarget returnTarget,
        final String hxRequest,
        final Model model) {
        try {
            return mutationResponse(mutation.apply(), cartToken, productSlug, returnTarget, hxRequest, model);
        } catch (final InvalidQuantityException ex) {
            return invalidQuantityResponse(cartToken, productSlug, returnTarget, hxRequest, ex.getMessage(), model);
        }
    }

    private String catalogCartMutation(final CartView cart, final String productSlug, final Model model) {
        model.addAttribute("cart", cart);
        model.addAttribute("product", catalogService.findActiveProduct(productSlug));
        return "catalog-cart-mutation";
    }

    private ModelAndView invalidQuantityResponse(
        final String cartToken,
        final String productSlug,
        final ReturnTarget returnTarget,
        final String hxRequest,
        final String message,
        final Model model) {
        final CartView cart = cartService.getActiveCart(cartToken);
        model.addAttribute("cart", cart);
        if (returnTarget.isCatalog()) {
            model.addAttribute("product", catalogService.findActiveProduct(productSlug));
            model.addAttribute("catalogCartError", message);
            if (isHtmx(hxRequest)) {
                return new ModelAndView("catalog-cart-mutation", HttpStatus.BAD_REQUEST);
            }
            model.addAttribute("catalog", catalogService.findCatalog(null, null));
            return new ModelAndView("catalog", HttpStatus.BAD_REQUEST);
        }
        model.addAttribute("cartError", message);
        return new ModelAndView(cartView(hxRequest), HttpStatus.BAD_REQUEST);
    }

    private static boolean isHtmx(final String hxRequest) {
        return "true".equalsIgnoreCase(hxRequest);
    }

    @FunctionalInterface
    private interface QuantityMutation {
        CartView apply();
    }

    private enum ReturnTarget {
        CART,
        CATALOG;

        static ReturnTarget from(final String value) {
            if ("catalog".equals(value)) {
                return CATALOG;
            }
            return CART;
        }

        boolean isCatalog() {
            return this == CATALOG;
        }
    }
}
