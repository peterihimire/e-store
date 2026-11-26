package com.benkih.estore.user.service;

import com.benkih.estore.user.dto.request.CreateAddressRequest;
import com.benkih.estore.user.dto.request.UpdateAddressRequest;
import com.benkih.estore.user.dto.response.AddressResponseDto;
import com.benkih.estore.user.entity.Address;
import java.util.List;

public interface AddressService {
    AddressResponseDto createAddress(CreateAddressRequest request);

    List<AddressResponseDto> getMyAddresses();

    AddressResponseDto updateAddress(String slug, UpdateAddressRequest request);

    AddressResponseDto getAddress(String slug);

    void deleteAddress(String slug);

    AddressResponseDto convertToDto(Address address);
}
