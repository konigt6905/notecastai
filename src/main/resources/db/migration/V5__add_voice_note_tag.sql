-- =====================================================
-- TABLE: voice_note_tag (Join Table)
-- =====================================================
CREATE TABLE notecastai.voice_note_tag (
                                           voice_note_id BIGINT NOT NULL,
                                           tag_id BIGINT NOT NULL,

                                           PRIMARY KEY (voice_note_id, tag_id),
                                           CONSTRAINT fk_voicenotetag_voicenote FOREIGN KEY (voice_note_id) REFERENCES notecastai.audio_file(id) ON DELETE CASCADE,
                                           CONSTRAINT fk_voicenotetag_tag FOREIGN KEY (tag_id) REFERENCES notecastai.tag(id) ON DELETE CASCADE
);

CREATE INDEX idx_voicenotetag_voicenote ON notecastai.voice_note_tag(voice_note_id);
CREATE INDEX idx_voicenotetag_tag ON notecastai.voice_note_tag(tag_id);
