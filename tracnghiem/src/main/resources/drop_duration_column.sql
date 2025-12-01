-- Run this SQL directly in your database tool (pgAdmin, DBeaver, etc.)
-- to remove duration_minutes column from exam_templates table

ALTER TABLE exam_templates DROP COLUMN IF EXISTS duration_minutes;

