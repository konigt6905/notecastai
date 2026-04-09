-- Extend note.title to 500 characters
ALTER TABLE notecastai.note
    ALTER COLUMN title TYPE VARCHAR(500);

-- Add title column to audio_file (voice notes)
ALTER TABLE notecastai.audio_file
    ADD COLUMN title VARCHAR(500);

-- Backfill existing rows using original_filename when available
UPDATE notecastai.audio_file
SET title = COALESCE(original_filename, 'Voice Note')
WHERE title IS NULL;

-- Enforce NOT NULL constraint after backfill
ALTER TABLE notecastai.audio_file
    ALTER COLUMN title SET NOT NULL;
