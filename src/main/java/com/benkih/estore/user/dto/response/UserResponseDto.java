package com.benkih.estore.user.dto.response;

import com.benkih.estore.cart.dto.response.CartResponseDto;
import com.benkih.estore.order.dto.response.OrderResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
  private String slug;
  private String firstName;
  private String LastName;
  private String email;
  private List<OrderResponseDto> orders;
  private CartResponseDto cart;
  private List<AddressResponseDto> addresses;
}
