package demo.supermarket.catalog;

import module java.base;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlugAndActiveTrue(String slug);

    @Query("""
        select product
        from Product product
        join fetch product.category category
        where product.active = true
          and category.active = true
          and (:categoryId is null or category.id = :categoryId)
          and (:searchPattern is null
                or lower(product.name) like :searchPattern escape '\\'
                or lower(product.description) like :searchPattern escape '\\')
        order by category.name asc, product.name asc""")
    List<Product> findActiveCatalogProducts(
        @Param("categoryId") Long categoryId,
        @Param("searchPattern") String searchPattern);
}
