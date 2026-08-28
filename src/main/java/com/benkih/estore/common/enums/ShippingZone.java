package com.benkih.estore.common.enums;


import com.benkih.estore.user.entity.Address;

public enum ShippingZone {

  LAGOS("LAGOS"),
  ABUJA("ABUJA"),
  SOUTH_WEST("SOUTH_WEST"),
  SOUTH_EAST("SOUTH_EAST"),
  SOUTH_SOUTH("SOUTH_SOUTH"),
  NORTH_CENTRAL("NORTH_CENTRAL"),
  NORTH_EAST("NORTH_EAST"),
  NORTH_WEST("NORTH_WEST");

  private final String code;

  ShippingZone(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static ShippingZone from(Address address) {

    if (address == null) {
      throw new IllegalArgumentException(
          "Address is required"
      );
    }

    String state = address.getState();

    if (state == null || state.isBlank()) {
      throw new IllegalArgumentException(
          "Address state is required"
      );
    }

    return switch (state.trim().toUpperCase()) {

      case "LAGOS" ->
          LAGOS;

      case "FCT", "ABUJA" ->
          ABUJA;

      case "IMO", "ABIA", "ANAMBRA",
           "ENUGU", "EBONYI" ->
          SOUTH_EAST;

      case "RIVERS", "DELTA", "EDO",
           "AKWA IBOM", "CROSS RIVER",
           "BAYELSA" ->
          SOUTH_SOUTH;

      case "OYO", "OGUN", "OSUN",
           "ONDO", "EKITI" ->
          SOUTH_WEST;

      case "BENUE", "KOGI", "KWARA",
           "NASARAWA", "NIGER",
           "PLATEAU" ->
          NORTH_CENTRAL;

      case "ADAMAWA", "BAUCHI",
           "BORNO", "GOMBE",
           "TARABA", "YOBE" ->
          NORTH_EAST;

      case "KADUNA", "KANO",
           "KATSINA", "KEBBI",
           "SOKOTO", "ZAMFARA",
           "JIGAWA" ->
          NORTH_WEST;

      default ->
          throw new IllegalArgumentException(
              "Unsupported shipping state: " + state
          );
    };
  }
}