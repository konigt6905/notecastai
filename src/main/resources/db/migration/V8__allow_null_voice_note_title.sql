-- Allow voice note titles to be null
ALTER TABLE notecastai.audio_file
    ALTER COLUMN title DROP NOT NULL;
