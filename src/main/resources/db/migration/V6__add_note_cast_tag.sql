-- Create join table for NoteCast-Tag Many-to-Many relationship
CREATE TABLE notecastai.note_cast_tag (
    note_cast_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (note_cast_id, tag_id),
    CONSTRAINT fk_notecasttag_notecast FOREIGN KEY (note_cast_id) REFERENCES notecastai.note_cast(id) ON DELETE CASCADE,
    CONSTRAINT fk_notecasttag_tag FOREIGN KEY (tag_id) REFERENCES notecastai.tag(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_notecasttag_notecast ON notecastai.note_cast_tag(note_cast_id);
CREATE INDEX idx_notecasttag_tag ON notecastai.note_cast_tag(tag_id);
