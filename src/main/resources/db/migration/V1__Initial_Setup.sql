-- =====================================================
-- EXTENSIONS
-- =====================================================
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- =====================================================
-- TABLE: user
-- =====================================================
CREATE TABLE "user" (
                        id BIGSERIAL PRIMARY KEY,
                        clerk_user_id VARCHAR(255) NOT NULL,
                        preferred_language VARCHAR(10) DEFAULT 'AUTO',

    -- BaseEntity audit fields
                        created_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                        updated_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                        version INTEGER NOT NULL DEFAULT 0,
                        created_by BIGINT,
                        updated_by BIGINT,
                        inactive BOOLEAN NOT NULL DEFAULT FALSE,

                        CONSTRAINT uq_user_clerk UNIQUE (clerk_user_id)
);

CREATE UNIQUE INDEX idx_user_clerk ON "user"(clerk_user_id);
CREATE INDEX idx_user_created_date ON "user"(created_date DESC);
CREATE INDEX idx_user_inactive ON "user"(inactive) WHERE inactive = FALSE;

-- =====================================================
-- TABLE: tag
-- =====================================================
CREATE TABLE tag (
                     id BIGSERIAL PRIMARY KEY,
                     user_id BIGINT NOT NULL,
                     name VARCHAR(255) NOT NULL,

    -- BaseEntity audit fields
                     created_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                     updated_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                     version INTEGER NOT NULL DEFAULT 0,
                     created_by BIGINT,
                     updated_by BIGINT,
                     inactive BOOLEAN NOT NULL DEFAULT FALSE,

                     CONSTRAINT fk_tag_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
                     CONSTRAINT uq_tag_user_name UNIQUE (user_id, name)
);

CREATE INDEX idx_tag_user ON tag(user_id);
CREATE INDEX idx_tag_name ON tag(name);
CREATE INDEX idx_tag_inactive ON tag(inactive) WHERE inactive = FALSE;
CREATE INDEX idx_tag_created_date ON tag(created_date DESC);

-- =====================================================
-- TABLE: note
-- =====================================================
CREATE TABLE note (
                      id BIGSERIAL PRIMARY KEY,
                      user_id BIGINT NOT NULL,
                      title VARCHAR(255) NOT NULL,
                      knowledge_base TEXT,
                      formatted_note TEXT,
                      current_formate VARCHAR(40) NOT NULL,

    -- BaseEntity audit fields
                      created_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                      updated_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                      version INTEGER NOT NULL DEFAULT 0,
                      created_by BIGINT,
                      updated_by BIGINT,
                      inactive BOOLEAN NOT NULL DEFAULT FALSE,

                      CONSTRAINT fk_note_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

CREATE INDEX idx_note_user ON note(user_id);
CREATE INDEX idx_note_created ON note(created_date DESC);
CREATE INDEX idx_note_inactive ON note(inactive) WHERE inactive = FALSE;
CREATE INDEX idx_note_user_created ON note(user_id, created_date DESC);
CREATE INDEX idx_note_formate ON note(current_formate);
CREATE INDEX idx_note_title_search ON note USING gin(to_tsvector('english', title));
CREATE INDEX idx_note_content_search ON note USING gin(to_tsvector('english', COALESCE(formatted_note, '')));

-- =====================================================
-- TABLE: note_tag (Join Table)
-- =====================================================
CREATE TABLE note_tag (
                          note_id BIGINT NOT NULL,
                          tag_id BIGINT NOT NULL,

                          PRIMARY KEY (note_id, tag_id),
                          CONSTRAINT fk_notetag_note FOREIGN KEY (note_id) REFERENCES note(id) ON DELETE CASCADE,
                          CONSTRAINT fk_notetag_tag FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE
);

CREATE INDEX idx_notetag_note ON note_tag(note_id);
CREATE INDEX idx_notetag_tag ON note_tag(tag_id);

-- =====================================================
-- TABLE: note_ai_action
-- =====================================================
CREATE TABLE note_ai_action (
                                note_id BIGINT NOT NULL,
                                name VARCHAR(200) NOT NULL,
                                prompt TEXT NOT NULL,
                                position INTEGER NOT NULL,

                                PRIMARY KEY (note_id, position),
                                CONSTRAINT fk_ai_action_note FOREIGN KEY (note_id) REFERENCES note(id) ON DELETE CASCADE
);

CREATE INDEX idx_ai_action_note ON note_ai_action(note_id);

-- =====================================================
-- TABLE: audio_file (Voice Notes)
-- =====================================================
CREATE TABLE audio_file (
                            id BIGSERIAL PRIMARY KEY,
                            user_id BIGINT NOT NULL,
                            note_id BIGINT,
                            filename VARCHAR(255) NOT NULL,
                            original_filename VARCHAR(255) NOT NULL,
                            content_type VARCHAR(100) NOT NULL,
                            file_size BIGINT NOT NULL,
                            user_instructions TEXT NOT NULL,
                            s3_file_url TEXT NOT NULL,
                            status VARCHAR(20) NOT NULL,
                            transcript TEXT,
                            word_timestamps_json TEXT,
                            segment_timestamps_json TEXT,
                            language VARCHAR(10) DEFAULT 'AUTO',
                            duration_seconds INTEGER,
                            transcript_processing_time_ms BIGINT,
                            error_message TEXT,

    -- BaseEntity audit fields
                            created_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                            updated_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                            version INTEGER NOT NULL DEFAULT 0,
                            created_by BIGINT,
                            updated_by BIGINT,
                            inactive BOOLEAN NOT NULL DEFAULT FALSE,

                            CONSTRAINT fk_audio_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
                            CONSTRAINT fk_audio_note FOREIGN KEY (note_id) REFERENCES note(id) ON DELETE SET NULL
);

CREATE INDEX idx_audio_user ON audio_file(user_id);
CREATE INDEX idx_audio_status ON audio_file(status);
CREATE INDEX idx_audio_note ON audio_file(note_id);
CREATE INDEX idx_audio_created_date ON audio_file(created_date DESC);
CREATE INDEX idx_audio_user_created ON audio_file(user_id, created_date DESC);
CREATE INDEX idx_audio_user_status ON audio_file(user_id, status);
CREATE INDEX idx_audio_inactive ON audio_file(inactive) WHERE inactive = FALSE;
CREATE INDEX idx_audio_transcript_search ON audio_file USING gin(to_tsvector('english', COALESCE(transcript, '')));

-- =====================================================
-- TABLE: note_cast (Text-to-Speech Audio)
-- =====================================================
CREATE TABLE note_cast (
                           id BIGSERIAL PRIMARY KEY,
                           note_id BIGINT NOT NULL,
                           s3_file_url TEXT,
                           transcript TEXT,
                           duration_seconds INTEGER,
                           audio_processing_time_seconds BIGINT,
                           language VARCHAR(10) DEFAULT 'ENGLISH',
                           status VARCHAR(20) NOT NULL,
                           style VARCHAR(30) NOT NULL,
                           error_message TEXT,

    -- BaseEntity audit fields
                           created_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                           updated_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
                           version INTEGER NOT NULL DEFAULT 0,
                           created_by BIGINT,
                           updated_by BIGINT,
                           inactive BOOLEAN NOT NULL DEFAULT FALSE,

                           CONSTRAINT fk_notecast_note FOREIGN KEY (note_id) REFERENCES note(id) ON DELETE CASCADE
);

CREATE INDEX idx_notecast_note ON note_cast(note_id);
CREATE INDEX idx_notecast_status ON note_cast(status);
CREATE INDEX idx_notecast_created ON note_cast(created_date DESC);
CREATE INDEX idx_notecast_note_created ON note_cast(note_id, created_date DESC);
CREATE INDEX idx_notecast_style ON note_cast(style);
CREATE INDEX idx_notecast_inactive ON note_cast(inactive) WHERE inactive = FALSE;

-- =====================================================
-- DATABASE CONFIGURATION
-- =====================================================
ALTER DATABASE notecastai SET default_text_search_config = 'pg_catalog.english';
