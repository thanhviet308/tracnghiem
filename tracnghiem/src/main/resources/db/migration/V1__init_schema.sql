CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE student_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE class_student (
    student_group_id BIGINT NOT NULL REFERENCES student_groups(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (student_group_id, student_id)
);

CREATE TABLE subjects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE chapters (
    id BIGSERIAL PRIMARY KEY,
    subject_id BIGINT NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE passages (
    id BIGSERIAL PRIMARY KEY,
    chapter_id BIGINT NOT NULL REFERENCES chapters(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE questions (
    id BIGSERIAL PRIMARY KEY,
    chapter_id BIGINT NOT NULL REFERENCES chapters(id) ON DELETE CASCADE,
    passage_id BIGINT REFERENCES passages(id) ON DELETE SET NULL,
    content TEXT NOT NULL,
    question_type VARCHAR(20) NOT NULL,
    difficulty VARCHAR(20),
    marks INT NOT NULL DEFAULT 1,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE question_options (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    option_order INT
);

CREATE TABLE question_answers (
    id BIGSERIAL PRIMARY KEY,
    question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    correct_answer TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE exam_templates (
    id BIGSERIAL PRIMARY KEY,
    subject_id BIGINT NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    total_questions INT NOT NULL,
    duration_minutes INT NOT NULL,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE exam_structure (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES exam_templates(id) ON DELETE CASCADE,
    chapter_id BIGINT NOT NULL REFERENCES chapters(id),
    num_question INT NOT NULL
);

CREATE TABLE exam_instances (
    id BIGSERIAL PRIMARY KEY,
    template_id BIGINT NOT NULL REFERENCES exam_templates(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    student_group_id BIGINT NOT NULL REFERENCES student_groups(id),
    start_time TIMESTAMPTZ NOT NULL,
    end_time TIMESTAMPTZ NOT NULL,
    duration_minutes INT NOT NULL,
    shuffle_questions BOOLEAN NOT NULL DEFAULT TRUE,
    shuffle_options BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE exam_questions (
    exam_instance_id BIGINT NOT NULL REFERENCES exam_instances(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    question_order INT,
    PRIMARY KEY (exam_instance_id, question_id)
);

CREATE TABLE exam_supervisors (
    id BIGSERIAL PRIMARY KEY,
    exam_instance_id BIGINT NOT NULL REFERENCES exam_instances(id) ON DELETE CASCADE,
    supervisor_id BIGINT NOT NULL REFERENCES users(id),
    room_number VARCHAR(50),
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE exam_attempts (
    id BIGSERIAL PRIMARY KEY,
    exam_instance_id BIGINT NOT NULL REFERENCES exam_instances(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES users(id),
    started_at TIMESTAMPTZ,
    submitted_at TIMESTAMPTZ,
    score INT,
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED'
);

CREATE TABLE exam_answers (
    id BIGSERIAL PRIMARY KEY,
    attempt_id BIGINT NOT NULL REFERENCES exam_attempts(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES questions(id),
    selected_option_id BIGINT REFERENCES question_options(id),
    fill_answer TEXT,
    is_correct BOOLEAN,
    answered_at TIMESTAMPTZ
);

CREATE TABLE student_group_subjects (
    student_group_id BIGINT NOT NULL REFERENCES student_groups(id) ON DELETE CASCADE,
    subject_id BIGINT NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    teacher_id BIGINT REFERENCES users(id),
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (student_group_id, subject_id)
);

