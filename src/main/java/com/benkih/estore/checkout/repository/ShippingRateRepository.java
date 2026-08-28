package com.benkih.estore.checkout.repository;

//package com.benkih.estore.shipping.repository;


import com.benkih.estore.checkout.entity.ShippingRate;
import com.benkih.estore.common.enums.DeliveryMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface ShippingRateRepository
    extends JpaRepository<ShippingRate, Long> {

  @Query("""
        SELECT r
        FROM ShippingRate r
        WHERE r.zone = :zone
          AND r.deliveryMethod = :deliveryMethod
          AND r.active = true
          AND r.minWeightKg <= :weight
          AND (
              r.maxWeightKg IS NULL
              OR r.maxWeightKg >= :weight
          )
        ORDER BY r.minWeightKg DESC
        """)
  Optional<ShippingRate> findApplicableRate(
      @Param("zone") String zone,
      @Param("deliveryMethod") DeliveryMethod deliveryMethod,
      @Param("weight") BigDecimal weight
  );
}
