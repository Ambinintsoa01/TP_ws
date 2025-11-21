import apiClient from "./api/api.js";

async function createHeureSupp(personnelId, { motif, date_heure_debut, date_heure_fin }) {
  if (!personnelId) throw new Error("personnelId requis");
  const payload = { motif, date_heure_debut, date_heure_fin };
  const res = await apiClient.post(`/api/personnels/${personnelId}/heures-supp`, payload);
  return res.data;
}

async function getAllHeuresSupp() {
  const res = await apiClient.get(`/api/personnels/heures-supp`);
  return res.data;
}

async function getDetailsHeuresSupp(personnelId) {
  const res = await apiClient.get(`/api/personnels/${personnelId}/heures-supp-details`);
  return res.data;
}

async function importHeuresSupp(file) {
  if (!file) throw new Error('Fichier requis');
  const formData = new FormData();
  formData.append('file', file);
  const res = await apiClient.post('/api/personnels/heures-supp/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
  return res.data;
}

export default {
  createHeureSupp,
  getAllHeuresSupp,
  getDetailsHeuresSupp,
  importHeuresSupp,
};
