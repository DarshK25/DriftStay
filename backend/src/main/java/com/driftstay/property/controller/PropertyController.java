package com.driftstay.property.controller;

import com.driftstay.property.dto.PropertySummary;
import com.driftstay.property.entity.Property;
import com.driftstay.property.service.PropertyService;
import com.driftstay.security.filter.JwtUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    /** Create a new property. POST /api/properties */
    @PostMapping
    public ResponseEntity<Property> createProperty(
            @RequestBody Property property,
            @AuthenticationPrincipal JwtUser currentUser) {
        Property created = propertyService.createProperty(property, currentUser.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** Update an existing property. PUT /api/properties/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<Property> updateProperty(
            @PathVariable Long id,
            @RequestBody Property property,
            @AuthenticationPrincipal JwtUser currentUser) {
        Property updated = propertyService.updateProperty(id, property);
        return ResponseEntity.ok(updated);
    }

    /** Delete/archive a property. DELETE /api/properties/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUser currentUser) {
        propertyService.deleteProperty(id);
        return ResponseEntity.noContent().build();
    }

    /** Get property details. GET /api/properties/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<Property> getProperty(@PathVariable Long id) {
        return ResponseEntity.ok(propertyService.getProperty(id));
    }

    /** Get property by slug. GET /api/properties/slug/{slug} */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<Property> getPropertyBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(propertyService.getPropertyBySlug(slug));
    }

    /** List all active properties. GET /api/properties */
    @GetMapping
    public ResponseEntity<List<Property>> listProperties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(propertyService.listProperties());
    }

    /** Upload images for a property. POST /api/properties/{id}/images */
    @PostMapping("/{id}/images")
    public ResponseEntity<List<String>> uploadImages(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal JwtUser currentUser) {
        List<String> urls = propertyService.uploadImages(id, files);
        return ResponseEntity.ok(urls);
    }
}
