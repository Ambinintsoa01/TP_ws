import api from './api/api.js'

class StudentService {
  async getStudents() {
    try {
      // In a real app, call the backend API, e.g.:
      // const response = await api.get('/etudiants')
      // return response.data

      // For now return mock data to allow the frontend build to succeed
      return [
        {
          id: 1,
          firstName: 'Alice',
          lastName: 'Dupont',
          registrationNumber: 'ETU001',
          program: { label: 'Informatique' },
          level: { label: 'L1' }
        },
        {
          id: 2,
          firstName: 'Bob',
          lastName: 'Martin',
          registrationNumber: 'ETU002',
          program: { label: 'Mathématiques' },
          level: { label: 'L2' }
        }
      ]
    } catch (error) {
      console.error('Error fetching students:', error)
      throw error
    }
  }
}

export default new StudentService()
