package com.benkih.estore.cart.entity;

import com.benkih.estore.common.entity.BaseEntity;
import com.benkih.estore.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "carts")
public class Cart extends BaseEntity {

  private BigDecimal totalAmount = BigDecimal.ZERO;

  @OneToMany(
      mappedBy = "cart",
      cascade = CascadeType.ALL,
      orphanRemoval = true
  )
  private List<CartItem> items = new ArrayList<>();
  //  private Set<CartItem> items = new HashSet<>();

  @OneToOne
  @JoinColumn(name = "user_id")
  private User user;

  public void addItem(CartItem item){
    this.items.add(item);
    item.setCart(this);
    updateTotalAmount();
  }

  public void removeItem(CartItem item){
    this.items.remove(item);
    item.setCart(null);
    updateTotalAmount();
  }

  public void updateTotalAmount(){
    this.totalAmount = items.stream().map(item -> {
      BigDecimal unitPrice = item.getUnitPrice();
      if(unitPrice == null){
        return BigDecimal.ZERO;
      }
      return unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
    }).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

}
