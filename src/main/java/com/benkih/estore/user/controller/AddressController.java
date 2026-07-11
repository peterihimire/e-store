package com.benkih.estore.user.controller;

import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.user.dto.request.CreateAddressRequest;
import com.benkih.estore.user.dto.request.UpdateAddressRequest;
import com.benkih.estore.user.dto.response.AddressResponseDto;
import com.benkih.estore.user.service.IAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("${api.prefix}/addresses")
@RequiredArgsConstructor
public class AddressController {
  private final IAddressService addressService;

  @PostMapping("/address/add")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse> addAddress(@RequestBody CreateAddressRequest request){
    AddressResponseDto address = addressService.createAddress(request);
    return ResponseEntity.ok(new ApiResponse("success", "Address added", address));
  }

  @GetMapping("/address/all")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse> getAllAddresses(){
    List<AddressResponseDto> adresses = addressService.getMyAddresses();
    return ResponseEntity.ok(new ApiResponse("success", "All my addresses",
        adresses));
  }

  @PutMapping("/address/{slug}/update")
  @PreAuthorize("isAuthenticated")
  public ResponseEntity<ApiResponse> updateAddress(@PathVariable String slug,
                                                   @RequestBody UpdateAddressRequest request){
    AddressResponseDto addressDto = addressService.updateAddress(slug, request);
    return ResponseEntity.ok(new ApiResponse("success", "Address updated",
        addressDto));
  }

}
