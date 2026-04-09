-- Fix "formate" typo in column names
ALTER TABLE notecastai."user" RENAME COLUMN default_formate TO default_format;
ALTER TABLE notecastai.note RENAME COLUMN current_formate TO current_format;

-- Recreate index with correct name
DROP INDEX IF EXISTS notecastai.idx_note_formate;
CREATE INDEX idx_note_format ON notecastai.note(current_format);
