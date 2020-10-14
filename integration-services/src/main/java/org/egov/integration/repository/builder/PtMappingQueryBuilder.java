package org.egov.integration.repository.builder;

public class PtMappingQueryBuilder {
	public static final String GET_PT_MAPPING_QUERY="SELECT pt.uuid, pt.user_id, pt.property_tax_id, pt.tenant_id, pt.is_active FROM pt_citizen_mapping pt WHERE pt.user_id=:userId and is_active=true ";
}
