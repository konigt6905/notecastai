ALTER TABLE notecastai.audio_file
    ALTER COLUMN user_instructions DROP NOT NULL;

ALTER TABLE notecastai.audio_file
    ALTER COLUMN s3_file_url DROP NOT NULL;