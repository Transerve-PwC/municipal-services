--> Property table index
CREATE INDEX property_index_1 ON cs_ep_property_v1 (
    category, id, tenantId
);

--> Property details table index
CREATE INDEX property_details_index ON cs_ep_property_details_v1 (
    id, tenantId, branch_type
);

--> Application table index
CREATE INDEX application_index_1 ON cs_ep_application_v1 (
    id, tenantId, application_details
);
