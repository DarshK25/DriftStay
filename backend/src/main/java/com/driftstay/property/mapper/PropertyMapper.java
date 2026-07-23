package com.driftstay.property.mapper;

import com.driftstay.property.dto.response.PropertyDetailResponse;
import com.driftstay.property.dto.response.PropertyPolicyResponse;
import com.driftstay.property.dto.response.PropertySummaryResponse;
import com.driftstay.property.dto.request.PropertyCreateRequest;
import com.driftstay.property.dto.request.PropertyUpdateRequest;
import com.driftstay.property.entity.Property;
import com.driftstay.property.entity.PropertyPolicy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PropertyMapper {

    @Mapping(target = "startingPrice", ignore = true)
    @Mapping(target = "thumbnailUrl", ignore = true)
    PropertySummaryResponse toSummary(Property property);

    @Mapping(target = "imageUrls", ignore = true)
    @Mapping(target = "amenityNames", ignore = true)
    @Mapping(target = "policy", ignore = true)
    PropertyDetailResponse toDetail(Property property);

    PropertyPolicyResponse toPolicy(PropertyPolicy policy);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "bookingCount", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "rooms", ignore = true)
    @Mapping(target = "policy", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    @Mapping(target = "propertyAmenities", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Property toEntity(PropertyCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicId", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "bookingCount", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "rooms", ignore = true)
    @Mapping(target = "policy", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    @Mapping(target = "propertyAmenities", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(PropertyUpdateRequest request, @MappingTarget Property property);

    List<PropertySummaryResponse> toSummaryList(List<Property> properties);
}
