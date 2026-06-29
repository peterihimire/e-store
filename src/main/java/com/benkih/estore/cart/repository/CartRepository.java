package com.benkih.estore.cart.repository;

import com.benkih.estore.cart.entity.Cart;
import com.benkih.estore.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long>{

  @EntityGraph(attributePaths = {
      "user",
      "items",
      "items.product",
//      "items.product.images",
      "items.product.category"
  })
  Optional<Cart> findBySlug(String slug);

//  void deleteBySlug(String slug);

//  void deleteByCart(Cart cart);

  @EntityGraph(attributePaths = {
      "user",
      "items",
      "items.product",
//      "items.product.images",
      "items.product.category"
  })
  Optional<Cart> findByUserSlug(String slug); // Optional here means I can handle the exception and avoids returning null and makes API safer

  Cart findByUser(User user);
}
