import apiClient from './api/api.js'

async function fetchUrgenceTypes() {
  const res = await apiClient.get('/api/urgences/types')
  return res.data
}

export default {
  fetchUrgenceTypes,
}
