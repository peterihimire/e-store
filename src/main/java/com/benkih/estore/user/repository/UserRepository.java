package com.benkih.estore.user.repository;

import com.benkih.estore.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Product, Long> {
}
