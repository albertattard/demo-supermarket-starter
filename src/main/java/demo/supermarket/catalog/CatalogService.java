package demo.supermarket.catalog;

import module java.base;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    CatalogService(final CategoryRepository categoryRepository,
                   final ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public CatalogView findCatalog(final Long categoryId, final String search) {
        final String displaySearch = normalizeDisplaySearch(search);
        final String searchPattern = searchPattern(displaySearch);

        final List<CatalogCategory> categories = categoryRepository.findByActiveTrueOrderByNameAsc()
            .stream()
            .map(CatalogService::mapCatalogCategory)
            .toList();

        final List<CatalogProduct> products = productRepository.findActiveCatalogProducts(categoryId, searchPattern)
            .stream()
            .map(CatalogService::mapCatalogProduct)
            .toList();

        return new CatalogView(categories, products, categoryId, displaySearch == null ? "" : displaySearch);
    }

    @Transactional(readOnly = true)
    public CatalogProduct findActiveProduct(final String productSlug) {
        return productRepository.findBySlugAndActiveTrue(productSlug)
            .map(CatalogService::mapCatalogProduct)
            .orElseThrow();
    }

    private static String normalizeDisplaySearch(final String search) {
        if (search == null) {
            return null;
        }

        final String trimmed = search.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        return trimmed;
    }

    private static String searchPattern(final String displaySearch) {
        if (displaySearch == null) {
            return null;
        }

        return "%" + displaySearch.toLowerCase(Locale.ROOT).replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_") + "%";
    }

    private static CatalogCategory mapCatalogCategory(final Category category) {
        return new CatalogCategory(
            category.getId(),
            category.getName());
    }

    private static CatalogProduct mapCatalogProduct(final Product product) {
        return new CatalogProduct(
            product.getSlug(),
            product.getName(),
            product.getDescription(),
            product.getCategory().getName(),
            product.getUnitLabel(),
            product.getUnitPrice(),
            product.getImagePath());
    }
}
