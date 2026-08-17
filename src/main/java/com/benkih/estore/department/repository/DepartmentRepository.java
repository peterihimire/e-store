package com.benkih.estore.department.repository;

import com.benkih.estore.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
  Optional<Department> findBySlug(String slug);

  Optional<Department> findByNameIgnoreCase(String name);

  boolean existsByNameIgnoreCaseAndBusinessId(String name, Long businessId);

  boolean existsBySlug(String slug);

  List<Department> findAllBySlugInAndBusinessId(Collection<String> slugs,
                                                Long businessId);
}
