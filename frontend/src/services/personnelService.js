import apiClient from './api/api.js'

export default {
  async getPersonnels() {
    const res = await apiClient.get('/api/personnels')
    return res.data
  }
}
