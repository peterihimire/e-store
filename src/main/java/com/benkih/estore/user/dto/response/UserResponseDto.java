package com.benkih.estore.user.dto.response;

import com.benkih.estore.cart.dto.response.CartResponseDto;
import com.benkih.estore.order.dto.response.OrderResponseDto;
import com.benkih.estore.role.dto.response.RoleResponseDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private String slug;
    private String firstName;
    private String LastName;
    private String email;
    private List<RoleResponseDto> roles;
    private List<OrderResponseDto> orders;
    private CartResponseDto cart;
    private List<AddressResponseDto> addresses;
}
