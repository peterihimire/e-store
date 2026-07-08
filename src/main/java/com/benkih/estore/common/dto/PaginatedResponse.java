package com.benkih.estore.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {

  private List<T> items;
  private long totalItems;
  private int totalPages;
  private int currentPage;  // ✅ One-based (user-friendly)
  private int pageSize;
  private boolean isFirstPage;
  private boolean isLastPage;
  private boolean hasNext;
  private boolean hasPrevious;

  public static <T> PaginatedResponse<T> from(Page<T> page, int requestedPage) {
    return PaginatedResponse.<T>builder()
        .items(page.getContent())
        .totalItems(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .currentPage(requestedPage)  // ✅ Use the page user requested
        .pageSize(page.getSize())
        .isFirstPage(requestedPage == 1)
        .isLastPage(requestedPage == page.getTotalPages())
        .hasNext(requestedPage < page.getTotalPages())
        .hasPrevious(requestedPage > 1)
        .build();
  }
}