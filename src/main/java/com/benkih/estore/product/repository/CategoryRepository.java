//package com.benkih.estore.product.repository;
//
//import com.benkih.estore.product.entity.Category;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.Optional;
//
//public interface CategoryRepository extends JpaRepository<Category, Long> {
//
//  Category findByNameAndBusinessId(String name, Long businessId);
//
//  boolean existsByName(String name);
//
//  Optional<Category> findBySlug(String slug);
//
//  Optional<Category> findByBusinessSlugAndSlug(String businessSlug,
//                                               String slug);
//
//  boolean existsByBusinessSlugAndSlug(
//      String businessSlug,
//      String slug
//  );
//}
package com.benkih.estore.product.repository;

import com.benkih.estore.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  Optional<Category> findBySlug(String slug);

  Optional<Category> findByName(String name);

  Optional<Category> findByNameAndParent(
      String name,
      Category parent
  );

  Optional<Category> findByNameAndParentIsNull(
      String name
  );

  List<Category> findByParent(Category parent);

  List<Category> findByParentIsNull();

  boolean existsByNameAndParent(
      String name,
      Category parent
  );

  boolean existsByNameAndParentIsNull(
      String name
  );

  boolean existsByParentId(Long parentId);
}