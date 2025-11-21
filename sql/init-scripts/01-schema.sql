-- Script d'initialisation de la base de données Student Management
-- Créé le: $(date)

-- Table des programmes
CREATE TABLE IF NOT EXISTS programs(
    id BIGSERIAL PRIMARY KEY,
    label VARCHAR(50) NOT NULL
);

-- Table des niveaux
CREATE TABLE IF NOT EXISTS levels(
    id BIGSERIAL PRIMARY KEY,
    label VARCHAR(50) NOT NULL
);

-- Table des semestres
CREATE TABLE IF NOT EXISTS semesters(
    id BIGSERIAL PRIMARY KEY,
    label VARCHAR(50) NOT NULL,
    id_level BIGINT,
    CONSTRAINT fk_semester_level FOREIGN KEY (id_level) REFERENCES levels(id)
);

-- Table des options
CREATE TABLE IF NOT EXISTS options(
    id BIGSERIAL PRIMARY KEY,
    label VARCHAR(50) NOT NULL
);

-- Table des semestres académiques
CREATE TABLE IF NOT EXISTS academic_year_semesters(
    id BIGSERIAL PRIMARY KEY,
    id_semester BIGINT NOT NULL,
    id_option BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    CONSTRAINT fk_academic_semester FOREIGN KEY (id_semester) REFERENCES semesters(id),
    CONSTRAINT fk_academic_option FOREIGN KEY (id_option) REFERENCES options(id)
);

-- Table des sessions d'examen
CREATE TABLE IF NOT EXISTS exam_sessions(
    id BIGSERIAL PRIMARY KEY,
    session_date DATE NOT NULL
);

-- Table des étudiants
CREATE TABLE IF NOT EXISTS students(
    id BIGSERIAL PRIMARY KEY,
    last_name VARCHAR(50) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    birth_date DATE NOT NULL,
    id_program BIGINT NOT NULL,
    CONSTRAINT fk_student_program FOREIGN KEY (id_program) REFERENCES programs(id)   
);

-- Table des inscriptions
CREATE TABLE IF NOT EXISTS registrations(
    id BIGSERIAL PRIMARY KEY,
    registration_number DECIMAL UNIQUE NOT NULL,
    id_student BIGINT NOT NULL,
    id_academic_year_semester BIGINT NOT NULL,
    registration_date DATE NOT NULL,
    CONSTRAINT fk_registration_student FOREIGN KEY (id_student) REFERENCES students(id),
    CONSTRAINT fk_registration_academic_semester FOREIGN KEY (id_academic_year_semester) REFERENCES academic_year_semesters(id)
);

-- Table des matières
CREATE TABLE IF NOT EXISTS subjects(
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    title VARCHAR(50) NOT NULL
);

-- Table des catégories de matières
CREATE TABLE IF NOT EXISTS subject_categories(
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) UNIQUE NOT NULL,
    label VARCHAR(50) NOT NULL
);

-- Table des groupes optionnels
CREATE TABLE IF NOT EXISTS optional_groups(
    id BIGSERIAL PRIMARY KEY,
    id_subject_category BIGINT NOT NULL,
    id_semester BIGINT NOT NULL,
    id_option BIGINT NOT NULL,
    CONSTRAINT fk_optional_group_category FOREIGN KEY (id_subject_category) REFERENCES subject_categories(id),
    CONSTRAINT fk_optional_group_semester FOREIGN KEY (id_semester) REFERENCES semesters(id),
    CONSTRAINT fk_optional_group_option FOREIGN KEY (id_option) REFERENCES options(id)
);

-- Table de liaison matières-groupes optionnels
CREATE TABLE IF NOT EXISTS optional_group_subjects( 
    id BIGSERIAL PRIMARY KEY,
    id_subject BIGINT NOT NULL,
    id_optional_group BIGINT NOT NULL,
    CONSTRAINT fk_ogs_subject FOREIGN KEY (id_subject) REFERENCES subjects(id),
    CONSTRAINT fk_ogs_optional_group FOREIGN KEY (id_optional_group) REFERENCES optional_groups(id),
    CONSTRAINT unique_subject_optional_group UNIQUE (id_subject, id_optional_group)
);

-- Table des crédits
CREATE TABLE IF NOT EXISTS credits(
    id BIGSERIAL PRIMARY KEY,
    id_subject BIGINT NOT NULL,
    id_option BIGINT NOT NULL,
    credits BIGINT NOT NULL CHECK (credits >= 0),
    CONSTRAINT fk_credits_subject FOREIGN KEY (id_subject) REFERENCES subjects(id),
    CONSTRAINT fk_credits_option FOREIGN KEY (id_option) REFERENCES options(id),
    CONSTRAINT unique_subject_option UNIQUE (id_subject, id_option)
);

-- Table des mentions
CREATE TABLE IF NOT EXISTS honors(
    id BIGSERIAL PRIMARY KEY,
    label VARCHAR(50) NOT NULL,
    min_threshold DECIMAL NOT NULL,
    max_threshold DECIMAL NOT NULL,
    CONSTRAINT valid_threshold_range CHECK (min_threshold <= max_threshold)
);

-- Table des rattrapages
CREATE TABLE IF NOT EXISTS retakes(
    id BIGSERIAL PRIMARY KEY,
    id_student BIGINT NOT NULL,
    id_exam_session BIGINT NOT NULL,
    id_subject BIGINT NOT NULL,
    CONSTRAINT fk_retake_student FOREIGN KEY (id_student) REFERENCES students(id),
    CONSTRAINT fk_retake_session FOREIGN KEY (id_exam_session) REFERENCES exam_sessions(id),
    CONSTRAINT fk_retake_subject FOREIGN KEY (id_subject) REFERENCES subjects(id),
    CONSTRAINT unique_retake UNIQUE (id_student, id_exam_session, id_subject)
);

-- Table des notes
CREATE TABLE IF NOT EXISTS grades(
    id BIGSERIAL PRIMARY KEY,
    id_student BIGINT NOT NULL,
    id_academic_year_semester BIGINT NOT NULL,
    id_subject BIGINT NOT NULL,
    id_exam_session BIGINT NOT NULL,
    grade FLOAT NOT NULL CHECK (grade >= 0 AND grade <= 20),
    CONSTRAINT fk_grade_student FOREIGN KEY (id_student) REFERENCES students(id),
    CONSTRAINT fk_grade_academic_semester FOREIGN KEY (id_academic_year_semester) REFERENCES academic_year_semesters(id),
    CONSTRAINT fk_grade_subject FOREIGN KEY (id_subject) REFERENCES subjects(id),
    CONSTRAINT fk_grade_session FOREIGN KEY (id_exam_session) REFERENCES exam_sessions(id),
    CONSTRAINT unique_grade UNIQUE (id_student, id_academic_year_semester, id_subject, id_exam_session)
);

-- Table des transferts de matières
CREATE TABLE IF NOT EXISTS subject_transfers(
    id BIGSERIAL PRIMARY KEY,
    id_subject BIGINT NOT NULL,
    id_source_semester BIGINT NOT NULL,
    id_target_semester BIGINT NOT NULL,
    CONSTRAINT fk_transfer_subject FOREIGN KEY (id_subject) REFERENCES subjects(id),
    CONSTRAINT fk_transfer_source_semester FOREIGN KEY (id_source_semester) REFERENCES semesters(id),
    CONSTRAINT fk_transfer_target_semester FOREIGN KEY (id_target_semester) REFERENCES semesters(id),
    CONSTRAINT different_semesters CHECK (id_source_semester != id_target_semester)
);

-- Création des index pour améliorer les performances
CREATE INDEX IF NOT EXISTS idx_students_program ON students(id_program);
CREATE INDEX IF NOT EXISTS idx_grades_student ON grades(id_student);
CREATE INDEX IF NOT EXISTS idx_grades_semester ON grades(id_academic_year_semester);
CREATE INDEX IF NOT EXISTS idx_registrations_student ON registrations(id_student);
CREATE INDEX IF NOT EXISTS idx_registrations_semester ON registrations(id_academic_year_semester);

-- Message de confirmation
DO $$ 
BEGIN
    RAISE NOTICE 'Tables créées avec succès pour Student Management System';
END $$;