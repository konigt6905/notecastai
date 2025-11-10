-- Add title column to note_cast
ALTER TABLE notecastai.note_cast
    ADD COLUMN title VARCHAR(500);

-- Backfill existing rows using note title
UPDATE notecastai.note_cast nc
SET title = n.title
FROM notecastai.note n
WHERE nc.note_id = n.id
  AND nc.title IS NULL;

-- =====================================================
-- Rename audio_file table to voice_note
-- =====================================================

-- Rename the table
ALTER TABLE notecastai.audio_file RENAME TO voice_note;

-- Rename indexes
ALTER INDEX notecastai.idx_audio_user RENAME TO idx_voice_note_user;
ALTER INDEX notecastai.idx_audio_status RENAME TO idx_voice_note_status;

-- Rename constraints
ALTER TABLE notecastai.voice_note RENAME CONSTRAINT fk_audio_user TO fk_voice_note_user;
ALTER TABLE notecastai.voice_note RENAME CONSTRAINT fk_audio_note TO fk_voice_note_note;

-- Update foreign key reference in voice_note_tag table
ALTER TABLE notecastai.voice_note_tag
    DROP CONSTRAINT fk_voicenotetag_voicenote;

ALTER TABLE notecastai.voice_note_tag
    ADD CONSTRAINT fk_voicenotetag_voicenote
        FOREIGN KEY (voice_note_id) REFERENCES notecastai.voice_note(id) ON DELETE CASCADE;
