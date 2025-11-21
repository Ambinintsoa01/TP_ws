// src/services/personnelAbsenceService.js
import apiClient from "./api/api.js";

async function getHeuresAbsence(personnelId, { mois = null, annee = null } = {}) {
  const params = {};
  if (mois !== null) params.mois = mois;
  if (annee !== null) params.annee = annee;

  const res = await apiClient.get(`/api/personnels/${personnelId}/absences`, { params });
  return res.data;
}

async function getHeuresRetard(personnelId, { mois = null, annee = null } = {}) {
  const params = {};
  if (mois !== null) params.mois = mois;
  if (annee !== null) params.annee = annee;

  const res = await apiClient.get(`/api/personnels/${personnelId}/retards`, { params });
  return res.data;
}

async function getStatistiques(personnelId, { mois = null, annee = null } = {}) {
  const params = {};
  if (mois !== null) params.mois = mois;
  if (annee !== null) params.annee = annee;

  const res = await apiClient.get(`/api/personnels/${personnelId}/stats`, { params });
  return res.data;
}

async function getStatsGlobaux({ mois = null, annee = null } = {}) {
  const params = {};
  if (mois !== null) params.mois = mois;
  if (annee !== null) params.annee = annee;

  const res = await apiClient.get("/api/personnels/stats", { params });
  return res.data;
}

async function getDetailsAbsencesRetards(personnelId, { mois = null, annee = null } = {}) {
  const params = {};
  if (mois !== null) params.mois = mois;
  if (annee !== null) params.annee = annee;

  const res = await apiClient.get(`/api/personnels/${personnelId}/absences-details`, { params });
  return res.data;
}
export default {
  getHeuresAbsence,
  getHeuresRetard,
  getStatistiques,
  getStatsGlobaux,
  getDetailsAbsencesRetards,
};
