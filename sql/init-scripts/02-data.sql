-- Données de test pour le système de gestion des étudiants
-- Insertion des données de base

-- 1. Insertion des programmes
INSERT INTO programs (label) VALUES 
('Informatique'),
('Mathématiques'),
('Physique'),
('Chimie');

-- 2. Insertion des niveaux
INSERT INTO levels (label) VALUES 
('L1'),
('L2'),
('L3'),
('M1'),
('M2');

-- 3. Insertion des options
INSERT INTO options (label) VALUES 
('Informatique Fondamentale'),
('Mathématiques Appliquées'),
('Physique Quantique'),
('Chimie Organique'),
('Sans Option');

-- 4. Insertion des semestres
INSERT INTO semesters (label, id_level) VALUES 
-- L1
('S1', 1),
('S2', 1),
-- L2
('S3', 2),
('S4', 2),
-- L3
('S5', 3),
('S6', 3);

-- 5. Insertion des semestres académiques
INSERT INTO academic_year_semesters (id_semester, id_option, start_date, end_date) VALUES 
-- S1 2024
(1, 1, '2024-09-01', '2025-01-31'),
(1, 2, '2024-09-01', '2025-01-31'),
-- S2 2024
(2, 1, '2025-02-01', '2025-06-30'),
(2, 2, '2025-02-01', '2025-06-30');

-- 6. Insertion des sessions d'examen
INSERT INTO exam_sessions (session_date) VALUES 
('2025-01-15'),
('2025-01-20'),
('2025-06-10'),
('2025-06-15'),
('2025-09-05'); -- Session de rattrapage

-- 7. Insertion des étudiants
INSERT INTO students (last_name, first_name, birth_date, id_program) VALUES 
('Dupont', 'Jean', '2003-05-15', 1),
('Martin', 'Marie', '2002-11-22', 1),
('Bernard', 'Pierre', '2003-03-08', 2),
('Dubois', 'Sophie', '2002-07-30', 1),
('Moreau', 'Luc', '2003-01-14', 3);

-- 8. Insertion des inscriptions
INSERT INTO registrations (registration_number, id_student, id_academic_year_semester, registration_date) VALUES 
(2024001, 1, 1, '2024-09-02'),
(2024002, 2, 1, '2024-09-02'),
(2024003, 3, 1, '2024-09-03'),
(2024004, 4, 1, '2024-09-02'),
(2024005, 5, 1, '2024-09-03');

-- 9. Insertion des catégories de matières
INSERT INTO subject_categories (code, label) VALUES 
('OBLIG', 'Obligatoire'),
('OPTION', 'Optionnelle'),
('PROJET', 'Projet'),
('STAGE', 'Stage');

-- 10. Insertion des matières
INSERT INTO subjects (code, title) VALUES 
-- Informatique S1
('INF101', 'Algorithmique'),
('INF102', 'Programmation Java'),
('INF103', 'Bases de données'),
('INF104', 'Systèmes d exploitation'),
('INF105', 'Réseaux'),
('INF106', 'Mathématiques discrètes'),
('INF107', 'Web développement'),

-- Mathématiques S1
('MAT101', 'Algèbre linéaire'),
('MAT102', 'Analyse réelle'),
('MAT103', 'Probabilités'),
('MAT104', 'Statistiques'),

-- Physique S1
('PHY101', 'Mécanique classique'),
('PHY102', 'Électromagnétisme'),
('PHY103', 'Thermodynamique');

-- 11. Insertion des groupes optionnels
INSERT INTO optional_groups (id_subject_category, id_semester, id_option) VALUES 
(2, 1, 1), -- Groupe optionnel Informatique S1
(2, 1, 2), -- Groupe optionnel Mathématiques S1
(3, 1, 1); -- Projet S1

-- 12. Insertion des matières dans les groupes optionnels
INSERT INTO optional_group_subjects (id_subject, id_optional_group) VALUES 
(7, 1), -- Web développement en option informatique
(4, 2); -- Statistiques en option mathématiques

-- 13. Insertion des crédits
INSERT INTO credits (id_subject, id_option, credits) VALUES 
-- Crédits pour Informatique S1
(1, 1, 6),
(2, 1, 6),
(3, 1, 4),
(4, 1, 4),
(5, 1, 3),
(6, 1, 4),
(7, 1, 3),

-- Crédits pour Mathématiques S1
(8, 2, 6),
(9, 2, 6),
(10, 2, 4),
(11, 2, 4);

-- 14. Insertion des mentions
INSERT INTO honors (label, min_threshold, max_threshold) VALUES 
('Très Bien', 16, 20),
('Bien', 14, 15.99),
('Assez Bien', 12, 13.99),
('Passable', 10, 11.99),
('Échec', 0, 9.99);

-- 15. Insertion des notes (S1 2024)
INSERT INTO grades (id_student, id_academic_year_semester, id_subject, id_exam_session, grade) VALUES 
-- Notes pour Jean Dupont (Étudiant 1)
(1, 1, 1, 1, 15.5),
(1, 1, 2, 1, 16.0),
(1, 1, 3, 1, 14.5),
(1, 1, 4, 1, 13.0),
(1, 1, 5, 1, 17.0),
(1, 1, 6, 1, 12.5),
(1, 1, 7, 1, 15.0),

-- Notes pour Marie Martin (Étudiant 2)
(2, 1, 1, 1, 18.0),
(2, 1, 2, 1, 17.5),
(2, 1, 3, 1, 16.0),
(2, 1, 4, 1, 15.5),
(2, 1, 5, 1, 18.5),
(2, 1, 6, 1, 14.0),
(2, 1, 7, 1, 16.5),

-- Notes pour Pierre Bernard (Étudiant 3)
(3, 1, 8, 1, 11.0),
(3, 1, 9, 1, 12.5),
(3, 1, 10, 1, 9.5),  -- Note en échec
(3, 1, 11, 1, 13.0),

-- Notes pour Sophie Dubois (Étudiant 4)
(4, 1, 1, 1, 14.0),
(4, 1, 2, 1, 13.5),
(4, 1, 3, 1, 15.0),
(4, 1, 4, 1, 12.0),
(4, 1, 5, 1, 16.5),
(4, 1, 6, 1, 11.5),
(4, 1, 7, 1, 14.5);

-- 16. Insertion des rattrapages (pour l'étudiant qui a échoué)
INSERT INTO retakes (id_student, id_exam_session, id_subject) VALUES 
(3, 5, 10); -- Pierre Bernard en rattrapage pour Probabilités

-- 17. Insertion des transferts de matières
INSERT INTO subject_transfers (id_subject, id_source_semester, id_target_semester) VALUES 
(7, 1, 3); -- Web développement transféré de S1 à S3

-- Vérification et statistiques
DO $$ 
DECLARE 
    student_count INTEGER;
    grade_count INTEGER;
    subject_count INTEGER;
BEGIN
    -- Compter les étudiants
    SELECT COUNT(*) INTO student_count FROM students;
    
    -- Compter les notes
    SELECT COUNT(*) INTO grade_count FROM grades;
    
    -- Compter les matières
    SELECT COUNT(*) INTO subject_count FROM subjects;
    
    RAISE NOTICE '=== DONNÉES DE TEST INSÉRÉES AVEC SUCCÈS ===';
    RAISE NOTICE 'Nombre d étudiants: %', student_count;
    RAISE NOTICE 'Nombre de matières: %', subject_count;
    RAISE NOTICE 'Nombre de notes: %', grade_count;
    RAISE NOTICE 'Base de données prête pour les tests!';
END $$;