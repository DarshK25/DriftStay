package com.driftstay.property.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
public class PropertyPolicyResponse {
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private LocalTime quietHoursStart;
    private LocalTime quietHoursEnd;
    private Boolean noParties;
    private Boolean visitorsAllowed;
    private Boolean petsAllowed;
    private Boolean smokingAllowed;
    private Integer minimumAge;
    private Integer freeCancellationHours;
    private Boolean extraBedAvailable;
}
