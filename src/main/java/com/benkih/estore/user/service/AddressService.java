package com.benkih.estore.user.service;

import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.security.user.CurrentUserService;
import com.benkih.estore.user.dto.request.CreateAddressRequest;
import com.benkih.estore.user.dto.request.UpdateAddressRequest;
import com.benkih.estore.user.dto.response.AddressResponseDto;
import com.benkih.estore.user.entity.Address;
import com.benkih.estore.user.entity.User;
import com.benkih.estore.user.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService implements IAddressService {
  private final AddressRepository addressRepository;
  private final CurrentUserService currentUserService;


  @Override
  public AddressResponseDto createAddress(CreateAddressRequest request) {
    User user = currentUserService.getCurrentUser();

    boolean firstAddress =
        addressRepository.findByUserSlug(user.getSlug()).isEmpty();

    Address address = new Address();

    address.setFirstName(request.getFirstName());
    address.setLastName(request.getLastName());
    address.setPhoneNumber(request.getPhoneNumber());
    address.setAddressLine1(request.getAddressLine1());
    address.setAddressLine2(request.getAddressLine2());
    address.setCity(request.getCity());
    address.setState(request.getState());
    address.setCountry(request.getCountry());
    address.setPostalCode(request.getPostalCode());
    //    address.setDefault(request.isDefault());
    if (firstAddress) {
      address.setDefaultAddress(true);
    } else if (request.isDefaultAddress()) {
      addressRepository.clearDefaultAddress(user.getSlug());
      address.setDefaultAddress(true);
    } else {
      address.setDefaultAddress(false);
    }
    address.setUser(user);
    address = addressRepository.save(address);
    return convertToDto(address);
  }

  @Override
  public List<AddressResponseDto> getMyAddresses() {
    User user = currentUserService.getCurrentUser();

    return addressRepository
        .findAllByUserSlugOrderByCreatedAtDesc(user.getSlug())
        .stream()
        .map(this::convertToDto)
        .toList();
  }

  @Override
  public AddressResponseDto updateAddress(String slug, UpdateAddressRequest request) {
    User user = currentUserService.getCurrentUser();

    Address address =
        addressRepository
            .findBySlugAndUserSlug(slug, user.getSlug())
            .orElseThrow(()->
                new ResourceNotFoundException("Address not found."));



    if (request.getFirstName() != null) {
      address.setFirstName(request.getFirstName());
    }

    if (request.getLastName() != null) {
      address.setLastName(request.getLastName());
    }

    if (request.getPhoneNumber() != null) {
      address.setPhoneNumber(request.getPhoneNumber());
    }

    if (request.getAddressLine1() != null) {
      address.setAddressLine1(request.getAddressLine1());
    }

    if (request.getAddressLine2() != null) {
      address.setAddressLine2(request.getAddressLine2());
    }

    if (request.getCity() != null) {
      address.setCity(request.getCity());
    }

    if (request.getState() != null) {
      address.setState(request.getState());
    }

    if (request.getCountry() != null) {
      address.setCountry(request.getCountry());
    }

    if (request.getPostalCode() != null) {
      address.setPostalCode(request.getPostalCode());
    }

    if (request.getDefaultAddress() != null) {
      if (request.getDefaultAddress()) {
        addressRepository.clearDefaultAddress(user.getSlug());
      }
      address.setDefaultAddress(request.getDefaultAddress());
    }

    address = addressRepository.save(address);

    return convertToDto(address);
  }

  @Override
  public AddressResponseDto getAddress(String slug) {
    User user = currentUserService.getCurrentUser();
    Address address =
        addressRepository.findBySlugAndUserSlug(slug,user.getSlug())
            .orElseThrow(() ->
                new ResourceNotFoundException("Address not found."));
    return convertToDto(address);
  }

  @Override
  public void deleteAddress(String slug) {
    User user = currentUserService.getCurrentUser();
    Address address =
        addressRepository.findBySlugAndUserSlug(slug, user.getSlug())
            .orElseThrow(() ->
                new ResourceNotFoundException("Address not found."));

    addressRepository.delete(address);
  }

  public AddressResponseDto convertToDto(Address address){
    AddressResponseDto dto = new AddressResponseDto();

    dto.setSlug(address.getSlug());
        dto.setFirstName(address.getFirstName());
        dto.setLastName(address.getLastName());
    dto.setPhoneNumber(address.getPhoneNumber());
    dto.setAddressLine1(address.getAddressLine1());
    dto.setAddressLine2(address.getAddressLine2());
    dto.setCity(address.getCity());
    dto.setState(address.getState());
    dto.setCountry(address.getCountry());
    dto.setPostalCode(address.getPostalCode());
    dto.setDefaultAddress(address.isDefaultAddress());
    return dto;
  }
}
