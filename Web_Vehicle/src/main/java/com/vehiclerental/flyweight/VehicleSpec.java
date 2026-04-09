package com.vehiclerental.flyweight;

public record VehicleSpec(
    String make,
    String model, 
    String engine,
    int seats,
    String fuelType,
    String transmission
) {}