ALTER TABLE notecastai.note_cast
    ADD COLUMN voice VARCHAR(50);

ALTER TABLE notecastai.note_cast
    ADD COLUMN size VARCHAR(20);

ALTER TABLE notecastai.note_cast
    ADD COLUMN share_token VARCHAR(100) UNIQUE;

ALTER TABLE notecastai.note_cast
    ADD COLUMN share_expires_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_notecast_share_token ON notecastai.note_cast(share_token)
WHERE share_token IS NOT NULL;

UPDATE notecastai.note_cast
SET voice = 'SARAH_EN'
WHERE voice IS NULL;

UPDATE notecastai.note_cast
SET size = 'EXTRA_SHORT'
WHERE size IS NULL;
