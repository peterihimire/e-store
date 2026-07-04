package com.benkih.estore.cart.entity;

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
public class Cart {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, updatable = false)
  private String slug;

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

  //  public void addItem(CartItem item) {
  //    if (!items.contains(item)) {
  //      items.add(item);
  //    }
  //    item.setCart(this);
  //    updateTotalAmount();
  //  }

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

  //  // watch here
  //  public Collection<Object> getItems() {
  //  }

  private String createdBy;
  private String updatedBy;

  @Column(nullable = false,updatable = false)
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @PrePersist
  public void onCreate() {
    // this.slug = UUID.randomUUID().toString();

    if (this.slug == null) {
      this.slug = UUID.randomUUID().toString();
    }
    if (this.createdAt == null) {
      this.createdAt = LocalDateTime.now();
    }
  }

  @PreUpdate
  public void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

}
