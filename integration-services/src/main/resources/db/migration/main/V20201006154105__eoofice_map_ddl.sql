CREATE TABLE employee_post_detail_map
(
  uuid character varying(64) NOT NULL,
  employee_id character varying(64),
   employee_code character varying(64),
  post_detail_id  character varying(64),
  post  character varying(64),
  employee_name character varying(64),
  employee_designation character varying(64),
  employee_email character varying(64),
  is_active boolean,
  created_by character varying(64) NOT NULL,
  created_time bigint,
  last_modified_by character varying(64),
  last_modified_time bigint,
  CONSTRAINT uk_employee_post_detail_map UNIQUE (employee_code,post_detail_id)
)
