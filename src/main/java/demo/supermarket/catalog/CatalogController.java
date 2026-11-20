package demo.supermarket.catalog;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
class CatalogController {

    private final CatalogService catalogService;

    CatalogController(final CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping({"/", "/products"})
    String catalog(@RequestParam(name = "category", required = false) final Long categoryId,
                   @RequestParam(name = "q", required = false) final String search,
                   final Model model) {
        model.addAttribute("catalog", catalogService.findCatalog(categoryId, search));
        return "catalog";
    }
}
