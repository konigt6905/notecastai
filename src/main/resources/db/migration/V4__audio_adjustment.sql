ALTER TABLE notecastai."user"
DROP COLUMN IF EXISTS preferred_voice;

ALTER TABLE notecastai."user"
    ADD COLUMN default_voice VARCHAR(40) NOT NULL DEFAULT 'BELLA';