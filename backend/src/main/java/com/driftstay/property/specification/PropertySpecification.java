package com.driftstay.property.specification;

import com.driftstay.common.enums.PropertyStatus;
import com.driftstay.property.entity.Property;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic query builder for property search using JPA Specification API.
 * Avoids 100 if-else statements by using a clean builder pattern.
 *
 * Usage:
 * <pre>
 * Specification<Property> spec = PropertySpecification.builder()
 *     .city("Goa")
 *     .minPrice(new BigDecimal("1000"))
 *     .maxPrice(new BigDecimal("10000"))
 *     .minRating(new BigDecimal("4.0"))
 *     .guestCount(4)
 *     .build();
 * List<Property> results = propertyRepository.findAll(spec, pageable);
 * </pre>
 */
@Getter
@AllArgsConstructor
public class PropertySpecification implements Specification<Property> {

    private final transient Specification<Property> specification;

    @Override
    public Predicate toPredicate(Root<Property> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        return specification.toPredicate(root, query, cb);
    }

    /**
     * Create a new builder for property search specifications.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for constructing complex property search queries.
     */
    public static class Builder {

        private final List<Specification<Property>> specs = new ArrayList<>();

        /**
         * Filter by city (case-insensitive).
         */
        public Builder city(String city) {
            if (city != null && !city.isBlank()) {
                specs.add((root, query, cb) ->
                        cb.equal(cb.lower(root.get("city")), city.toLowerCase().trim()));
            }
            return this;
        }

        /**
         * Filter by property name or city (text search).
         */
        public Builder searchText(String searchText) {
            if (searchText != null && !searchText.isBlank()) {
                String pattern = "%" + searchText.toLowerCase().trim() + "%";
                specs.add((root, query, cb) ->
                        cb.or(
                                cb.like(cb.lower(root.get("name")), pattern),
                                cb.like(cb.lower(root.get("city")), pattern),
                                cb.like(cb.lower(root.get("shortDescription")), pattern)
                        ));
            }
            return this;
        }

        /**
         * Filter by minimum price (based on the cheapest room).
         * <p>
         * NOTE: Price filtering requires a subquery join with the Room table.
         * This is a placeholder that will be implemented with a proper subquery
         * when the full search is built. Currently this is a no-op to avoid
         * returning incorrect results.
         *
         * @param minPrice Minimum price per night
         * @return this builder
         */
        public Builder minPrice(BigDecimal minPrice) {
            // TODO: Implement with Subquery<BigDecimal> on Room.basePrice
            // This requires a correlated subquery:
            // SELECT p FROM Property p WHERE p.id IN (
            //   SELECT r.property.id FROM Room r GROUP BY r.property.id
            //   HAVING MIN(r.basePrice) >= :minPrice
            // )
            return this;
        }

        /**
         * Filter by maximum price.
         * <p>
         * NOTE: Same as minPrice - requires a subquery join with Room table.
         * Currently a no-op.
         *
         * @param maxPrice Maximum price per night
         * @return this builder
         */
        public Builder maxPrice(BigDecimal maxPrice) {
            // TODO: Implement with Subquery<BigDecimal> on Room.basePrice
            return this;
        }

        /**
         * Filter by minimum rating.
         */
        public Builder minRating(BigDecimal minRating) {
            if (minRating != null) {
                specs.add((root, query, cb) ->
                        cb.greaterThanOrEqualTo(root.get("averageRating"), minRating));
            }
            return this;
        }

        /**
         * Filter by property type.
         */
        public Builder propertyType(String propertyType) {
            if (propertyType != null && !propertyType.isBlank()) {
                specs.add((root, query, cb) ->
                        cb.equal(root.get("propertyType"), propertyType.toUpperCase()));
            }
            return this;
        }

        /**
         * Filter by star category.
         */
        public Builder starCategory(Integer starCategory) {
            if (starCategory != null) {
                specs.add((root, query, cb) ->
                        cb.equal(root.get("starCategory"), starCategory));
            }
            return this;
        }

        /**
         * Filter by active status only (default filter).
         */
        public Builder activeOnly() {
            specs.add((root, query, cb) ->
                    cb.equal(root.get("status"), PropertyStatus.ACTIVE));
            return this;
        }

        /**
         * Filter by specific state.
         */
        public Builder state(String state) {
            if (state != null && !state.isBlank()) {
                specs.add((root, query, cb) ->
                        cb.equal(cb.lower(root.get("state")), state.toLowerCase().trim()));
            }
            return this;
        }

        /**
         * Filter by country (default: India).
         */
        public Builder country(String country) {
            if (country != null && !country.isBlank()) {
                specs.add((root, query, cb) ->
                        cb.equal(cb.lower(root.get("country")), country.toLowerCase().trim()));
            }
            return this;
        }

        /**
         * Add a custom specification.
         */
        public Builder custom(Specification<Property> spec) {
            if (spec != null) {
                specs.add(spec);
            }
            return this;
        }

        /**
         * Build the composite specification.
         * All specifications are AND-ed together.
         */
        public PropertySpecification build() {
            if (specs.isEmpty()) {
                return new PropertySpecification(Specification.where(null));
            }

            Specification<Property> result = Specification.where(specs.get(0));
            for (int i = 1; i < specs.size(); i++) {
                result = result.and(specs.get(i));
            }

            return new PropertySpecification(result);
        }
    }
}
