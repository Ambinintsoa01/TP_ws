<template>
  <div class="notes-container">
    <!-- Header -->
    <div class="page-header">
      <div>
        <h1 class="page-title">Consultation des Notes</h1>
        <p class="page-subtitle">Consultez les notes des étudiants par semestre ou année</p>
      </div>
    </div>

    <!-- Search and Filter -->
    <div class="search-section">
      <div class="mb-3">
        <router-link to="/options" class="px-3 py-2 bg-indigo-600 text-white rounded">Voir S4 par option</router-link>
      </div>
      <div class="level-buttons mb-4 flex gap-2">
        <button class="px-3 py-1 rounded bg-green-600 text-white" @click="setLevel('L1')">L1 (S1+S2)</button>
        <button class="px-3 py-1 rounded bg-green-600 text-white" @click="setLevel('L2')">L2 (S3+S4)</button>
        <button class="px-3 py-1 rounded bg-gray-200" @click="resetFilters">Réinitialiser</button>
      </div>
      <div class="search-box">
        <Search class="w-5 h-5 text-gray-400" />
        <input type="text" placeholder="Rechercher par nom, prénom ou numéro étudiant..." class="search-input" v-model="searchQuery" />
      </div>
      <div class="filter-options">
        <select v-model="selectedSemester" class="filter-select">
          <option value="">Tous les semestres</option>
          <option value="S1">Semestre 1</option>
          <option value="S2">Semestre 2</option>
          <option value="S3">Semestre 3</option>
          <option value="S4">Semestre 4</option>
        </select>
        <select v-model="selectedLevel" class="filter-select">
          <option value="">Tous les niveaux</option>
          <option value="L1">Licence 1</option>
          <option value="L2">Licence 2</option>
          <option value="L3">Licence 3</option>
          <option value="M1">Master 1</option>
          <option value="M2">Master 2</option>
        </select>
      </div>

      <div v-if="selectedSemester === 'S4'" class="s4-option-selector">
        <label class="text-sm font-medium text-gray-700">Option S4 :</label>
        <select v-model="selectedS4Option" class="filter-select">
          <option value="">Toutes les options</option>
          <option v-for="opt in s4Options" :key="opt.id" :value="opt.id">{{ opt.label }}</option>
        </select>
      </div>

      <div v-if="selectedLevel === 'L2' && s4Options.length" class="l2-option-links">
        <p class="text-sm text-gray-600">Accès rapide aux options S4 :</p>
        <div class="flex flex-wrap gap-2">
          <router-link
            v-for="opt in s4Options"
            :key="`l2-opt-${opt.id}`"
            class="option-chip"
            :to="{ name: 'options', query: { option: opt.id } }"
          >
            {{ opt.label }}
          </router-link>
        </div>
      </div>
    </div>

    <!-- Loading state -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p class="loading-text">Chargement des notes...</p>
    </div>

    <!-- Option averages (for S4) -->
    <div v-if="!loading && optionAverages.length > 0" class="mb-6 p-4 bg-white rounded">
      <h3 class="font-semibold mb-2">Moyennes par option ({{ selectedSemester || selectedLevel }})</h3>
      <ul class="flex gap-4">
        <li v-for="opt in optionAverages" :key="opt.optionLabel" class="p-2 border rounded">
          <div class="text-sm font-medium">{{ opt.optionLabel }}</div>
          <div class="text-lg font-bold">{{ opt.average !== null ? opt.average : '—' }}</div>
          <div class="text-xs text-gray-500">({{ opt.count }} notes)</div>
        </li>
      </ul>
    </div>

    <!-- S4 option specific notes -->
    <div v-if="selectedSemester === 'S4' && selectedS4Option" class="s4-option-panel">
      <div class="p-4 bg-white rounded shadow-sm border border-gray-100">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold">Notes S4 — {{ currentS4OptionLabel }}</h3>
          <span class="text-sm text-gray-500">Moyenne option: <strong>{{ s4OptionData.optionAverage !== null ? s4OptionData.optionAverage : '—' }}</strong></span>
        </div>
        <div v-if="loadingS4Option" class="text-sm text-gray-500">Chargement des notes de l'option…</div>
        <div v-else-if="s4OptionError" class="text-red-600">{{ s4OptionError }}</div>
        <div v-else-if="s4OptionData.subjects.length" class="overflow-auto">
          <table class="w-full text-sm s4-table">
            <thead>
              <tr>
                <th class="p-2 text-left bg-gray-50">Étudiant</th>
                <th v-for="sub in s4OptionData.subjects" :key="sub.subjectId" class="p-2 text-center bg-gray-50">{{ sub.subjectCode }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="stu in s4OptionData.students" :key="stu.studentId" class="border-t">
                <td class="p-2 font-medium">{{ stu.firstName }} {{ stu.lastName }}</td>
                <td v-for="sub in s4OptionData.subjects" :key="sub.subjectId" class="p-2 text-center">
                  {{ getS4Grade(stu, sub.subjectId) }}
                </td>
              </tr>
            </tbody>
            <tfoot>
              <tr class="bg-gray-50">
                <td class="p-2 font-semibold">Moyenne matière</td>
                <td v-for="avg in s4OptionData.subjectAverages" :key="avg.subjectId" class="p-2 text-center font-semibold">
                  {{ avg.average !== null ? avg.average : '—' }}
                </td>
              </tr>
            </tfoot>
          </table>
        </div>
        <div v-else class="text-sm text-gray-500">Aucune note disponible pour cette option.</div>
      </div>
    </div>

    <!-- Notes display -->
    <div v-else-if="notesData.length > 0" class="notes-grid">
      <div v-for="studentNotes in notesData" :key="studentNotes.studentId" class="student-notes-card">
        <div class="student-header">
          <div class="student-info">
            <h3 class="student-name">{{ studentNotes.studentName }}</h3>
            <p class="student-details">Numéro: {{ studentNotes.registrationNumber }} • Programme: {{ studentNotes.program }}</p>
          </div>
          <div class="student-stats">
            <span class="stat-badge">Moyenne: {{ getStudentAverageDisplay(studentNotes) }}/20</span>
            <span
              class="stat-badge"
              :class="getDecisionBadgeClass(studentNotes)"
            >
              Décision: {{ getStudentDecisionLabel(studentNotes) }}
            </span>
          </div>
        </div>

        <div class="grades-section">
          <h4 class="section-title">{{ selectedLevel ? `Notes ${selectedLevel}` : selectedSemester ? `Notes ${selectedSemester}` : 'Toutes les notes' }}</h4>
          <div class="grades-list">
            <div v-for="grade in studentNotes.grades" :key="grade.gradeId" class="grade-item">
              <div class="grade-info">
                <span class="subject-code">{{ grade.subjectCode }}</span>
                <span class="subject-title">{{ grade.subjectTitle }}</span>
                <span class="semester-label">{{ grade.semesterLabel }}</span>
              </div>
              <div class="grade-value">
                <span class="grade-number">{{ resolveGradeValue(grade) }}/20</span>
                <span class="grade-date">{{ formatDate(grade.sessionDate) }}</span>
                <div class="grade-metadata">
                  <span>Crédits: {{ getSubjectCredits(grade.subjectId) }}</span>
                  <span>Appréciation: {{ getGradeAppreciation(studentNotes, grade) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <div v-else class="empty-state">
      <div class="empty-icon">
        <BookOpen class="w-16 h-16 text-gray-300" />
      </div>
      <h3 class="empty-title">Aucune note trouvée</h3>
      <p class="empty-text">Aucune note ne correspond aux critères de recherche</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Search, BookOpen } from 'lucide-vue-next'
import notesService from '../services/notesService'

const loading = ref(true)
const searchQuery = ref('')
const selectedSemester = ref('')
const selectedLevel = ref('')
const notesData = ref([])
const optionAverages = ref([])
const subjects = ref([])
const s4Options = ref([])
const selectedS4Option = ref('')
const s4OptionData = ref({ subjects: [], students: [], subjectAverages: [], optionAverage: null })
const loadingS4Option = ref(false)
const s4OptionError = ref(null)
const route = useRoute()

const subjectCreditsMap = computed(() => {
  const map = new Map()
  for (const subject of subjects.value || []) {
    map.set(subject.subjectId, subject.credits || 0)
  }
  return map
})

const studentSummaries = computed(() => {
  const summaries = new Map()
  const credits = subjectCreditsMap.value
  for (const student of notesData.value || []) {
    summaries.set(student.studentId, buildDecisionSummary(student.grades || [], credits))
  }
  return summaries
})

// initialize selectedSemester from query param if present
if (route.query.semester) {
  selectedSemester.value = route.query.semester
}

// react to query changes
watch(() => route.query.semester, (val) => {
  selectedSemester.value = val || ''
})

const filteredNotes = computed(() => {
  let filtered = notesData.value

  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(student =>
      student.studentName.toLowerCase().includes(query) ||
      student.registrationNumber.toString().includes(query)
    )
  }

  return filtered
})

watch([selectedSemester, selectedLevel], () => {
  loadNotes()
})

watch(selectedSemester, (val) => {
  if (val === 'S4') {
    ensureS4Options()
  } else {
    selectedS4Option.value = ''
    resetS4OptionData()
  }
})

watch(selectedLevel, (val) => {
  if (val === 'L2') {
    ensureS4Options()
  }
})

watch(selectedS4Option, (val) => {
  if (selectedSemester.value !== 'S4') return
  if (val) {
    loadS4OptionNotes()
  } else {
    resetS4OptionData()
  }
})

async function loadNotes() {
  loading.value = true
  try {
    // This would call the backend API
    // For now, using mock data
    const resp = await notesService.getAllStudentNotes(selectedSemester.value, selectedLevel.value)
    if (Array.isArray(resp)) {
      notesData.value = resp
      optionAverages.value = []
      subjects.value = []
    } else if (resp && resp.students) {
      notesData.value = resp.students
      optionAverages.value = resp.optionAverages || []
      subjects.value = resp.subjects || []
    } else {
      notesData.value = []
      optionAverages.value = []
      subjects.value = []
    }
  } catch (e) {
    console.error(e)
    notesData.value = []
  } finally {
    loading.value = false
  }
}

function resolveGradeValue(grade) {
  if (!grade) return 0
  if (grade.optionalGroupId && grade.bestOptionalGrade != null) return Number(grade.bestOptionalGrade)
  if (grade.grade != null) return Number(grade.grade)
  if (grade.bestGrade != null) return Number(grade.bestGrade)
  if (grade.rawGrade != null) return Number(grade.rawGrade)
  return 0
}

function buildDecisionSummary(grades, creditsMap) {
  let totalCredits = 0
  let weightedSum = 0
  let belowSix = 0
  let belowTen = 0
  const compensable = []

  for (const grade of grades || []) {
    const value = resolveGradeValue(grade)
    if (value == null || Number.isNaN(value)) continue
    const credits = resolveDecisionCredits(creditsMap.get(grade.subjectId))
    weightedSum += value * credits
    totalCredits += credits
    if (value < 6) {
      belowSix += 1
      belowTen += 1
    } else if (value < 10) {
      belowTen += 1
      compensable.push(grade.subjectId)
    }
  }

  const average = totalCredits > 0 ? Math.round((weightedSum / totalCredits) * 100) / 100 : null
  const summary = { average, decision: 'Admis', compensated: new Set() }

  if (average == null || average < 10 || belowSix > 0 || belowTen >= 3) {
    summary.decision = 'Ajourné'
    return summary
  }

  if (compensable.length) {
    for (const subjectId of compensable.slice(0, 2)) summary.compensated.add(subjectId)
  }

  return summary
}

function getStudentSummary(student) {
  if (!student) return { average: null, decision: '—', compensated: new Set() }
  return studentSummaries.value.get(student.studentId) || { average: null, decision: '—', compensated: new Set() }
}

function getStudentAverageDisplay(student) {
  const summary = getStudentSummary(student)
  if (summary.average == null) return '—'
  return summary.average.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function getStudentDecisionLabel(student) {
  return getStudentSummary(student).decision
}

function getDecisionBadgeClass(student) {
  const decision = getStudentDecisionLabel(student)
  return decision === 'Admis' ? 'stat-badge-positive' : 'stat-badge-negative'
}

function getSubjectCredits(subjectId) {
  const credits = subjectCreditsMap.value.get(subjectId)
  if (!credits) return '—'
  return credits
}

function getGradeAppreciation(student, grade) {
  const summary = getStudentSummary(student)
  const value = resolveGradeValue(grade)
  if (value == null || Number.isNaN(value)) return '—'
  if (value < 6) return 'Ajournée'
  if (value < 10) {
    return summary.compensated.has(grade.subjectId) && summary.decision === 'Admis' ? 'Compensée' : 'Ajournée'
  }
  if (value < 12) return 'Passable'
  if (value < 14) return 'Assez Bien'
  if (value < 16) return 'Bien'
  return 'Très Bien'
}

function resolveDecisionCredits(value) {
  const num = Number(value)
  if (Number.isFinite(num) && num > 0) return num
  return 1
}

async function ensureS4Options() {
  if (s4Options.value.length) return
  try {
    const resp = await notesService.getOptions()
    const filtered = (resp || []).filter(opt => opt.id !== 1)
    s4Options.value = filtered
  } catch (e) {
    console.error('Unable to load options', e)
  }
}

function resetS4OptionData() {
  s4OptionData.value = { subjects: [], students: [], subjectAverages: [], optionAverage: null }
  s4OptionError.value = null
  loadingS4Option.value = false
}

async function loadS4OptionNotes() {
  if (!selectedS4Option.value) return
  loadingS4Option.value = true
  s4OptionError.value = null
  try {
    const optionId = Number(selectedS4Option.value)
    const payload = await notesService.getOptionSubjects(4, Number.isNaN(optionId) ? selectedS4Option.value : optionId)
    s4OptionData.value = {
      subjects: payload?.subjects || [],
      students: payload?.students || [],
      subjectAverages: payload?.subjectAverages || [],
      optionAverage: payload?.optionAverage ?? null
    }
  } catch (e) {
    console.error('Failed to load S4 option notes', e)
    s4OptionError.value = 'Impossible de charger les notes pour cette option.'
  } finally {
    loadingS4Option.value = false
  }
}

const currentS4OptionLabel = computed(() => {
  const opt = s4Options.value.find(o => String(o.id) === String(selectedS4Option.value))
  return opt ? opt.label : ''
})

function getS4Grade(student, subjectId) {
  if (!student || !student.grades) return 0
  const entry = student.grades.find(g => g.subjectId === subjectId)
  const val = entry && entry.bestGrade != null ? entry.bestGrade : entry?.grade
  return val != null ? Number(val) : 0
}

function formatDate(dateString) {
  if (!dateString) return ''
  return new Date(dateString).toLocaleDateString('fr-FR')
}

onMounted(loadNotes)

function setLevel(l) {
  selectedLevel.value = l
  selectedSemester.value = ''
}

function resetFilters() {
  selectedLevel.value = ''
  selectedSemester.value = ''
}
</script>

<style scoped>
.notes-container {
  @apply space-y-6 animate-fade-in;
}

.page-header {
  @apply mb-8;
}

.page-title {
  @apply text-4xl font-bold text-gray-900 tracking-tight;
}

.page-subtitle {
  @apply text-gray-500 mt-1 text-lg;
}

.search-section {
  @apply bg-white p-6 rounded-2xl shadow-sm border border-gray-100 mb-8;
}

.search-box {
  @apply flex items-center gap-3 bg-gray-50 rounded-xl px-4 py-3 flex-1 max-w-md mb-4;
  @apply border border-gray-200 transition-all;
}

.search-box:focus-within {
  @apply border-gray-900 ring-2 ring-gray-900/10;
}

.search-input {
  @apply flex-1 bg-transparent outline-none text-gray-700 placeholder-gray-400 font-medium;
}

.filter-options {
  @apply flex gap-4;
}

.l2-option-links {
  @apply mt-4 space-y-2;
}

.option-chip {
  @apply px-3 py-1 rounded-full bg-indigo-50 text-indigo-700 text-sm font-semibold border border-indigo-100 hover:bg-indigo-100;
}

.filter-select {
  @apply px-4 py-2 border border-gray-300 rounded-lg bg-white text-gray-700;
  @apply focus:outline-none focus:ring-2 focus:ring-blue-500;
}

.notes-grid {
  @apply space-y-6;
}

.s4-option-selector {
  @apply mt-4 flex items-center gap-3;
}

.s4-option-panel {
  @apply mb-6;
}

.s4-table th,
.s4-table td {
  @apply border border-gray-200;
}

.student-notes-card {
  @apply bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden;
}

.student-header {
  @apply flex items-center justify-between p-6 border-b border-gray-100;
}

.student-info h3 {
  @apply text-xl font-bold text-gray-900;
}

.student-details {
  @apply text-sm text-gray-500 mt-1;
}

.student-stats {
  @apply flex items-center gap-2;
}

.stat-badge {
  @apply px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-sm font-medium;
}

.stat-badge-positive {
  @apply bg-green-100 text-green-800;
}

.stat-badge-negative {
  @apply bg-red-100 text-red-800;
}

.grades-section {
  @apply p-6;
}

.section-title {
  @apply text-lg font-semibold text-gray-900 mb-4;
}

.grades-list {
  @apply space-y-3;
}

.grade-item {
  @apply flex items-center justify-between p-4 bg-gray-50 rounded-xl;
}

.grade-info {
  @apply flex items-center gap-4;
}

.subject-code {
  @apply font-bold text-gray-900 text-sm bg-white px-2 py-1 rounded;
}

.subject-title {
  @apply text-gray-700 font-medium;
}

.semester-label {
  @apply text-xs bg-blue-100 text-blue-800 px-2 py-1 rounded-full;
}

.grade-value {
  @apply text-right;
}

.grade-number {
  @apply text-2xl font-bold text-gray-900;
}

.grade-date {
  @apply text-sm text-gray-500;
}

.grade-metadata {
  @apply mt-1 text-xs text-gray-500 flex gap-4 justify-end;
}

.grade-metadata span {
  @apply whitespace-nowrap;
}

.loading-state {
  @apply flex flex-col items-center justify-center py-20;
}

.loading-spinner {
  @apply w-12 h-12 border-4 border-gray-200 border-t-gray-900 rounded-full animate-spin;
}

.loading-text {
  @apply mt-4 text-gray-500 font-medium;
}

.empty-state {
  @apply flex flex-col items-center justify-center py-20;
}

.empty-icon {
  @apply w-24 h-24 rounded-2xl bg-gray-50 flex items-center justify-center mb-4;
}

.empty-title {
  @apply text-2xl font-bold text-gray-900 mb-2;
}

.empty-text {
  @apply text-gray-500;
}

/* Responsive */
@media (max-width: 768px) {
  .student-header {
    @apply flex-col items-start gap-4;
  }

  .grade-item {
    @apply flex-col items-start gap-2;
  }

  .grade-value {
    @apply text-left;
  }

  .filter-options {
    @apply flex-col;
  }
}
</style>