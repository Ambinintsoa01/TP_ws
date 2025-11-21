import apiClient from "./api/api.js";

async function createDemandeConge(
  personnelId,
  { motif, date_demande, date_debut, date_fin, date_retour = null }
) {
  const payload = { motif, date_demande, date_debut, date_fin };
  if (date_retour) payload.date_retour = date_retour;
  const res = await apiClient.post(`/api/personnels/${personnelId}/conges`, payload);
  return res.data;
}

async function createAbsenceSpecialeDemande(
  personnelId,
  { motif, date_demande, date_debut, date_fin, date_retour = null }
) {
  const payload = { motif, date_demande, date_debut, date_fin };
  if (date_retour) payload.date_retour = date_retour;
  const res = await apiClient.post(`/api/personnels/${personnelId}/absences`, payload);
  return res.data;
}

async function createUrgenceDemande(
  personnelId,
  {
    motif,
    id_type_urgence,
    date_demande,
    date_heure_debut,
    date_heure_fin,
    date_retour = null,
    file = null,
  }
) {
  const formData = new FormData();

  formData.append("motif", motif);
  formData.append("id_type_urgence", id_type_urgence);
  formData.append("date_demande", date_demande);
  formData.append("date_heure_debut", date_heure_debut);
  formData.append("date_heure_fin", date_heure_fin);
  if (date_retour) formData.append("date_retour", date_retour);
  if (file) formData.append("justificatif", file);

  const res = await apiClient.post(`/api/personnels/${personnelId}/urgences`, formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });

  return res.data;
}
export default {
  createDemandeConge,
  createAbsenceSpecialeDemande,
  createUrgenceDemande,
  async importConges(file) {
    if (!file) throw new Error('Fichier requis');
    const formData = new FormData();
    formData.append('file', file);
    const res = await apiClient.post('/api/personnels/conges/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    return res.data;
  }
};
