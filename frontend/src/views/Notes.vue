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
    </div>

    <!-- Loading state -->
    <div v-if="loading" class="loading-state">
      <div class="loading-spinner"></div>
      <p class="loading-text">Chargement des notes...</p>
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
            <span class="stat-badge">Moyenne: {{ calculateAverage(studentNotes.grades) }}/20</span>
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
                <span class="grade-number">{{ grade.grade }}/20</span>
                <span class="grade-date">{{ formatDate(grade.sessionDate) }}</span>
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
import { Search, BookOpen } from 'lucide-vue-next'
import notesService from '../services/notesService'

const loading = ref(true)
const searchQuery = ref('')
const selectedSemester = ref('')
const selectedLevel = ref('')
const notesData = ref([])

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

async function loadNotes() {
  loading.value = true
  try {
    // This would call the backend API
    // For now, using mock data
    notesData.value = await notesService.getAllStudentNotes(selectedSemester.value, selectedLevel.value)
  } catch (e) {
    console.error(e)
    notesData.value = []
  } finally {
    loading.value = false
  }
}

function calculateAverage(grades) {
  if (!grades.length) return 0
  const sum = grades.reduce((acc, grade) => acc + grade.grade, 0)
  return (sum / grades.length).toFixed(2)
}

function formatDate(dateString) {
  if (!dateString) return ''
  return new Date(dateString).toLocaleDateString('fr-FR')
}

onMounted(loadNotes)
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

.filter-select {
  @apply px-4 py-2 border border-gray-300 rounded-lg bg-white text-gray-700;
  @apply focus:outline-none focus:ring-2 focus:ring-blue-500;
}

.notes-grid {
  @apply space-y-6;
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