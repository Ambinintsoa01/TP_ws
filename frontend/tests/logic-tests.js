// logic-tests.js
// Simple Node test harness to verify weighted-average logic used in Notes.vue and OptionS4.vue

function calculateAverage(grades, subjects) {
  if (!grades || grades.length === 0) return 0
  if (subjects && subjects.length > 0) {
    const creditsMap = new Map()
    for (const s of subjects) creditsMap.set(s.subjectId, s.credits || 0)

    let numer = 0
    let denom = 0
    for (const s of subjects) {
      const cred = creditsMap.get(s.subjectId) || 0
      denom += cred
      const g = grades.find(g => g.subjectId === s.subjectId)
      const val = g ? (g.grade != null ? Number(g.grade) : (g.bestGrade != null ? Number(g.bestGrade) : 0)) : 0
      numer += (val || 0) * cred
    }
    if (denom === 0) return 0
    return Math.round((numer / denom) * 100) / 100
  }
  // fallback
  const sum = grades.reduce((acc, grade) => {
    const val = grade && (grade.grade != null ? grade.grade : (grade.bestGrade != null ? grade.bestGrade : 0))
    return acc + Number(val || 0)
  }, 0)
  return Math.round((sum / grades.length) * 100) / 100
}

function computeOptionAverages(subjects, students) {
  // subjects: [{subjectId, credits}]
  // students: [{studentId, grades:[{subjectId, grade or bestGrade}]}]
  const creditsMap = new Map();
  for (const s of subjects) creditsMap.set(s.subjectId, s.credits || 0)

  // per-subject avg treating missing as 0
  const subjAgg = new Map()
  for (const s of subjects) subjAgg.set(s.subjectId, { sum: 0, count: students.length })

  let totalNumer = 0
  let totalDenom = 0
  for (const stu of students) {
    let numer = 0
    let denom = 0
    for (const sub of subjects) {
      const sid = sub.subjectId
      const cred = creditsMap.get(sid) || 0
      denom += cred
      const gobj = (stu.grades || []).find(x => x.subjectId === sid)
      const g = gobj && (gobj.bestGrade != null ? gobj.bestGrade : (gobj.grade != null ? gobj.grade : null))
      const val = (g != null) ? Number(g) : 0
      numer += val * cred
      const agg = subjAgg.get(sid)
      if (agg) agg.sum += val
    }
    if (denom > 0) {
      totalNumer += numer
      totalDenom += denom
    }
  }

  const subjectAverages = []
  for (const s of subjects) {
    const agg = subjAgg.get(s.subjectId)
    const avg = agg && agg.count > 0 ? Math.round((agg.sum / agg.count) * 100) / 100 : null
    subjectAverages.push({ subjectId: s.subjectId, average: avg })
  }

  const optionAverage = (totalDenom > 0) ? Math.round((totalNumer / totalDenom) * 100) / 100 : null
  return { subjectAverages, optionAverage }
}

// Tests
console.log('--- Test 1: simple weighted average with missing grade treated as 0')
const subjects1 = [{ subjectId: 1, credits: 3 }, { subjectId: 2, credits: 2 }]
const grades1 = [{ subjectId: 1, grade: 10 }]
const avg1 = calculateAverage(grades1, subjects1)
console.log('Expected (10*3+0*2)/(3+2)=6.00 ->', avg1)

console.log('\n--- Test 2: multiple students option average')
const students2 = [
  { studentId: 1, grades: [{ subjectId: 1, grade: 15 }, { subjectId: 2, grade: 12 }] },
  { studentId: 2, grades: [{ subjectId: 1, grade: 14 }] }, // missing subj2 -> 0
  { studentId: 3, grades: [{ subjectId: 1, grade: 13 }, { subjectId: 2, grade: 10 }] }
]
const subjects2 = [{ subjectId: 1, credits: 6 }, { subjectId: 2, credits: 4 }]
const res2 = computeOptionAverages(subjects2, students2)
console.log('Subject averages:', res2.subjectAverages)
console.log('Option average (weighted):', res2.optionAverage)

console.log('\n--- Test 3: optional group best grade handling (simulate bestGrade on representative only)')
const subjects3 = [{ subjectId: 19, credits: 6 }, { subjectId: 20, credits: 6 }, { subjectId: 21, credits: 6 }]
// Suppose subjects 19-21 form an optional group; server picks representative column 19 with bestGrade
const students3 = [
  { studentId: 1, grades: [{ subjectId: 19, bestGrade: 15 }] },
  { studentId: 2, grades: [{ subjectId: 19, bestGrade: 12 }] },
  { studentId: 3, grades: [{ subjectId: 19, bestGrade: 14 }] }
]
const res3 = computeOptionAverages(subjects3, students3)
console.log('Subject averages (opt group displayed on rep col):', res3.subjectAverages)
console.log('Option average:', res3.optionAverage)

console.log('\nAll tests done.')
