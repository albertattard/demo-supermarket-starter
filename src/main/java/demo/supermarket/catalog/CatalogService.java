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
        final Optional<String> displaySearch = normalizeDisplaySearch(search);
        final String searchPattern = displaySearch.map(CatalogService::searchPattern).orElse(null);

        final List<CatalogCategory> categories = categoryRepository.findByActiveTrueOrderByNameAsc()
            .stream()
            .map(CatalogService::mapCatalogCategory)
            .toList();

        final List<CatalogProduct> products = productRepository.findActiveCatalogProducts(categoryId, searchPattern)
            .stream()
            .map(CatalogService::mapCatalogProduct)
            .toList();

        return new CatalogView(categories, products, categoryId, displaySearch.orElse(""));
    }

    @Transactional(readOnly = true)
    public CatalogProduct findActiveProduct(final String productSlug) {
        return productRepository.findBySlugAndActiveTrue(productSlug)
            .map(CatalogService::mapCatalogProduct)
            .orElseThrow();
    }

    private static Optional<String> normalizeDisplaySearch(final String search) {
        return Optional.ofNullable(search)
            .map(String::trim)
            .filter(Predicate.not(String::isEmpty));
    }

    private static String searchPattern(final String displaySearch) {
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
