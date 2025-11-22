-- ==============================================
-- Programs
-- ==============================================
INSERT INTO programs (label) VALUES
('Informatique');

-- ==============================================
-- Levels
-- ==============================================
INSERT INTO levels (label) VALUES
('Licence 1'),
('Licence 2');

-- ==============================================
-- Semesters
-- ==============================================
INSERT INTO semesters (label, id_level) VALUES
('S1',1),
('S2',1),
('S3',2),
('S4',2);

-- ==============================================
-- Options
-- ==============================================
INSERT INTO options (label) VALUES
('Connaissances Scientifiques et Techniques de Base'),
('Développement'),
('Bases de Données & Réseaux'),
('Web & Design');

-- ==============================================
-- Academic Year Semesters
-- ==============================================
INSERT INTO academic_year_semesters (id_semester, id_option, start_date, end_date) VALUES
(1,1,'2024-09-01','2025-01-31'), -- S1 CSTB
(2,1,'2025-02-01','2025-06-30'), -- S2 CSTB
(3,1,'2025-09-01','2026-01-31'), -- S3 CSTB
(4,2,'2026-02-01','2026-06-30'), -- S4 Développement
(4,3,'2026-02-01','2026-06-30'), -- S4 BDD & Réseaux
(4,4,'2026-02-01','2026-06-30'); -- S4 Web & Design

-- ==============================================
-- Exam Sessions
-- ==============================================
INSERT INTO exam_sessions (session_date) VALUES
('2024-12-15'), -- S1
('2025-05-20'), -- S2
('2025-12-10'), -- S3
('2026-05-25'); -- S4

-- ==============================================
-- Students
-- ==============================================
INSERT INTO students (last_name, first_name, birth_date, id_program) VALUES
('Rakoto','Andry','2003-03-12',1),
('Rabe','Fanja','2003-07-25',1),
('Rasolondraibe','Hery','2003-01-10',1),
('Ramanantsoa','Mialy','2003-11-05',1),
('Randrianarisoa','Tiana','2003-06-18',1),
('Rakotovao','Jean','2003-09-22',1),
('Razafimahatratra','Lala','2003-04-30',1),
('Rajaonarivelo','Clara','2003-05-14',1),
('Rakotomanga','David','2003-02-20',1),
('Rasolo','Miora','2003-08-11',1),
('Randriamiharisoa','Niry','2003-10-01',1),
('Andrianantenaina','Soa','2003-12-19',1);

-- ==============================================
-- Subjects
-- ==============================================
INSERT INTO subjects (code, title) VALUES
-- S1 CSTB
('INF101','Programmation procédurale'),
('INF104','HTML et Introduction au Web'),
('INF107','Informatique de Base'),
('MTH101','Arithmétique et nombres'),
('MTH102','Analyse mathématique'),
('ORG101','Techniques de communication'),
-- S2 CSTB
('INF102','Bases de données relationnelles'),
('INF103','Bases de l’administration système'),
('INF105','Maintenance matériel et logiciel'),
('INF106','Compléments de programmation'),
('MTH103','Calcul Vectoriel et Matriciel'),
('MTH105','Probabilité et Statistique'),
-- S3 CSTB
('INF201','Programmation orientée objet'),
('INF202','Bases de données objets'),
('INF203','Programmation système'),
('INF208','Réseaux informatiques'),
('MTH201','Méthodes numériques'),
('ORG201','Bases de gestion'),
-- S4 Développement / BDD / Web
('INF204','Système d’information géographique'),
('INF205','Système d’information'),
('INF206','Interface Homme/Machine'),
('INF207','Eléments d’algorithmique'),
('INF210','Mini-projet de développement'),
('MTH204','Géométrie'),
('MTH205','Equations différentielles'),
('MTH206','Optimisation'),
('MTH203','MAO'),
('INF211','Mini-projet de bases de données et/ou de réseaux'),
('INF209','Web dynamique'),
('INF212','Mini-projet de Web et design'),
('MTH202','Analyse des données');

-- ==============================================
-- Credits
-- ==============================================
-- Semestre 1 (CSTB)
INSERT INTO credits (id_subject, id_option, credits) VALUES
(1,1,7),(2,1,5),(3,1,4),(4,1,4),(5,1,6),(6,1,4);
-- Semestre 2 (CSTB)
INSERT INTO credits (id_subject, id_option, credits) VALUES
(7,1,5),(8,1,5),(9,1,4),(10,1,6),(11,1,6),(12,1,4);
-- Semestre 3 (CSTB)
INSERT INTO credits (id_subject, id_option, credits) VALUES
(13,1,6),(14,1,6),(15,1,4),(16,1,6),(17,1,4),(18,1,4);
-- Semestre 4 Développement
INSERT INTO credits (id_subject, id_option, credits) VALUES
(19,2,6),(20,2,6),(21,2,6),(22,2,6),(23,2,10),(24,2,4),(25,2,4),(26,2,4),(27,2,4);
-- Semestre 4 Bases de Données & Réseaux
INSERT INTO credits (id_subject, id_option, credits) VALUES
(20,3,6), -- INF205 obligatoire
(19,3,6),(21,3,6),(22,3,6), -- 1 UE parmi INF204/INF206/INF207
(28,3,10), -- Mini-projet BDD/Réseaux
(31,3,4),(25,3,4),(26,3,4), -- 1 UE parmi MTH202/MTH205/MTH206
(27,3,4); -- MTH203 obligatoire
-- Semestre 4 Web & Design
INSERT INTO credits (id_subject, id_option, credits) VALUES
(19,4,6),(20,4,6),(21,4,6), -- 1 UE parmi INF204/INF205/INF206
(29,4,6), -- Web dynamique
(30,4,10), -- Mini-projet Web & Design
(31,4,4),(24,4,4),(26,4,4), -- 1 UE parmi MTH202/MTH204/MTH206
(27,4,4); -- MTH203 obligatoire

-- ==============================================
-- Optional Groups & Subjects
-- ==============================================
INSERT INTO subject_categories (code, label) VALUES
('OPT','Optional');

INSERT INTO optional_groups (id_subject_category, id_semester, id_option) VALUES
(1,4,2), -- Développement - UE techniques (INF204/205/206)
(1,4,2), -- Développement - UE math (MTH204/205/206)
(1,4,3), -- BDD & Réseaux - UE techniques (INF204/206/207)
(1,4,3), -- BDD & Réseaux - UE math (MTH202/205/206)
(1,4,4), -- Web & Design - UE techniques (INF204/205/206)
(1,4,4); -- Web & Design - UE math (MTH202/204/206)

INSERT INTO optional_group_subjects (id_subject, id_optional_group) VALUES
(19,1),(20,1),(21,1), -- Dév. techniques
(24,2),(25,2),(26,2), -- Dév. mathématiques
(19,3),(21,3),(22,3), -- BDD techniques (INF204/INF206/INF207)
(31,4),(25,4),(26,4), -- BDD mathématiques (MTH202/MTH205/MTH206)
(19,5),(20,5),(21,5), -- Web techniques
(31,6),(24,6),(26,6); -- Web mathématiques (MTH202/MTH204/MTH206)

INSERT INTO grades (id_student, id_academic_year_semester, id_subject, id_exam_session, grade) VALUES
(1,4,19,4,15),(1,4,20,4,16),(1,4,21,4,14),(1,4,22,4,13),(1,4,23,4,10),(1,4,24,4,14),(1,4,25,4,12),(1,4,26,4,13),(1,4,27,4,14),
(2,4,19,4,16),(2,4,20,4,15),(2,4,21,4,14),(2,4,22,4,14),(2,4,23,4,12),(2,4,24,4,15),(2,4,25,4,13),(2,4,26,4,14),(2,4,27,4,15),
(3,4,19,4,13),(3,4,20,4,14),(3,4,21,4,12),(3,4,22,4,12),(3,4,23,4,11),(3,4,24,4,12),(3,4,25,4,10),(3,4,26,4,12),(3,4,27,4,13);

-- S4 Développement (cas de test supplémentaires)
INSERT INTO grades (id_student, id_academic_year_semester, id_subject, id_exam_session, grade) VALUES
(11,4,19,4,9),(11,4,20,4,8),(11,4,21,4,7),(11,4,22,4,14),(11,4,23,4,15),(11,4,24,4,16),(11,4,25,4,14),(11,4,26,4,15),(11,4,27,4,13),
(12,4,19,4,15),(12,4,20,4,14),(12,4,21,4,13),(12,4,22,4,5),(12,4,23,4,16),(12,4,24,4,15),(12,4,25,4,14),(12,4,26,4,13),(12,4,27,4,12);

-- S4 Bases de Données & Réseaux (academic_year_semester id = 5, students 4-6)
INSERT INTO grades (id_student, id_academic_year_semester, id_subject, id_exam_session, grade) VALUES
(4,5,20,4,17),(4,5,19,4,15),(4,5,21,4,14),(4,5,22,4,13),(4,5,28,4,16),(4,5,31,4,12),(4,5,25,4,14),(4,5,26,4,11),(4,5,27,4,13),
(5,5,20,4,18),(5,5,19,4,16),(5,5,21,4,15),(5,5,22,4,14),(5,5,28,4,17),(5,5,31,4,13),(5,5,25,4,15),(5,5,26,4,12),(5,5,27,4,14),
(6,5,20,4,16),(6,5,19,4,14),(6,5,21,4,13),(6,5,22,4,12),(6,5,28,4,18),(6,5,31,4,11),(6,5,25,4,13),(6,5,26,4,10),(6,5,27,4,12);

-- S4 Web & Design (academic_year_semester id = 6, students 7-10)
INSERT INTO grades (id_student, id_academic_year_semester, id_subject, id_exam_session, grade) VALUES
(7,6,19,4,13),(7,6,20,4,12),(7,6,21,4,11),(7,6,29,4,15),(7,6,30,4,14),(7,6,31,4,10),(7,6,24,4,12),(7,6,26,4,9),(7,6,27,4,11),
(8,6,19,4,15),(8,6,20,4,14),(8,6,21,4,13),(8,6,29,4,17),(8,6,30,4,16),(8,6,31,4,12),(8,6,24,4,14),(8,6,26,4,11),(8,6,27,4,13),
(9,6,19,4,12),(9,6,20,4,11),(9,6,21,4,10),(9,6,29,4,14),(9,6,30,4,13),(9,6,31,4,9),(9,6,24,4,11),(9,6,26,4,8),(9,6,27,4,10),
(10,6,19,4,11),(10,6,20,4,10),(10,6,21,4,9),(10,6,29,4,13),(10,6,30,4,15),(10,6,31,4,8),(10,6,24,4,10),(10,6,26,4,7),(10,6,27,4,9);
-- S1 CSTB (academic_year_semester id = 1, subjects 1-6)
INSERT INTO grades (id_student, id_academic_year_semester, id_subject, id_exam_session, grade) VALUES
(1,1,1,1,15),(1,1,2,1,14),(1,1,3,1,13),(1,1,4,1,12),(1,1,5,1,11),(1,1,6,1,10),
(2,1,1,1,16),(2,1,2,1,15),(2,1,3,1,14),(2,1,4,1,13),(2,1,5,1,12),(2,1,6,1,11),
(3,1,1,1,14),(3,1,2,1,13),(3,1,3,1,12),(3,1,4,1,11),(3,1,5,1,10),(3,1,6,1,9),
(4,1,1,1,13),(4,1,2,1,12),(4,1,3,1,11),(4,1,4,1,10),(4,1,5,1,9),(4,1,6,1,8),
(5,1,1,1,17),(5,1,2,1,16),(5,1,3,1,15),(5,1,4,1,14),(5,1,5,1,13),(5,1,6,1,12),
(6,1,1,1,12),(6,1,2,1,11),(6,1,3,1,10),(6,1,4,1,9),(6,1,5,1,8),(6,1,6,1,7),
(7,1,1,1,11),(7,1,2,1,10),(7,1,3,1,9),(7,1,4,1,8),(7,1,5,1,7),(7,1,6,1,6),
(8,1,1,1,10),(8,1,2,1,9),(8,1,3,1,8),(8,1,4,1,7),(8,1,5,1,6),(8,1,6,1,5),
(9,1,1,1,9),(9,1,2,1,8),(9,1,3,1,7),(9,1,4,1,6),(9,1,5,1,5),(9,1,6,1,4),
(10,1,1,1,8),(10,1,2,1,7),(10,1,3,1,6),(10,1,4,1,5),(10,1,5,1,4),(10,1,6,1,3),
(11,1,1,1,7),(11,1,2,1,8),(11,1,3,1,9),(11,1,4,1,15),(11,1,5,1,16),(11,1,6,1,14),
(12,1,1,1,17),(12,1,2,1,16),(12,1,3,1,15),(12,1,4,1,5),(12,1,5,1,18),(12,1,6,1,17);

-- S2 CSTB (academic_year_semester id = 2, subjects 7-12)
INSERT INTO grades (id_student, id_academic_year_semester, id_subject, id_exam_session, grade) VALUES
(1,2,7,2,14),(1,2,8,2,13),(1,2,9,2,12),(1,2,10,2,15),(1,2,11,2,18),(1,2,12,2,11),
(2,2,7,2,16),(2,2,8,2,15),(2,2,9,2,14),(2,2,10,2,17),(2,2,11,2,20),(2,2,12,2,13),
(3,2,7,2,13),(3,2,8,2,12),(3,2,9,2,11),(3,2,10,2,14),(3,2,11,2,17),(3,2,12,2,10),
(4,2,7,2,12),(4,2,8,2,11),(4,2,9,2,10),(4,2,10,2,13),(4,2,11,2,16),(4,2,12,2,9),
(5,2,7,2,15),(5,2,8,2,14),(5,2,9,2,13),(5,2,10,2,18),(5,2,11,2,19),(5,2,12,2,12),
(6,2,7,2,11),(6,2,8,2,10),(6,2,9,2,9),(6,2,10,2,12),(6,2,11,2,15),(6,2,12,2,8),
(7,2,7,2,10),(7,2,8,2,9),(7,2,9,2,8),(7,2,10,2,11),(7,2,11,2,14),(7,2,12,2,7),
(8,2,7,2,9),(8,2,8,2,8),(8,2,9,2,7),(8,2,10,2,10),(8,2,11,2,13),(8,2,12,2,6),
(9,2,7,2,8),(9,2,8,2,7),(9,2,9,2,6),(9,2,10,2,9),(9,2,11,2,12),(9,2,12,2,5),
(10,2,7,2,7),(10,2,8,2,6),(10,2,9,2,5),(10,2,10,2,8),(10,2,11,2,11),(10,2,12,2,4);

-- S3 CSTB (academic_year_semester id = 3, subjects 13-18)
INSERT INTO grades (id_student, id_academic_year_semester, id_subject, id_exam_session, grade) VALUES
(1,3,13,3,15),(1,3,14,3,14),(1,3,15,3,13),(1,3,16,3,12),(1,3,17,3,11),(1,3,18,3,10),
(2,3,13,3,16),(2,3,14,3,15),(2,3,15,3,14),(2,3,16,3,13),(2,3,17,3,12),(2,3,18,3,11),
(3,3,13,3,14),(3,3,14,3,13),(3,3,15,3,12),(3,3,16,3,11),(3,3,17,3,10),(3,3,18,3,9),
(4,3,13,3,13),(4,3,14,3,12),(4,3,15,3,11),(4,3,16,3,10),(4,3,17,3,9),(4,3,18,3,8),
(5,3,13,3,17),(5,3,14,3,16),(5,3,15,3,15),(5,3,16,3,14),(5,3,17,3,13),(5,3,18,3,12),
(6,3,13,3,12),(6,3,14,3,11),(6,3,15,3,10),(6,3,16,3,9),(6,3,17,3,8),(6,3,18,3,7),
(7,3,13,3,11),(7,3,14,3,10),(7,3,15,3,9),(7,3,16,3,8),(7,3,17,3,7),(7,3,18,3,6),
(8,3,13,3,10),(8,3,14,3,9),(8,3,15,3,8),(8,3,16,3,7),(8,3,17,3,6),(8,3,18,3,5),
(9,3,13,3,9),(9,3,14,3,8),(9,3,15,3,7),(9,3,16,3,6),(9,3,17,3,5),(9,3,18,3,4),
(10,3,13,3,8),(10,3,14,3,7),(10,3,15,3,6),(10,3,16,3,5),(10,3,17,3,4),(10,3,18,3,3);

