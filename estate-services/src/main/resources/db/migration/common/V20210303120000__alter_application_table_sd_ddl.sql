--> Book Page and Volumn number in Application Table

ALTER TABLE cs_ep_application_v1
ADD COLUMN book_number CHARACTER VARYING (256);

ALTER TABLE cs_ep_application_v1
ADD COLUMN page_number CHARACTER VARYING (256);

ALTER TABLE cs_ep_application_v1
ADD COLUMN volume_number CHARACTER VARYING (256);
