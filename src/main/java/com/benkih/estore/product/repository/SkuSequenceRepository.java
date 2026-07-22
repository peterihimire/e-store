package com.benkih.estore.product.repository;


import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SkuSequenceRepository {

  private final JdbcTemplate jdbcTemplate;

  public long nextSkuNumber() {

    return jdbcTemplate.queryForObject(
        "SELECT nextval('sku_sequence')",
        Long.class
    );
  }
}