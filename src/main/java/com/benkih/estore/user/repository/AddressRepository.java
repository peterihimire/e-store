package com.benkih.estore.user.repository;

import com.benkih.estore.user.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface AddressRepository extends JpaRepository<Address, Long> {
  Optional<Address> findBySlug(String slug);

  List<Address> findByUserSlug(String userSlug);

  List<Address> findAllByUserSlugOrderByCreatedAtDesc(String userSlug);

  Optional<Address> findBySlugAndUserSlug(
      String slug,
      String userSlug
  );

  Optional<Address> findByUserSlugAndDefaultAddressTrue(
      String userSlug
  );

  @Modifying
  @Transactional
  @Query("""
    update Address a
    set a.defaultAddress = false
    where a.user.slug = :userSlug
""")
  void clearDefaultAddress(String userSlug);
}
