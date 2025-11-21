1️⃣ Fonctionnalité: Notes d'un étudiant sur semestre S1 (ok)
Requête SQL (ok)

    Écrire la requête pour récupérer les notes d'un étudiant pour le semestre S1 (ok)

    Joindre les tables: etudiants, note, semestres_annee, semestres (ok)

    Filtrer par libellé "S1" dans la table semestres (ok)

Implémentation Backend (ok)

    Créer l'endpoint API: /api/etudiants/{id}/notes/s1 (ok)

    Gérer les paramètres: id_etudiant (ok)

    Valider l'existence de l'étudiant (ok)

    Gérer les erreurs (étudiant non trouvé, pas de notes, etc.) (ok)

Tests (ok)

    Tester avec un étudiant existant ayant des notes (ok)

    Tester avec un étudiant sans notes (ok)

    Tester avec un étudiant inexistant (ok)

2️⃣ Fonctionnalité: Notes d'un étudiant sur année L1 (ok)
Requête SQL (ok)

    Écrire la requête pour récupérer les notes d'un étudiant pour l'année L1 (ok)

    Joindre les tables: etudiants, note, semestres_annee, semestres, niveau (ok)

    Filtrer par libellé "L1" dans la table niveau (ok)

    Grouper par semestre (ok)

Implémentation Backend (ok)

    Créer l'endpoint API: /api/etudiants/{id}/notes/l1 (ok)

    Gérer les paramètres: id_etudiant (ok)

    Valider l'existence de l'étudiant (ok)

    Structurer la réponse par semestre (ok)
    
