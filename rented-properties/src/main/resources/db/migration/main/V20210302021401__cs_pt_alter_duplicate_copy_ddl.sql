--Removing APRO chanrges column from Duplicate copy

ALTER TABLE cs_pt_duplicatecopy_applicant DROP COLUMN apro_charge;

ALTER TABLE cs_pt_duplicatecopy_applicant_audit DROP COLUMN apro_charge;
