package demo.supermarket.cart;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(basePackages = { "demo.supermarket.cart", "demo.supermarket.catalog" })
class CartExceptionHandler {

    @ExceptionHandler(CartNotFoundException.class)
    ModelAndView cartNotFound(final Model model) {
        model.addAttribute("notFoundTitle", "Cart not found");
        model.addAttribute("notFoundMessage", "This cart link is no longer available.");
        return new ModelAndView("cart-not-found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    ModelAndView productNotFound(final Model model) {
        model.addAttribute("notFoundTitle", "Product not found");
        model.addAttribute("notFoundMessage", "That product is not available in the catalog.");
        return new ModelAndView("cart-not-found", HttpStatus.NOT_FOUND);
    }
}
