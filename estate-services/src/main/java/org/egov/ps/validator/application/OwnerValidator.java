package org.egov.ps.validator.application;

import java.util.Collections;
import java.util.List;

import org.egov.ps.model.Property;
import org.egov.ps.model.PropertyCriteria;
import org.egov.ps.repository.PropertyQueryBuilder;
import org.egov.ps.repository.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OwnerValidator {

    private static final String NO_OWNER_FOUND = "No owner with id '%s' found for property '%s'";

    @Autowired
    PropertyRepository propertyRepository;

    public List<String> validateOwner(String propertyId, String ownerId) {
        PropertyCriteria propertySearchCriteria = PropertyCriteria.builder().propertyId(propertyId)
                .relations(Collections.singletonList(PropertyQueryBuilder.RELATION_OWNER)).build();
        List<Property> properties = propertyRepository.getProperties(propertySearchCriteria);
        if (CollectionUtils.isEmpty(properties)) {
            log.warn("Owner validator could not find atleast one property for id {}", propertyId);
            return Collections
                    .singletonList(String.format("Owner validator could not find a property with id %s", propertyId));
        }
        if (properties.size() > 1) {
            log.warn("Owner validator found more than one property for id {}", propertyId);
            return Collections
                    .singletonList(String.format("Owner validator found more than one property for id %s", propertyId));
        }
        Property firstProperty = properties.get(0);
        if (CollectionUtils.isEmpty(firstProperty.getPropertyDetails().getOwners())) {
            log.warn("No owners found for property with id {}", propertyId);
            return Collections.singletonList(String.format(NO_OWNER_FOUND, ownerId, propertyId));
        }
        boolean ownerIdFound = firstProperty.getPropertyDetails().getOwners().stream()
                .anyMatch(owner -> owner.getId().equalsIgnoreCase(ownerId));
        if (!ownerIdFound) {
            return Collections.singletonList(String.format(NO_OWNER_FOUND, ownerId, propertyId));
        }
        return null;
    }
}
