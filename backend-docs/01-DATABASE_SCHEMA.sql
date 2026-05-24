-- QuizMaster Database Schema
-- PostgreSQL 15+
-- This file contains the complete database schema for the QuizMaster application

-- ============================================
-- DROP EXISTING TABLES (for development only)
-- ============================================
DROP TABLE IF EXISTS question_answers CASCADE;
DROP TABLE IF EXISTS quiz_attempts CASCADE;
DROP TABLE IF EXISTS questions CASCADE;
DROP TABLE IF EXISTS quizzes CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TYPE IF EXISTS oauth_provider_enum CASCADE;
DROP TYPE IF EXISTS question_type_enum CASCADE;
DROP TYPE IF EXISTS quiz_difficulty_enum CASCADE;

-- ============================================
-- ENUMS
-- ============================================

CREATE TYPE oauth_provider_enum AS ENUM ('GOOGLE', 'GITHUB', 'LOCAL');
CREATE TYPE question_type_enum AS ENUM (
    'OBJECTIVE',
    'MULTIPLE_CHOICE',
    'YES_NO_GRID',
    'DRAG_MATCH',
    'DRAG_CLASSIFY',
    'INLINE_DROPDOWN',
    'MATCHING_DROPDOWN'
);
CREATE TYPE quiz_difficulty_enum AS ENUM ('BEGINNER', 'INTERMEDIATE', 'ADVANCED');
CREATE TYPE user_role_enum AS ENUM ('OWNER', 'ADMIN', 'EDITOR', 'VIEWER');

-- ============================================
-- USERS TABLE
-- ============================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
	fullname VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(512),
    oauth_provider oauth_provider_enum NOT NULL DEFAULT 'LOCAL',
	user_role user_role_enum NOT NULL DEFAULT 'VIEWER',
    oauth_id VARCHAR(255),
    password_hash VARCHAR(255), -- Only for LOCAL auth
    is_active BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT unique_oauth_user UNIQUE (oauth_provider, oauth_id)
);

-- Indexes for users
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_oauth_provider ON users(oauth_provider);
CREATE INDEX idx_users_created_at ON users(created_at);

-- ============================================
-- CATEGORIES TABLE
-- ============================================
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    icon VARCHAR(50), -- Emoji or icon identifier
    color VARCHAR(20), -- Hex color code
    is_custom BOOLEAN DEFAULT FALSE,
    created_by_user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
	updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_slug_format CHECK (slug ~* '^[a-z0-9-]+$')
);

-- Indexes for categories
CREATE INDEX idx_categories_slug ON categories(slug);
CREATE INDEX idx_categories_is_custom ON categories(is_custom);

-- Insert default categories
INSERT INTO categories (name, slug, description, icon, color, is_custom) VALUES
('Azure AZ-900', 'azure', 'Microsoft Azure Fundamentals Certification', '☁️', '#0078D4', FALSE),
('AWS Cloud Practitioner', 'aws', 'AWS Certified Cloud Practitioner', '🌩️', '#FF9900', FALSE),
('Salesforce Admin', 'salesforce', 'Salesforce Administrator Certification', '⚡', '#00A1E0', FALSE),
('MuleSoft', 'mulesoft', 'MuleSoft Certified Developer', '🔗', '#00A0DF', FALSE),
('Google Cloud', 'gcp', 'Google Cloud Digital Leader', '🌐', '#4285F4', FALSE),
('CompTIA A+', 'comptia', 'CompTIA A+ Core Certification', '💻', '#E4022D', FALSE);

-- ============================================
-- QUIZZES TABLE
-- ============================================
CREATE TABLE quizzes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    description TEXT,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    creator_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    difficulty quiz_difficulty_enum DEFAULT 'INTERMEDIATE',
    duration_minutes INTEGER NOT NULL CHECK (duration_minutes > 0),
    passing_percentage INTEGER DEFAULT 70 CHECK (passing_percentage >= 0 AND passing_percentage <= 100),
    total_questions INTEGER DEFAULT 0,
    is_published BOOLEAN DEFAULT TRUE,
    is_featured BOOLEAN DEFAULT FALSE,
    total_attempts INTEGER DEFAULT 0,
    average_score DECIMAL(5,2) DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT unique_quiz_slug UNIQUE (slug),
    CONSTRAINT chk_quiz_slug_format CHECK (slug ~* '^[a-z0-9-]+$')
);

-- Indexes for quizzes
CREATE INDEX idx_quizzes_category ON quizzes(category_id);
CREATE INDEX idx_quizzes_creator ON quizzes(creator_id);
CREATE INDEX idx_quizzes_difficulty ON quizzes(difficulty);
CREATE INDEX idx_quizzes_is_published ON quizzes(is_published);
CREATE INDEX idx_quizzes_is_featured ON quizzes(is_featured);
CREATE INDEX idx_quizzes_created_at ON quizzes(created_at DESC);

-- ============================================
-- QUESTIONS TABLE
-- ============================================
CREATE TABLE questions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quiz_id UUID NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    question_type question_type_enum NOT NULL,
    question_text TEXT NOT NULL,
    question_image_url VARCHAR(512), -- Optional image for OBJECTIVE and MULTIPLE_CHOICE
    explanation TEXT NOT NULL,
    position INTEGER NOT NULL, -- Order of question in quiz

    -- JSON columns for polymorphic question data
    -- Structure varies by question_type
    options JSONB, -- For OBJECTIVE and MULTIPLE_CHOICE
    correct_answer JSONB NOT NULL, -- Stores correct answer(s) in various formats

    -- For YES_NO_GRID
    statements JSONB, -- Array of {id, text, correctAnswer}

    -- For DRAG_MATCH
    match_pairs JSONB, -- Array of {id, term, definition}

    -- For DRAG_CLASSIFY
    categories JSONB, -- Array of {id, name}
    classify_items JSONB, -- Array of {id, text, correctCategoryId}

    -- For INLINE_DROPDOWN
    sentence_template TEXT, -- Template with [select] placeholders
    inline_dropdowns JSONB, -- Array of {id, options, correctAnswer}

    -- For MATCHING_DROPDOWN
    dropdown_rows JSONB, -- Array of {id, label, options, correctAnswer}

    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT unique_question_position UNIQUE (quiz_id, position)
);

-- Indexes for questions
CREATE INDEX idx_questions_quiz_id ON questions(quiz_id);
CREATE INDEX idx_questions_type ON questions(question_type);
CREATE INDEX idx_questions_position ON questions(quiz_id, position);

-- GIN indexes for JSONB columns (for efficient querying)
CREATE INDEX idx_questions_options ON questions USING GIN (options);
CREATE INDEX idx_questions_correct_answer ON questions USING GIN (correct_answer);

-- ============================================
-- QUIZ_ATTEMPTS TABLE
-- ============================================
CREATE TABLE quiz_attempts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    quiz_id UUID NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    mode VARCHAR(20) NOT NULL CHECK (mode IN ('REAL', 'PRACTICE')),
    score DECIMAL(5,2) NOT NULL CHECK (score >= 0 AND score <= 100),
    total_questions INTEGER NOT NULL,
    correct_answers INTEGER NOT NULL,
    time_taken_seconds INTEGER NOT NULL,
    passing_score INTEGER NOT NULL, -- Snapshot of quiz's passing score at attempt time
    is_passed BOOLEAN GENERATED ALWAYS AS (score >= passing_score) STORED,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_attempt_time CHECK (completed_at > started_at),
    CONSTRAINT chk_correct_answers CHECK (correct_answers <= total_questions)
);

-- Indexes for quiz_attempts
CREATE INDEX idx_attempts_user ON quiz_attempts(user_id);
CREATE INDEX idx_attempts_quiz ON quiz_attempts(quiz_id);
CREATE INDEX idx_attempts_completed_at ON quiz_attempts(completed_at DESC);
CREATE INDEX idx_attempts_user_quiz ON quiz_attempts(user_id, quiz_id);
CREATE INDEX idx_attempts_score ON quiz_attempts(score DESC);

-- ============================================
-- QUESTION_ANSWERS TABLE
-- ============================================
CREATE TABLE question_answers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attempt_id UUID NOT NULL REFERENCES quiz_attempts(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    user_answer JSONB NOT NULL, -- Stores user's answer in format matching question type
    is_correct BOOLEAN NOT NULL,
	is_answer_revealed BOOLEAN NOT NULL DEFAULT FALSE,
    points_earned DECIMAL(5,2) DEFAULT 0.00,
    time_spent_seconds INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT unique_attempt_question UNIQUE (attempt_id, question_id)
);

-- Indexes for question_answers
CREATE INDEX idx_answers_attempt ON question_answers(attempt_id);
CREATE INDEX idx_answers_question ON question_answers(question_id);
CREATE INDEX idx_answers_is_correct ON question_answers(is_correct);

-- GIN index for JSONB user_answer column
CREATE INDEX idx_answers_user_answer ON question_answers USING GIN (user_answer);

-- ============================================
-- VIEWS
-- ============================================

-- View: User Statistics
CREATE VIEW user_statistics AS
SELECT
    u.id AS user_id,
    u.name AS user_name,
    u.email,
    COUNT(DISTINCT qa.quiz_id) AS total_quizzes_taken,
    COUNT(qa.id) AS total_attempts,
    AVG(qa.score) AS average_score,
    MAX(qa.score) AS best_score,
    SUM(qa.time_taken_seconds) AS total_time_spent_seconds,
    COUNT(CASE WHEN qa.is_passed THEN 1 END) AS passed_attempts,
    COUNT(CASE WHEN NOT qa.is_passed THEN 1 END) AS failed_attempts
FROM users u
LEFT JOIN quiz_attempts qa ON u.id = qa.user_id
GROUP BY u.id, u.name, u.email;

-- View: Quiz Statistics
CREATE VIEW quiz_statistics AS
SELECT
    q.id AS quiz_id,
    q.title AS quiz_title,
    q.category_id,
    c.name AS category_name,
    q.creator_id,
    q.total_questions,
    COUNT(qa.id) AS total_attempts,
    AVG(qa.score) AS average_score,
    COUNT(DISTINCT qa.user_id) AS unique_users,
    COUNT(CASE WHEN qa.is_passed THEN 1 END) AS passed_attempts,
    COUNT(CASE WHEN NOT qa.is_passed THEN 1 END) AS failed_attempts,
    ROUND((COUNT(CASE WHEN qa.is_passed THEN 1 END)::DECIMAL / NULLIF(COUNT(qa.id), 0)) * 100, 2) AS pass_rate
FROM quizzes q
LEFT JOIN categories c ON q.category_id = c.id
LEFT JOIN quiz_attempts qa ON q.id = qa.quiz_id
GROUP BY q.id, q.title, q.category_id, c.name, q.creator_id, q.total_questions;

-- View: Recent Activity (for dashboard)
CREATE VIEW recent_activity AS
SELECT
    qa.id AS attempt_id,
    qa.user_id,
    u.name AS user_name,
    qa.quiz_id,
    q.title AS quiz_title,
    c.name AS category_name,
    qa.score,
    qa.is_passed,
    qa.completed_at
FROM quiz_attempts qa
JOIN users u ON qa.user_id = u.id
JOIN quizzes q ON qa.quiz_id = q.id
JOIN categories c ON q.category_id = c.id
ORDER BY qa.completed_at DESC;

-- ============================================
-- TRIGGERS
-- ============================================

-- Trigger: Update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_quizzes_updated_at
    BEFORE UPDATE ON quizzes
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_questions_updated_at
    BEFORE UPDATE ON questions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger: Update quiz statistics after attempt
CREATE OR REPLACE FUNCTION update_quiz_statistics()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE quizzes
    SET
        total_attempts = (SELECT COUNT(*) FROM quiz_attempts WHERE quiz_id = NEW.quiz_id),
        average_score = (SELECT AVG(score) FROM quiz_attempts WHERE quiz_id = NEW.quiz_id)
    WHERE id = NEW.quiz_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_quiz_statistics
    AFTER INSERT ON quiz_attempts
    FOR EACH ROW
    EXECUTE FUNCTION update_quiz_statistics();

-- ============================================
-- SAMPLE DATA (for development/testing)
-- ============================================

-- Sample User
INSERT INTO users (email, name, avatar_url, oauth_provider, oauth_id) VALUES
('coursecanon@example.com', 'coursecanon', 'Course Canon', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=64&h=64&fit=crop', 'GOOGLE', 'google-123456');

-- Sample Quiz
INSERT INTO quizzes (title, slug, description, category_id, creator_id, duration_minutes, total_questions)
SELECT
    'Azure Fundamentals Practice Test',
    'azure-fundamentals-practice',
    'Comprehensive test covering Azure basics',
    (SELECT id FROM categories WHERE slug = 'azure'),
    (SELECT id FROM users WHERE email = 'coursecanon@example.com'),
    60,
    5;

-- Sample Questions
INSERT INTO questions (quiz_id, question_type, question_text, explanation, position, options, correct_answer)
SELECT
    (SELECT id FROM quizzes WHERE slug = 'azure-fundamentals-practice'),
    'OBJECTIVE',
    'What is Azure?',
    'Azure is Microsoft''s cloud computing platform.',
    1,
    '["A cloud platform", "A database", "An operating system", "A programming language"]'::jsonb,
    '{"answer": 0}'::jsonb;

-- ============================================
-- FUNCTIONS
-- ============================================

-- Function: Get user's quiz history
CREATE OR REPLACE FUNCTION get_user_quiz_history(p_user_id UUID)
RETURNS TABLE (
    quiz_id UUID,
    quiz_title VARCHAR,
    category_name VARCHAR,
    score DECIMAL,
    completed_at TIMESTAMP WITH TIME ZONE,
    is_passed BOOLEAN
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        q.id,
        q.title,
        c.name,
        qa.score,
        qa.completed_at,
        qa.is_passed
    FROM quiz_attempts qa
    JOIN quizzes q ON qa.quiz_id = q.id
    JOIN categories c ON q.category_id = c.id
    WHERE qa.user_id = p_user_id
    ORDER BY qa.completed_at DESC;
END;
$$ LANGUAGE plpgsql;

-- Function: Get leaderboard for a quiz
CREATE OR REPLACE FUNCTION get_quiz_leaderboard(p_quiz_id UUID, p_limit INTEGER DEFAULT 10)
RETURNS TABLE (
    rank BIGINT,
    user_id UUID,
    user_name VARCHAR,
    score DECIMAL,
    completed_at TIMESTAMP WITH TIME ZONE
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        ROW_NUMBER() OVER (ORDER BY qa.score DESC, qa.time_taken_seconds ASC) AS rank,
        u.id,
        u.name,
        qa.score,
        qa.completed_at
    FROM quiz_attempts qa
    JOIN users u ON qa.user_id = u.id
    WHERE qa.quiz_id = p_quiz_id
    ORDER BY qa.score DESC, qa.time_taken_seconds ASC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- GRANTS (adjust based on your user setup)
-- ============================================

-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO quizmaster_app;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO quizmaster_app;

-- ============================================
-- COMMENTS
-- ============================================

COMMENT ON TABLE users IS 'Stores user authentication and profile information';
COMMENT ON TABLE categories IS 'Quiz categories (Azure, AWS, etc.)';
COMMENT ON TABLE quizzes IS 'Quiz metadata and configuration';
COMMENT ON TABLE questions IS 'Quiz questions with polymorphic structure using JSONB';
COMMENT ON TABLE quiz_attempts IS 'User quiz completion records';
COMMENT ON TABLE question_answers IS 'Individual question answers within an attempt';

COMMENT ON COLUMN questions.correct_answer IS 'JSONB structure varies by question type. Examples:
- OBJECTIVE: {"answer": 2}
- MULTIPLE_CHOICE: {"answers": [0, 2, 3]}
- YES_NO_GRID: [{"id": "1", "answer": "yes"}, {"id": "2", "answer": "no"}]
- DRAG_MATCH: [{"termId": "1", "definitionId": "1"}, ...]
- DRAG_CLASSIFY: [{"itemId": "1", "categoryId": "1"}, ...]
- INLINE_DROPDOWN: [{"dropdownId": "1", "answer": 0}, ...]
- MATCHING_DROPDOWN: [{"rowId": "1", "answer": 2}, ...]';

-- ============================================
-- END OF SCHEMA
-- ============================================
