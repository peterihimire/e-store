package com.benkih.estore.product.repository;


import com.benkih.estore.product.entity.AttributeValue;
import com.benkih.estore.product.entity.CategoryAttribute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {

  Optional<AttributeValue> findBySlug(String slug);

  Optional<AttributeValue> findByAttributeAndValue(
      CategoryAttribute attribute,
      String value
  );

  Optional<AttributeValue> findByAttributeAndSlug(
      CategoryAttribute attribute,
      String slug
  );

  List<AttributeValue> findByAttribute(
      CategoryAttribute attribute
  );

  List<AttributeValue> findByAttributeId(
      Long attributeId
  );

  boolean existsByAttributeAndValue(
      CategoryAttribute attribute,
      String value
  );

  boolean existsByAttributeIdAndValue(
      Long attributeId,
      String value
  );
}
