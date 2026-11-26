package com.benkih.estore.user.controller;

import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.user.dto.request.CreateAddressRequest;
import com.benkih.estore.user.dto.request.UpdateAddressRequest;
import com.benkih.estore.user.dto.response.AddressResponseDto;
import com.benkih.estore.user.service.AddressService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Addresses", description = "User delivery addresses")
@RestController
@RequestMapping("${api.prefix}/addresses")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService addressService;

    @PostMapping("/address/add")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AddressResponseDto>> addAddress(
            @RequestBody CreateAddressRequest request) {
        AddressResponseDto address = addressService.createAddress(request);
        return ResponseEntity.ok(new ApiResponse<>("success", "Address added", address));
    }

    @GetMapping("/address/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<AddressResponseDto>>> getAllAddresses() {
        List<AddressResponseDto> adresses = addressService.getMyAddresses();
        return ResponseEntity.ok(new ApiResponse<>("success", "All my addresses", adresses));
    }

    @PutMapping("/address/{slug}/update")
    @PreAuthorize("isAuthenticated")
    public ResponseEntity<ApiResponse<AddressResponseDto>> updateAddress(
            @PathVariable String slug, @RequestBody UpdateAddressRequest request) {
        AddressResponseDto addressDto = addressService.updateAddress(slug, request);
        return ResponseEntity.ok(new ApiResponse<>("success", "Address updated", addressDto));
    }
}
