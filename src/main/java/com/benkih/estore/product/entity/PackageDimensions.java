package com.benkih.estore.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
public class PackageDimensions {
  @PositiveOrZero
  @Column(
      precision = 10,
      scale = 3
  )
  private BigDecimal weightKg = BigDecimal.ZERO;

  @PositiveOrZero
  @Column(
      precision = 10,
      scale = 2
  )
  private BigDecimal lengthCm;

  @PositiveOrZero
  @Column(
      precision = 10,
      scale = 2
  )
  private BigDecimal widthCm;

  @PositiveOrZero
  @Column(
      precision = 10,
      scale = 2
  )
  private BigDecimal heightCm;
}
