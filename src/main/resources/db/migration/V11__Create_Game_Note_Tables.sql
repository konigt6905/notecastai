-- =====================================================
-- CREATE GAME NOTE TABLES
-- =====================================================

-- =====================================================
-- TABLE: game_note
-- =====================================================
CREATE TABLE IF NOT EXISTS notecastai.game_note (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    source_note_id BIGINT NOT NULL,
    title VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    number_of_questions INTEGER NOT NULL,
    question_length VARCHAR(20) NOT NULL,
    answer_length VARCHAR(20) NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    question_type VARCHAR(30) NOT NULL,
    custom_instructions TEXT,
    error_message TEXT,

    -- BaseEntity audit fields
    created_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version INTEGER NOT NULL DEFAULT 0,
    created_by BIGINT,
    updated_by BIGINT,
    inactive BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_game_note_user FOREIGN KEY (user_id) REFERENCES notecastai."user"(id) ON DELETE CASCADE,
    CONSTRAINT fk_game_note_source_note FOREIGN KEY (source_note_id) REFERENCES notecastai.note(id) ON DELETE CASCADE,
    CONSTRAINT chk_game_note_questions CHECK (number_of_questions > 0 AND number_of_questions <= 50)
);

CREATE INDEX IF NOT EXISTS idx_game_note_user ON notecastai.game_note(user_id);
CREATE INDEX IF NOT EXISTS idx_game_note_status ON notecastai.game_note(status);
CREATE INDEX IF NOT EXISTS idx_game_note_source_note ON notecastai.game_note(source_note_id);
CREATE INDEX IF NOT EXISTS idx_game_note_inactive ON notecastai.game_note(inactive) WHERE inactive = FALSE;
CREATE INDEX IF NOT EXISTS idx_game_note_created ON notecastai.game_note(created_date DESC);
CREATE INDEX IF NOT EXISTS idx_game_note_user_created ON notecastai.game_note(user_id, created_date DESC);

-- =====================================================
-- TABLE: game_note_tag (join table)
-- =====================================================
CREATE TABLE IF NOT EXISTS notecastai.game_note_tag (
    game_note_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,

    PRIMARY KEY (game_note_id, tag_id),

    CONSTRAINT fk_gamenotetag_gamenote FOREIGN KEY (game_note_id) REFERENCES notecastai.game_note(id) ON DELETE CASCADE,
    CONSTRAINT fk_gamenotetag_tag FOREIGN KEY (tag_id) REFERENCES notecastai.tag(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_gamenotetag_gamenote ON notecastai.game_note_tag(game_note_id);
CREATE INDEX IF NOT EXISTS idx_gamenotetag_tag ON notecastai.game_note_tag(tag_id);

-- =====================================================
-- TABLE: game_question
-- =====================================================
CREATE TABLE IF NOT EXISTS notecastai.game_question (
    id BIGSERIAL PRIMARY KEY,
    game_note_id BIGINT NOT NULL,
    question_order INTEGER NOT NULL,
    type VARCHAR(30) NOT NULL,
    question_text TEXT NOT NULL,
    correct_answer TEXT,
    answer TEXT,
    explanation TEXT,
    hint TEXT,

    -- BaseEntity audit fields
    created_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version INTEGER NOT NULL DEFAULT 0,
    created_by BIGINT,
    updated_by BIGINT,
    inactive BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_game_question_game_note FOREIGN KEY (game_note_id) REFERENCES notecastai.game_note(id) ON DELETE CASCADE,
    CONSTRAINT uq_game_question_order UNIQUE (game_note_id, question_order)
);

CREATE INDEX IF NOT EXISTS idx_game_question_game_note ON notecastai.game_question(game_note_id);
CREATE INDEX IF NOT EXISTS idx_game_question_order ON notecastai.game_question(game_note_id, question_order);
CREATE INDEX IF NOT EXISTS idx_game_question_inactive ON notecastai.game_question(inactive) WHERE inactive = FALSE;

-- =====================================================
-- TABLE: game_question_option (for multiple choice options)
-- =====================================================
CREATE TABLE IF NOT EXISTS notecastai.game_question_option (
    game_question_id BIGINT NOT NULL,
    option_text TEXT NOT NULL,
    option_order INTEGER NOT NULL,

    CONSTRAINT fk_game_question_option_question FOREIGN KEY (game_question_id) REFERENCES notecastai.game_question(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_game_question_option_question_order ON notecastai.game_question_option(game_question_id, option_order);


