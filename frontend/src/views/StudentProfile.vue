<template>
  <div class="p-6">
    <button class="mb-4 text-sm text-gray-600 hover:underline" @click="$router.back()">← Retour</button>
    
    <div v-if="loading" class="text-center py-8">Chargement des informations de l'étudiant...</div>
    
    <div v-else-if="error" class="p-4 bg-red-100 text-red-800 rounded">{{ error }}</div>
    
    <div v-else>
      <!-- En-tête avec informations personnelles -->
      <div class="mb-6 p-6 bg-white rounded border border-gray-100 shadow-sm">
        <h1 class="text-3xl font-bold mb-4 text-gray-900">{{ fullName }}</h1>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 text-sm">
          <div>
            <span class="font-semibold text-gray-600">ID Étudiant:</span>
            <span class="ml-2 text-gray-900">{{ studentId }}</span>
          </div>
          <div>
            <span class="font-semibold text-gray-600">Nom:</span>
            <span class="ml-2 text-gray-900">{{ studentInfo.lastName }}</span>
          </div>
          <div>
            <span class="font-semibold text-gray-600">Prénom:</span>
            <span class="ml-2 text-gray-900">{{ studentInfo.firstName }}</span>
          </div>
          <div>
            <span class="font-semibold text-gray-600">Date de naissance:</span>
            <span class="ml-2 text-gray-900">{{ formatDate(studentInfo.birthDate) }}</span>
          </div>
          <div>
            <span class="font-semibold text-gray-600">Programme:</span>
            <span class="ml-2 text-gray-900">{{ studentInfo.program || '—' }}</span>
          </div>
        </div>
      </div>

      <!-- Navigation rapide -->
      <div class="mb-6 p-4 bg-indigo-50 rounded border border-indigo-100">
        <h2 class="text-lg font-semibold mb-3 text-indigo-900">Navigation rapide</h2>
        <div class="flex flex-wrap gap-2">
          <button 
            @click="$router.push({ name: 'student-notes-level', params: { id: studentId, level: 'L1' }, query: { name: fullName } })"
            class="px-4 py-2 text-sm rounded bg-green-100 text-green-800 hover:bg-green-200 font-medium"
          >
            📚 Licence 1 (S1 + S2)
          </button>
          <button 
            @click="$router.push({ name: 'student-notes-level', params: { id: studentId, level: 'L2' }, query: { name: fullName } })"
            class="px-4 py-2 text-sm rounded bg-yellow-100 text-yellow-800 hover:bg-yellow-200 font-medium"
          >
            📚 Licence 2 (S3 + S4)
          </button>
          <button 
            v-for="sem in ['S1', 'S2', 'S3', 'S4']"
            :key="sem"
            @click="$router.push({ name: 'student-notes-semester', params: { id: studentId, semester: sem }, query: { name: fullName } })"
            class="px-4 py-2 text-sm rounded bg-blue-100 text-blue-800 hover:bg-blue-200 font-medium"
          >
            {{ sem }}
          </button>
        </div>
      </div>

      <!-- Sélection d'option pour S4 -->
      <div v-if="s4Options.length > 0" class="mb-6 p-4 bg-blue-50 rounded border border-blue-200">
        <h2 class="text-lg font-semibold mb-3 text-blue-900">Option S4</h2>
        <div class="flex items-center gap-4">
          <label class="text-sm font-medium text-gray-700">Sélectionnez une option:</label>
          <select 
            v-model="selectedS4OptionId" 
            @change="recalculateS4Credits"
            class="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          >
            <option value="auto">Auto (Meilleure option)</option>
            <option v-for="opt in s4Options" :key="opt.id" :value="opt.id">
              {{ opt.label }}
            </option>
          </select>
        </div>
      </div>

      <!-- Résumé des notes par semestre -->
      <div class="mb-6 p-6 bg-white rounded border border-gray-100 shadow-sm">
        <h2 class="text-2xl font-bold mb-4 text-gray-900">Résumé des notes</h2>
        
        <div v-if="loadingSummary" class="text-sm text-gray-500">Chargement du résumé...</div>
        
        <div v-else-if="semesterSummaries.length === 0" class="text-sm text-gray-500">
          Aucune note disponible pour cet étudiant.
        </div>
        
        <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div 
            v-for="summary in semesterSummaries" 
            :key="summary.semester"
            class="p-4 border border-gray-200 rounded hover:border-blue-300 transition-colors cursor-pointer"
            @click="$router.push({ name: 'student-notes-semester', params: { id: studentId, semester: summary.semester }, query: { name: fullName } })"
          >
            <div class="flex justify-between items-start mb-3">
              <h3 class="text-lg font-semibold text-gray-900">{{ summary.semester }}</h3>
              <span 
                class="px-2 py-1 text-xs rounded font-medium"
                :class="summary.decision === 'Admis' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'"
              >
                {{ summary.decision || '—' }}
              </span>
            </div>
            
            <div class="space-y-2 text-sm">
              <div class="flex justify-between">
                <span class="text-gray-600">Moyenne:</span>
                <span class="font-semibold text-gray-900">{{ formatGrade(summary.average) }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">Crédits capitalisés:</span>
                <span class="font-semibold text-gray-900">{{ summary.credits || '—' }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-600">Nombre de matières:</span>
                <span class="font-semibold text-gray-900">{{ summary.subjectCount || 0 }}</span>
              </div>
              <div v-if="summary.optionLabel" class="flex justify-between">
                <span class="text-gray-600">Option:</span>
                <span class="font-semibold text-indigo-700">{{ summary.optionLabel }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Statistiques globales -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div class="p-4 bg-white rounded border border-gray-100 shadow-sm">
          <h3 class="text-sm font-semibold text-gray-600 mb-2">Moyenne générale</h3>
          <p class="text-3xl font-bold text-blue-600">{{ formatGrade(overallAverage) }}</p>
        </div>
        <div class="p-4 bg-white rounded border border-gray-100 shadow-sm">
          <h3 class="text-sm font-semibold text-gray-600 mb-2">Crédits totaux</h3>
          <p class="text-3xl font-bold text-green-600">{{ totalCredits }}</p>
        </div>
        <div class="p-4 bg-white rounded border border-gray-100 shadow-sm">
          <h3 class="text-sm font-semibold text-gray-600 mb-2">Semestres validés</h3>
          <p class="text-3xl font-bold text-purple-600">{{ validatedSemesters }} / {{ semesterSummaries.length }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../services/api/api.js'
import notesService from '../services/notesService'

const route = useRoute()
const studentId = Number(route.params.id || route.query.id)

const loading = ref(true)
const loadingSummary = ref(true)
const error = ref(null)
const studentInfo = ref({})
const semesterSummaries = ref([])
const s4Options = ref([])
const selectedS4OptionId = ref('auto')
const s4NotesData = ref(null)

const fullName = computed(() => {
  if (!studentInfo.value.firstName && !studentInfo.value.lastName) return `Étudiant ${studentId}`
  return `${studentInfo.value.firstName || ''} ${studentInfo.value.lastName || ''}`.trim()
})

const overallAverage = computed(() => {
  if (semesterSummaries.value.length === 0) return 0
  const validAverages = semesterSummaries.value
    .map(s => s.average)
    .filter(avg => avg !== null && avg !== undefined && !isNaN(avg))
  if (validAverages.length === 0) return 0
  const sum = validAverages.reduce((acc, avg) => acc + avg, 0)
  return sum / validAverages.length
})

const totalCredits = computed(() => {
  return semesterSummaries.value.reduce((acc, s) => {
    const credits = parseInt(s.credits) || 0
    return acc + credits
  }, 0)
})

const validatedSemesters = computed(() => {
  return semesterSummaries.value.filter(s => s.decision === 'Admis').length
})

function formatDate(dateString) {
  if (!dateString) return '—'
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('fr-FR', { year: 'numeric', month: 'long', day: 'numeric' })
  } catch {
    return dateString
  }
}

function formatGrade(grade) {
  if (grade === null || grade === undefined || isNaN(grade)) return '—'
  return Number(grade).toFixed(2)
}

async function loadStudentInfo() {
  try {
    const response = await api.get(`/students/${studentId}`)
    const data = response.data?.data || response.data
    studentInfo.value = data
  } catch (err) {
    console.error('Error loading student info:', err)
    error.value = 'Erreur lors du chargement des informations de l\'étudiant'
  } finally {
    loading.value = false
  }
}

async function loadSemesterSummaries() {
  try {
    loadingSummary.value = true
    
    // Map semester labels to IDs
    const semesterIdMap = { 'S1': 1, 'S2': 2, 'S3': 3, 'S4': 4 }
    
    // Charger d'abord les crédits pour tous les semestres
    const creditsCache = new Map()
    const semesters = ['S1', 'S2', 'S3', 'S4']
    
    // Charger les métadonnées de crédits pour chaque semestre
    for (const semester of semesters) {
      try {
        const semesterId = semesterIdMap[semester]
        const creditsResponse = await api.get(`/grades/subjects/credits/semester/${semesterId}`)
        const creditsData = creditsResponse.data?.data || creditsResponse.data
        
        const creditsMap = new Map()
        if (Array.isArray(creditsData)) {
          creditsData.forEach(item => {
            const subjectId = item.subjectId || item.id
            const credits = item.credits || item.credit || 1
            if (subjectId) {
              creditsMap.set(`id:${Number(subjectId)}`, credits)
              creditsMap.set(`id:${String(subjectId)}`, credits)
            }
            if (item.subjectCode) {
              const normalizedCode = String(item.subjectCode).trim().toUpperCase()
              creditsMap.set(`code:${normalizedCode}`, credits)
            }
          })
        }
        creditsCache.set(semester, creditsMap)
      } catch (err) {
        console.log(`No credits for ${semester}:`, err)
        creditsCache.set(semester, new Map())
      }
    }
    
    const summaries = []
    
    // Charger les options S4 pour le calcul spécial
    if (semesters.includes('S4')) {
      try {
        const allOptions = await notesService.getOptions()
        s4Options.value = (allOptions || []).filter(opt => opt.id !== 1).map(opt => ({
          id: opt.id || opt.optionId,
          label: opt.label || opt.optionLabel
        }))
      } catch (err) {
        console.log('No S4 options:', err)
      }
    }
    
    for (const semester of semesters) {
      try {
        const response = await api.get(`/students/${studentId}/notes/semesters/${semester}`)
        const data = response.data?.data || response.data
        
        if (data && data.notes && data.notes.length > 0) {
          const creditsMap = creditsCache.get(semester) || new Map()
          
          // Fonction pour résoudre les crédits
          const resolveCredits = (note) => {
            const subjectId = note.subjectId || note.id
            const subjectCode = note.subjectCode
            
            // Essayer avec l'ID
            if (subjectId) {
              let credits = creditsMap.get(`id:${Number(subjectId)}`)
              if (credits !== undefined) return credits
              credits = creditsMap.get(`id:${String(subjectId)}`)
              if (credits !== undefined) return credits
            }
            
            // Essayer avec le code
            if (subjectCode) {
              const normalizedCode = String(subjectCode).trim().toUpperCase()
              const credits = creditsMap.get(`code:${normalizedCode}`)
              if (credits !== undefined) return credits
            }
            
            return 1 // Par défaut
          }
          
          let creditsTotal = 0
          let average = 0
          let optionLabel = null
          
          // Pour S4, calculer avec les options comme dans les autres vues
          if (semester === 'S4' && s4Options.value.length > 0) {
            // Sauvegarder les données S4 pour recalcul ultérieur
            s4NotesData.value = data
            let bestOption = null
            let bestAverage = -1
            let bestCredits = 0
            
            // Filtrer les options selon la sélection
            const optionsToCheck = selectedS4OptionId.value === 'auto' 
              ? s4Options.value 
              : s4Options.value.filter(opt => opt.id === Number(selectedS4OptionId.value))
            
            for (const option of optionsToCheck) {
              try {
                const payload = await notesService.getOptionSubjects(4, option.id)
                const optSubjects = Array.isArray(payload) ? payload : (payload.subjects || [])
                
                // Construire la map des crédits pour cette option
                const optCreditsMap = new Map()
                optSubjects.forEach(sub => {
                  const subId = sub.subjectId || sub.id
                  const creds = sub.credits || 1
                  if (subId) {
                    optCreditsMap.set(`id:${Number(subId)}`, creds)
                    optCreditsMap.set(`id:${String(subId)}`, creds)
                  }
                })
                
                console.log(`[Credits Map for option ${option.label}]:`, {
                  optCreditsMapEntries: Array.from(optCreditsMap.entries()),
                  optSubjectsWithCredits: optSubjects.map(s => ({
                    id: s.subjectId || s.id,
                    code: s.subjectCode,
                    credits: s.credits
                  }))
                })
                
                // Calculer moyenne et crédits comme dans SemesterAverages
                const notesBySubject = new Map()
                data.notes.forEach(note => {
                  const subId = note.subjectId || note.id
                  if (subId) notesBySubject.set(Number(subId), note)
                })
                
                // Calculer moyenne et crédits avec computeDecisionSummary
                const entries = []
                
                // Grouper les matières optionnelles par groupe
                const optionalGroups = new Map()
                const requiredSubjects = []
                
                optSubjects.forEach(sub => {
                  if (sub.optionalGroupId) {
                    if (!optionalGroups.has(sub.optionalGroupId)) {
                      optionalGroups.set(sub.optionalGroupId, [])
                    }
                    optionalGroups.get(sub.optionalGroupId).push(sub)
                  } else {
                    requiredSubjects.push(sub)
                  }
                })
                
                // Ajouter TOUTES les matières obligatoires (même celles sans notes = 0)
                requiredSubjects.forEach(sub => {
                  const subId = Number(sub.subjectId || sub.id)
                  const note = notesBySubject.get(subId)
                  // Si pas de note, utiliser 0 comme dans SemesterAverages (ensureSubjectsCoverage)
                  const gradeVal = note?.grade
                  const rawGrade = (gradeVal != null && !Number.isNaN(gradeVal)) ? gradeVal : 0
                  entries.push({
                    subjectId: subId,
                    rawGrade: rawGrade,
                    __credits: optCreditsMap.get(`id:${subId}`) || 1
                  })
                })
                
                // Ajouter la meilleure note de chaque groupe optionnel
                optionalGroups.forEach(groupSubjects => {
                  let bestGrade = null
                  let bestSubject = null
                  
                  groupSubjects.forEach(sub => {
                    const subId = Number(sub.subjectId || sub.id)
                    const note = notesBySubject.get(subId)
                    const gradeVal = note?.grade
                    const effectiveGrade = (gradeVal != null && !Number.isNaN(gradeVal)) ? gradeVal : 0
                    if (bestGrade == null || effectiveGrade > bestGrade) {
                      bestGrade = effectiveGrade
                      bestSubject = sub
                    }
                  })
                  
                  if (bestSubject) {
                    const subId = Number(bestSubject.subjectId || bestSubject.id)
                    entries.push({
                      subjectId: subId,
                      rawGrade: bestGrade,
                      __credits: optCreditsMap.get(`id:${subId}`) || 1
                    })
                  }
                })
                
                // Utiliser computeDecisionSummary comme SemesterAverages
                const summary = computeDecisionSummary(entries, optCreditsMap)
                const optAvg = summary?.average ?? 0
                
                // Calculer les crédits capitalisés selon les règles d'admission
                // Compter les crédits des matières dans entries si:
                // - note >= 10, OU
                // - matière compensée (dans summary.compensated)
                let capitalizedCredits = 0
                const compensatedSet = summary?.compensated || new Set()
                entries.forEach(entry => {
                  const gradeVal = entry.rawGrade
                  const effectiveGrade = (gradeVal != null && !Number.isNaN(gradeVal)) ? gradeVal : 0
                  const isCompensated = compensatedSet.has(entry.subjectId)
                  
                  if (effectiveGrade >= 10 || isCompensated) {
                    capitalizedCredits += entry.__credits
                  }
                })
                
                console.log(`[StudentProfile S4] Option ${option.label}:`, {
                  entries,
                  summary,
                  optAvg,
                  capitalizedCredits,
                  compensatedSet: Array.from(compensatedSet),
                  optSubjects: optSubjects.length,
                  requiredSubjects: requiredSubjects.length,
                  optionalGroups: optionalGroups.size,
                  notesBySubject: Array.from(notesBySubject.entries())
                })
                
                if (optAvg > bestAverage) {
                  bestAverage = optAvg
                  bestOption = option
                  bestCredits = capitalizedCredits
                }
              } catch (err) {
                console.log(`Error loading option ${option.id}:`, err)
              }
            }
            
            average = bestAverage
            creditsTotal = bestCredits
            optionLabel = bestOption ? bestOption.label : null
            
            console.log(`[StudentProfile S4 FINAL]`, {
              bestOption: bestOption?.label,
              bestAverage,
              bestCredits,
              semester: 'S4'
            })
          } else {
            // Pour S1, S2, S3 : calcul normal avec la même logique
            let totalWeighted = 0
            let totalCredits = 0
            data.notes.forEach(note => {
              const gradeVal = note.grade
              const effectiveGrade = (gradeVal != null && !Number.isNaN(gradeVal)) ? gradeVal : 0
              const credits = resolveCredits(note)
              totalWeighted += effectiveGrade * credits
              totalCredits += credits
              if (effectiveGrade >= 10) {
                creditsTotal += credits
              }
            })
            average = totalCredits > 0 ? Math.round((totalWeighted / totalCredits) * 100) / 100 : 0
          }
          
          // Déterminer la décision selon les mêmes règles que StudentGrades
          const gradesBelow6 = data.notes.filter(n => {
            const g = n.grade
            const eff = (g != null && !Number.isNaN(g)) ? g : 0
            return eff < 6
          }).length
          const gradesBelow10 = data.notes.filter(n => {
            const g = n.grade
            const eff = (g != null && !Number.isNaN(g)) ? g : 0
            return eff < 10
          }).length
          let decision = 'Admis'
          if (average < 10 || gradesBelow6 > 0 || gradesBelow10 >= 3) {
            decision = 'Ajourné'
          }
          
          summaries.push({
            semester,
            average,
            credits: creditsTotal,
            decision,
            subjectCount: data.notes.length,
            optionLabel: optionLabel
          })
        }
      } catch (err) {
        console.log(`No data for ${semester}:`, err)
      }
    }
    
    semesterSummaries.value = summaries
  } catch (err) {
    console.error('Error loading semester summaries:', err)
  } finally {
    loadingSummary.value = false
  }
}

async function recalculateS4Credits() {
  if (!s4NotesData.value || s4Options.value.length === 0) return
  
  try {
    const data = s4NotesData.value
    
    // Filtrer les options selon la sélection
    const optionsToCheck = selectedS4OptionId.value === 'auto' 
      ? s4Options.value 
      : s4Options.value.filter(opt => opt.id === Number(selectedS4OptionId.value))
    
    let bestOption = null
    let bestAverage = -1
    let bestCredits = 0
    
    for (const option of optionsToCheck) {
      try {
        const payload = await notesService.getOptionSubjects(4, option.id)
        const optSubjects = Array.isArray(payload) ? payload : (payload.subjects || [])
        
        // Construire la map des crédits pour cette option
        const optCreditsMap = new Map()
        optSubjects.forEach(sub => {
          const subId = sub.subjectId || sub.id
          const creds = sub.credits || 1
          if (subId) {
            optCreditsMap.set(`id:${Number(subId)}`, creds)
            optCreditsMap.set(`id:${String(subId)}`, creds)
          }
        })
        
        // Calculer moyenne et crédits comme dans SemesterAverages
        const notesBySubject = new Map()
        data.notes.forEach(note => {
          const subId = note.subjectId || note.id
          if (subId) notesBySubject.set(Number(subId), note)
        })
        
        const entries = []
        
        // Grouper les matières optionnelles par groupe
        const optionalGroups = new Map()
        const requiredSubjects = []
        
        optSubjects.forEach(sub => {
          if (sub.optionalGroupId) {
            if (!optionalGroups.has(sub.optionalGroupId)) {
              optionalGroups.set(sub.optionalGroupId, [])
            }
            optionalGroups.get(sub.optionalGroupId).push(sub)
          } else {
            requiredSubjects.push(sub)
          }
        })
        
        // Ajouter TOUTES les matières obligatoires (même celles sans notes = 0)
        requiredSubjects.forEach(sub => {
          const subId = Number(sub.subjectId || sub.id)
          const note = notesBySubject.get(subId)
          // Si pas de note, utiliser 0 comme dans SemesterAverages
          const gradeVal = note?.grade
          const rawGrade = (gradeVal != null && !Number.isNaN(gradeVal)) ? gradeVal : 0
          entries.push({
            subjectId: subId,
            rawGrade: rawGrade,
            __credits: optCreditsMap.get(`id:${subId}`) || 1
          })
        })
        
        // Ajouter la meilleure note de chaque groupe optionnel
        optionalGroups.forEach(groupSubjects => {
          let bestGrade = null
          let bestSubject = null
          
          groupSubjects.forEach(sub => {
            const subId = Number(sub.subjectId || sub.id)
            const note = notesBySubject.get(subId)
            const gradeVal = note?.grade
            const effectiveGrade = (gradeVal != null && !Number.isNaN(gradeVal)) ? gradeVal : 0
            if (bestGrade == null || effectiveGrade > bestGrade) {
              bestGrade = effectiveGrade
              bestSubject = sub
            }
          })
          
          if (bestSubject) {
            const subId = Number(bestSubject.subjectId || bestSubject.id)
            entries.push({
              subjectId: subId,
              rawGrade: bestGrade,
              __credits: optCreditsMap.get(`id:${subId}`) || 1
            })
          }
        })
        
        // Utiliser computeDecisionSummary
        const summary = computeDecisionSummary(entries, optCreditsMap)
        const optAvg = summary?.average ?? 0
        
        // Calculer les crédits capitalisés selon les règles d'admission
        let capitalizedCredits = 0
        const compensatedSet = summary?.compensated || new Set()
        entries.forEach(entry => {
          const gradeVal = entry.rawGrade
          const effectiveGrade = (gradeVal != null && !Number.isNaN(gradeVal)) ? gradeVal : 0
          const isCompensated = compensatedSet.has(entry.subjectId)
          
          if (effectiveGrade >= 10 || isCompensated) {
            capitalizedCredits += entry.__credits
          }
        })
        
        if (optAvg > bestAverage) {
          bestAverage = optAvg
          bestOption = option
          bestCredits = capitalizedCredits
        }
      } catch (err) {
        console.log(`Error loading option ${option.id}:`, err)
      }
    }
    
    if (bestOption) {
      // Mettre à jour le résumé S4
      const s4Index = semesterSummaries.value.findIndex(s => s.semester === 'S4')
      if (s4Index !== -1) {
        // Calculer la décision selon les mêmes règles
        const gradesBelow6 = data.notes.filter(n => {
          const g = n.grade
          const eff = (g != null && !Number.isNaN(g)) ? g : 0
          return eff < 6
        }).length
        const gradesBelow10 = data.notes.filter(n => {
          const g = n.grade
          const eff = (g != null && !Number.isNaN(g)) ? g : 0
          return eff < 10
        }).length
        
        let decision = 'Admis'
        if (bestAverage < 10 || gradesBelow6 > 0 || gradesBelow10 >= 3) {
          decision = 'Ajourné'
        }
        
        semesterSummaries.value[s4Index] = {
          semester: 'S4',
          average: bestAverage,
          credits: bestCredits,
          decision,
          subjectCount: data.notes.length,
          optionLabel: bestOption.label
        }
      }
    }
  } catch (err) {
    console.error('Error recalculating S4:', err)
  }
}

function computeDecisionSummary(entries, creditsMap = new Map()) {
  if (!entries || !entries.length) return null
  let totalCredits = 0
  let weightedSum = 0
  let belowSix = 0
  let belowTen = 0
  const compensable = []

  for (const entry of entries) {
    const gradeVal = entry.rawGrade ?? entry.grade ?? entry.bestOptionalGrade ?? entry.bestGrade
    // Treat null/NaN as 0 instead of skipping
    const effectiveGrade = (gradeVal != null && !Number.isNaN(gradeVal)) ? gradeVal : 0
    const credits = entry.__credits ?? getCreditsForEntry(entry, creditsMap)
    weightedSum += effectiveGrade * credits
    totalCredits += credits
    if (effectiveGrade < 6) {
      belowSix += 1
      belowTen += 1
    } else if (effectiveGrade < 10) {
      belowTen += 1
      compensable.push(entry.subjectId)
    }
  }

  if (totalCredits === 0) return null

  const average = Math.round((weightedSum / totalCredits) * 100) / 100
  const summary = {
    average,
    decision: 'Admis',
    compensated: new Set()
  }

  if (average < 10 || belowSix > 0 || belowTen >= 3) {
    summary.decision = 'Ajourné'
    return summary
  }

  for (const subjectId of compensable.slice(0, 2)) {
    summary.compensated.add(subjectId)
  }

  return summary
}

function getCreditsForEntry(entry, creditsMap) {
  if (entry?.__credits != null) return Number(entry.__credits) || 1
  if (!creditsMap || !creditsMap.size) return 1
  const subjectId = entry?.subjectId ?? entry?.subject?.subjectId ?? null
  if (subjectId != null) {
    const byId = creditsMap.get(`id:${Number(subjectId)}`) ?? creditsMap.get(`id:${String(subjectId)}`)
    if (byId != null) return Number(byId) || 1
  }
  return 1
}

onMounted(async () => {
  await loadStudentInfo()
  await loadSemesterSummaries()
})
</script>

<style scoped>
/* Additional styles if needed */
</style>
