import api from './api/api.js'

class SemesterService {
  async getSemesters() {
    try {
      const response = await api.get('/semesters')
      return response.data
    } catch (error) {
      console.error('Error fetching semesters:', error)
      throw error
    }
  }

  async getAverages(label) {
    try {
      const response = await api.get(`/semester-averages/${encodeURIComponent(label)}`)
      return response.data
    } catch (error) {
      console.error('Error fetching semester averages:', error)
      throw error
    }
  }
}

export default new SemesterService()
