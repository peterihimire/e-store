package com.benkih.estore.cart.repository;

import com.benkih.estore.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

  void deleteAllByCartSlug(String slug);
}
