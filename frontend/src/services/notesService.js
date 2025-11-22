import api from './api/api.js'
import semesterService from './semesterService'

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
      // load mapping of semesters to ids
      const semsResp = await semesterService.getSemesters()
      const sems = semsResp && semsResp.data ? semsResp.data : semsResp

      let targetLabels = []
      // If a level is provided, derive semesters from the semesters list returned by the backend
      if (level) {
        // normalize level code (L1 -> Licence 1)
        const normalized = (level || '').toUpperCase()
        let levelLabel = normalized
        if (normalized === 'L1') levelLabel = 'LICENCE 1'
        else if (normalized === 'L2') levelLabel = 'LICENCE 2'
        else if (normalized === 'L3') levelLabel = 'LICENCE 3'

        // find semesters whose level label contains the levelLabel
        const found = (sems || []).filter(s => s.level && String(s.level).toUpperCase().includes(levelLabel))
        targetLabels = found.map(s => s.label)
      } else if (semester) {
        targetLabels = [semester]
      } else {
        // if no filter provided, return empty list to avoid heavy queries
        return []
      }

      // find semester ids for the requested labels
      function matchesLabel(slabel, lbl) {
        if (!slabel || !lbl) return false
        const a = String(slabel).toUpperCase()
        const b = String(lbl).toUpperCase()
        if (a === b) return true
        if (a.includes(b) || b.includes(a)) return true
        const da = (a.match(/\d+/) || [])[0]
        const db = (b.match(/\d+/) || [])[0]
        if (da && db && da === db) return true
        return false
      }

      const targetIds = targetLabels.map(lbl => {
        const found = (sems || []).find(s => matchesLabel(s.label, lbl))
        return found ? found.id : null
      }).filter(id => id != null)

      // for each semester id, call the backend structured endpoint that returns subjects, students, averages
      const subjMap = new Map()
      const studentMap = new Map()
      const optionAgg = new Map() // optionLabel -> { sum, count }

      for (const id of targetIds) {
        try {
          const resp = await api.get(`/grades/semester/${id}/subjects/grades`)
          const payload = resp.data && resp.data.data ? resp.data.data : resp.data
          if (!payload) continue

          const semObj = (sems || []).find(s => s.id === id)
          const semLabel = semObj ? semObj.label : ''

          // merge subjects
          if (Array.isArray(payload.subjects)) {
            for (const s of payload.subjects) {
              const sid = s.subjectId
              const existing = subjMap.get(sid) || { subjectId: s.subjectId, subjectCode: s.subjectCode, subjectTitle: s.subjectTitle, credits: s.credits || null, optionId: s.optionId || null, optionLabel: s.optionLabel || null, optionalInSemester: !!s.optionalInSemester, optionalGroupId: s.optionalGroupId || null }
              // if this semester is S4 mark optional flag
              subjMap.set(sid, { ...existing, optionalInSemester: existing.optionalInSemester || !!s.optionalInSemester, optionalGroupId: existing.optionalGroupId || s.optionalGroupId })
            }
          }

          // merge students and their grades
          if (Array.isArray(payload.students)) {
            for (const st of payload.students) {
              const sid = st.studentId || `${st.lastName}_${st.firstName}`
              const entry = studentMap.get(sid) || { studentId: st.studentId, studentName: `${st.firstName || ''} ${st.lastName || ''}`.trim(), registrationNumber: '', program: st.program || '', grades: [] }
              if (Array.isArray(st.grades)) {
                for (const g of st.grades) {
                  entry.grades.push({
                    subjectId: g.subjectId,
                    subjectCode: g.subjectCode,
                    subjectTitle: g.subjectTitle,
                    grade: g.grade,
                    semesterLabel: semLabel,
                    optionalGroupId: g.optionalGroupId || null,
                    bestOptionalGrade: g.bestOptionalGrade != null ? g.bestOptionalGrade : null,
                    rawGrade: g.rawGrade != null ? g.rawGrade : null
                  })
                }
              }
              studentMap.set(sid, entry)
            }
          }

          // merge option averages if present
          if (Array.isArray(payload.optionAverages)) {
            for (const opt of payload.optionAverages) {
              const key = opt.optionLabel || opt.optionId || 'UNKNOWN'
              const cur = optionAgg.get(key) || { sum: 0, count: 0 }
              if (opt.average != null && opt.count) {
                cur.sum += Number(opt.average) * Number(opt.count)
                cur.count += Number(opt.count)
              }
              optionAgg.set(key, cur)
            }
          }
        } catch (e) {
          console.error('Failed to fetch semester structured grades for', id, e)
        }
      }

      const subjects = Array.from(subjMap.values())

      const optionAverages = Array.from(optionAgg.entries()).map(([k, v]) => ({ optionLabel: k, average: v.count ? Math.round((v.sum / v.count) * 100) / 100 : null, count: v.count }))

      return { students: Array.from(studentMap.values()), optionAverages, subjects }
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

  async getOptionSubjects(semesterId, optionId) {
    try {
      const resp = await api.get(`/grades/semester/${semesterId}/option/${optionId}/subjects`)
      return resp.data && resp.data.data ? resp.data.data : resp.data
    } catch (error) {
      console.error('Error fetching option subjects:', error)
      throw error
    }
  }

  async getOptions() {
    try {
      const resp = await api.get('/grades/options')
      return resp.data && resp.data.data ? resp.data.data : resp.data
    } catch (error) {
      console.error('Error fetching options:', error)
      throw error
    }
  }

  async getSubjectsCreditsBySemester(semesterId) {
    try {
      const resp = await api.get(`/grades/subjects/credits/semester/${semesterId}`)
      return resp.data && resp.data.data ? resp.data.data : resp.data
    } catch (error) {
      console.error('Error fetching semester credits:', error)
      throw error
    }
  }
}

export default new NotesService()