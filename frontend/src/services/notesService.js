import api from './api/api.js'

class NotesService {
  async getStudentNotesBySemester(studentId, semester) {
    try {
      const response = await api.get(`/etudiants/${studentId}/notes/${semester}`)
      return response.data
    } catch (error) {
      console.error('Error fetching student notes by semester:', error)
      throw error
    }
  }

  async getStudentNotesByLevel(studentId, level) {
    try {
      const response = await api.get(`/etudiants/${studentId}/notes/${level}`)
      return response.data
    } catch (error) {
      console.error('Error fetching student notes by level:', error)
      throw error
    }
  }

  async getAllStudentNotes(semester = '', level = '') {
    try {
      // This would be a new endpoint to get all students' notes
      // For now, we'll simulate with mock data
      const mockData = [
        {
          studentId: 1,
          studentName: 'Alice Dupont',
          registrationNumber: 'ETU001',
          program: 'Informatique',
          grades: [
            {
              gradeId: 1,
              subjectCode: 'INF101',
              subjectTitle: 'Algorithmique',
              semesterLabel: 'S1',
              grade: 15.5,
              sessionDate: '2024-01-15'
            },
            {
              gradeId: 2,
              subjectCode: 'INF102',
              subjectTitle: 'Programmation',
              semesterLabel: 'S1',
              grade: 17.0,
              sessionDate: '2024-01-20'
            }
          ]
        },
        {
          studentId: 2,
          studentName: 'Bob Martin',
          registrationNumber: 'ETU002',
          program: 'Informatique',
          grades: [
            {
              gradeId: 3,
              subjectCode: 'INF101',
              subjectTitle: 'Algorithmique',
              semesterLabel: 'S1',
              grade: 14.0,
              sessionDate: '2024-01-15'
            },
            {
              gradeId: 4,
              subjectCode: 'INF102',
              subjectTitle: 'Programmation',
              semesterLabel: 'S1',
              grade: 16.5,
              sessionDate: '2024-01-20'
            }
          ]
        }
      ]

      // Filter by semester or level if provided
      if (semester) {
        mockData.forEach(student => {
          student.grades = student.grades.filter(grade => grade.semesterLabel === semester)
        })
      }

      return mockData.filter(student => student.grades.length > 0)
    } catch (error) {
      console.error('Error fetching all student notes:', error)
      throw error
    }
  }

  async getStudentNotesSummary(studentId) {
    try {
      // This would aggregate notes by semester/level
      const response = await api.get(`/etudiants/${studentId}/notes/summary`)
      return response.data
    } catch (error) {
      console.error('Error fetching student notes summary:', error)
      throw error
    }
  }
}

export default new NotesService()