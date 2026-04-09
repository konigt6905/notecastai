-- =====================================================
-- CREATE GAME NOTE STATISTICS TABLE
-- =====================================================

-- =====================================================
-- TABLE: game_note_statistics
-- =====================================================
CREATE TABLE IF NOT EXISTS notecastai.game_note_statistics (
    id BIGSERIAL PRIMARY KEY,
    game_note_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,

    -- Attempt tracking
    attempt_number INTEGER NOT NULL DEFAULT 1,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP WITH TIME ZONE,

    -- Question type (denormalized for filtering)
    question_type VARCHAR(30) NOT NULL,

    -- Core metrics (all types)
    total_questions INTEGER NOT NULL,
    questions_attempted INTEGER NOT NULL,
    questions_correct INTEGER NOT NULL,
    correctness_percentage DECIMAL(5,2) NOT NULL,

    -- Time tracking
    total_time_seconds INTEGER NOT NULL,
    average_time_per_question DECIMAL(8,2),

    -- Scoring
    final_score INTEGER NOT NULL,
    score_breakdown JSONB,

    -- Type-specific statistics
    type_specific_stats JSONB,

    -- BaseEntity audit fields
    created_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version INTEGER NOT NULL DEFAULT 0,
    created_by BIGINT,
    updated_by BIGINT,
    inactive BOOLEAN NOT NULL DEFAULT FALSE,

    -- Foreign keys
    CONSTRAINT fk_game_stats_game_note FOREIGN KEY (game_note_id)
        REFERENCES notecastai.game_note(id) ON DELETE CASCADE,
    CONSTRAINT fk_game_stats_user FOREIGN KEY (user_id)
        REFERENCES notecastai."user"(id) ON DELETE CASCADE,

    -- Constraints
    CONSTRAINT chk_correctness_percentage
        CHECK (correctness_percentage >= 0 AND correctness_percentage <= 100),
    CONSTRAINT chk_final_score
        CHECK (final_score >= 0 AND final_score <= 100),
    CONSTRAINT chk_questions_attempted
        CHECK (questions_attempted >= 0 AND questions_attempted <= total_questions),
    CONSTRAINT chk_questions_correct
        CHECK (questions_correct >= 0 AND questions_correct <= questions_attempted)
);

-- Indexes for common queries
CREATE INDEX IF NOT EXISTS idx_game_stats_user ON notecastai.game_note_statistics(user_id);
CREATE INDEX IF NOT EXISTS idx_game_stats_game_note ON notecastai.game_note_statistics(game_note_id);
CREATE INDEX IF NOT EXISTS idx_game_stats_user_game ON notecastai.game_note_statistics(user_id, game_note_id);
CREATE INDEX IF NOT EXISTS idx_game_stats_completed_at ON notecastai.game_note_statistics(completed_at DESC);
CREATE INDEX IF NOT EXISTS idx_game_stats_score ON notecastai.game_note_statistics(final_score DESC);
CREATE INDEX IF NOT EXISTS idx_game_stats_inactive ON notecastai.game_note_statistics(inactive) WHERE inactive = FALSE;

-- =====================================================
-- COMMENTS
-- =====================================================
COMMENT ON TABLE notecastai.game_note_statistics IS 'User performance statistics and analytics for game note sessions';
COMMENT ON COLUMN notecastai.game_note_statistics.attempt_number IS 'Sequential attempt number for this user and game note combination';
COMMENT ON COLUMN notecastai.game_note_statistics.score_breakdown IS 'JSON breakdown of how the final score was calculated';
COMMENT ON COLUMN notecastai.game_note_statistics.type_specific_stats IS 'Question type-specific statistics stored as JSON';
