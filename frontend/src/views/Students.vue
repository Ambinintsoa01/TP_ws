<template>
  <div class="students-container">
    <!-- Header avec titre et actions -->
    <div class="page-header">
      <div>
        <h1 class="page-title">Gestion des Étudiants</h1>
        <p class="page-subtitle">Gérez les étudiants et consultez leurs notes</p>
      </div>
      <div class="header-actions">
        <button class="btn-outline">
          <Download class="w-5 h-5" />
          <span>Exporter</span>
        </button>
        <button class="btn-primary btn-shine">
          <UserPlus class="w-5 h-5" />
          <span>Nouvel Étudiant</span>
        </button>
      </div>
    </div>

    <!-- Stats cards -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon bg-gray-900">
          <Users class="w-6 h-6 text-white" />
        </div>
        <div>
          <p class="stat-label">Total Étudiants</p>
          <p class="stat-value">{{ students.length }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon bg-blue-500">
          <BookOpen class="w-6 h-6 text-white" />
        </div>
        <div>
          <p class="stat-label">Notes Consultées</p>
          <p class="stat-value">{{ totalNotes }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon bg-green-500">
          <GraduationCap class="w-6 h-6 text-white" />
        </div>
        <div>
          <p class="stat-label">Diplômés</p>
          <p class="stat-value">0</p>
        </div>
      </div>
    </div>

    <!-- Tableau des étudiants -->
    <div class="table-card">
      <!-- Barre de recherche et filtres -->
      <div class="table-toolbar">
        <div class="search-box">
          <Search class="w-5 h-5 text-gray-400" />
          <input type="text" placeholder="Rechercher un étudiant..." class="search-input" v-model="searchQuery" />
        </div>
        <div class="flex items-center gap-3">
          <button class="filter-btn">
            <Filter class="w-5 h-5" />
            <span>Filtrer</span>
          </button>
          <button class="icon-btn" @click="load">
            <RefreshCw class="w-5 h-5" />
          </button>
        </div>
      </div>

      <!-- Loading state -->
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p class="loading-text">Chargement des données...</p>
      </div>

      <!-- Table -->
      <div v-else-if="students.length > 0" class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th class="th-cell">
                <input type="checkbox" class="checkbox" />
              </th>
              <th class="th-cell">Étudiant</th>
              <th class="th-cell">Numéro Étudiant</th>
              <th class="th-cell">Programme</th>
              <th class="th-cell">Niveau</th>
              <th class="th-cell text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="student in filteredStudents" :key="student.id" class="table-row">
              <td class="td-cell">
                <input type="checkbox" class="checkbox" />
              </td>
              <td class="td-cell">
                <div class="student-info">
                  <div class="student-avatar">
                    <User class="w-5 h-5 text-gray-600" />
                  </div>
                  <div>
                    <p class="student-name">{{ student.firstName }} {{ student.lastName }}</p>
                    <p class="student-id">ID: #{{ student.id }}</p>
                  </div>
                </div>
              </td>
              <td class="td-cell">
                <span class="text-gray-700 font-medium">{{ student.registrationNumber || 'N/A' }}</span>
              </td>
              <td class="td-cell">
                <span class="program-badge">{{ student.program?.label || 'N/A' }}</span>
              </td>
              <td class="td-cell">
                <span class="level-badge">{{ student.level?.label || 'N/A' }}</span>
              </td>
              <td class="td-cell text-right">
                <div class="action-buttons-group">
                  <button class="action-btn action-btn-notes" @click="viewNotes(student)" title="Voir les notes">
                    <BookOpen class="w-4 h-4" />
                    <span>Notes</span>
                  </button>
                  <button class="action-btn action-btn-details" @click="viewDetails(student)" title="Détails">
                    <Eye class="w-4 h-4" />
                    <span>Détails</span>
                  </button>
                  <button class="action-btn" @click.stop="openLevelForStudent(student, 'L1')" title="Voir L1 (S1+S2)">
                    <span class="text-xs font-semibold">L1</span>
                  </button>
                  <button class="action-btn" @click.stop="openLevelForStudent(student, 'L2')" title="Voir L2 (S3+S4)">
                    <span class="text-xs font-semibold">L2</span>
                  </button>
                  <div class="relative">
                    <button class="action-btn action-btn-options" @click.stop="toggleOptionMenu(student.id)" title="Options S4">
                      <span class="text-xs font-semibold">Options</span>
                    </button>
                    <div v-if="optionMenuStudent === student.id" class="option-menu-panel">
                      <p class="option-menu-title">Accès rapide S4</p>
                      <button
                        v-for="opt in s4Options"
                        :key="opt.id"
                        class="option-menu-item"
                        @click.stop="openStudentOption(student, opt.id)"
                      >
                        {{ opt.label }}
                      </button>
                      <router-link class="option-menu-link" :to="{ name: 'options' }" @click="optionMenuStudent = null">
                        Vue agrégée par option
                      </router-link>
                    </div>
                  </div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Empty state -->
      <div v-else class="empty-state">
        <div class="empty-icon">
          <Users class="w-16 h-16 text-gray-300" />
        </div>
        <h3 class="empty-title">Aucun étudiant</h3>
        <p class="empty-text">Commencez par ajouter votre premier étudiant</p>
        <button class="btn-primary mt-4">
          <UserPlus class="w-5 h-5" />
          <span>Ajouter un étudiant</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import {
  Users, UserPlus, Download, Search, Filter, RefreshCw,
  User, BookOpen, Eye, GraduationCap, CheckCircle
} from 'lucide-vue-next'
import { useRouter } from 'vue-router'
import studentService from '../services/studentService'
import notesService from '../services/notesService'

const router = useRouter()

const students = ref([])
const loading = ref(true)
const searchQuery = ref('')
const totalNotes = ref(0)
const s4Options = ref([])
const optionMenuStudent = ref(null)

const filteredStudents = computed(() => {
  if (!searchQuery.value) return students.value
  const query = searchQuery.value.toLowerCase()
  return students.value.filter(s =>
    s.firstName.toLowerCase().includes(query) ||
    s.lastName.toLowerCase().includes(query) ||
    (s.registrationNumber && s.registrationNumber.toString().includes(query))
  )
})

async function load() {
  loading.value = true
  try {
    students.value = await studentService.getStudents()
    // Calculate total notes (placeholder)
    totalNotes.value = students.value.length * 5 // Assuming 5 subjects per student
  } catch (e) {
    console.error(e)
    students.value = []
  } finally {
    loading.value = false
  }
}

async function loadOptions() {
  try {
    const resp = await notesService.getOptions()
    s4Options.value = (resp || []).filter(opt => opt.id !== 1)
  } catch (e) {
    console.error('Unable to fetch options', e)
    s4Options.value = []
  }
}

function viewNotes(student) {
  // Navigate to notes view for this student
  console.log('View notes for:', student)
  router.push({ name: 'student-notes-semester', params: { id: student.id, semester: 'S1' }, query: { name: `${student.firstName} ${student.lastName}` } })
}

function viewDetails(student) {
  // Show student details
  console.log('View details for:', student)
  router.push({ path: `/students/${student.id}`, query: { name: `${student.firstName} ${student.lastName}` } })
}

function openLevelForStudent(student, level) {
  if (!student || !student.id) return
  router.push({ name: 'student-notes-level', params: { id: student.id, level }, query: { name: `${student.firstName} ${student.lastName}` } })
}

function toggleOptionMenu(studentId) {
  optionMenuStudent.value = optionMenuStudent.value === studentId ? null : studentId
}

function openStudentOption(student, optionId) {
  if (!student || !student.id || !optionId) return
  router.push({
    name: 'student-notes-semester',
    params: { id: student.id, semester: 'S4' },
    query: { name: `${student.firstName} ${student.lastName}`, option: optionId }
  })
  optionMenuStudent.value = null
}

onMounted(() => {
  load()
  loadOptions()
})
</script>

<style scoped>
.students-container {
  @apply space-y-6 animate-fade-in;
}

.page-header {
  @apply flex items-center justify-between mb-8;
}

.page-title {
  @apply text-4xl font-bold text-gray-900 tracking-tight;
}

.page-subtitle {
  @apply text-gray-500 mt-1 text-lg;
}

.header-actions {
  @apply flex items-center gap-3;
}

.stats-grid {
  @apply grid grid-cols-1 md:grid-cols-3 gap-6 mb-8;
}

.stat-card {
  @apply card flex items-center gap-4 cursor-pointer;
  transition: all 0.3s ease;
}

.stat-card:hover {
  @apply shadow-xl -translate-y-1;
}

.stat-icon {
  @apply w-14 h-14 rounded-xl flex items-center justify-center shadow-lg;
}

.stat-label {
  @apply text-sm text-gray-500 font-medium;
}

.stat-value {
  @apply text-3xl font-bold text-gray-900;
}

.table-card {
  @apply card p-0 overflow-hidden;
}

.table-toolbar {
  @apply flex items-center justify-between p-6 border-b border-gray-100;
}

.search-box {
  @apply flex items-center gap-3 bg-gray-50 rounded-xl px-4 py-3 flex-1 max-w-md;
  @apply border border-gray-200 transition-all;
}

.search-box:focus-within {
  @apply border-gray-900 ring-2 ring-gray-900/10;
}

.search-input {
  @apply flex-1 bg-transparent outline-none text-gray-700 placeholder-gray-400 font-medium;
}

.filter-btn {
  @apply flex items-center gap-2 px-4 py-2.5 rounded-xl;
  @apply border border-gray-300 hover:bg-gray-50 font-medium text-gray-700;
  transition: all 0.2s ease;
}

.icon-btn {
  @apply p-2.5 rounded-xl hover:bg-gray-100 text-gray-600 hover:text-gray-900;
  transition: all 0.2s ease;
}

.table-wrapper {
  @apply overflow-x-auto;
}

.data-table {
  @apply w-full;
}

.th-cell {
  @apply px-6 py-4 text-left text-xs font-bold text-gray-500;
  @apply uppercase tracking-wider bg-gray-50 border-b border-gray-200;
}

.table-row {
  @apply border-b border-gray-100;
  transition: background-color 0.2s ease;
}

.table-row:hover {
  @apply bg-gray-50;
}

.td-cell {
  @apply px-6 py-5 text-sm;
}

.student-info {
  @apply flex items-center gap-3;
}

.student-avatar {
  @apply w-11 h-11 rounded-xl bg-gray-100 flex items-center justify-center border border-gray-200;
}

.student-name {
  @apply font-semibold text-gray-900 text-base;
}

.student-id {
  @apply text-xs text-gray-500 font-medium;
}

.program-badge {
  @apply inline-flex items-center px-3 py-1.5 rounded-lg bg-blue-100 text-blue-800 text-sm font-bold;
}

.level-badge {
  @apply inline-flex items-center px-3 py-1.5 rounded-lg bg-green-100 text-green-800 text-sm font-bold;
}

.checkbox {
  @apply w-5 h-5 rounded border-2 border-gray-300 text-gray-900 cursor-pointer;
}

.checkbox:focus {
  @apply ring-2 ring-gray-900;
}

/* Boutons d'action améliorés */
.action-buttons-group {
  @apply flex items-center justify-end gap-2;
}

.action-btn {
  @apply flex items-center gap-2 px-3 py-2 rounded-lg font-semibold text-xs;
  @apply border-2 transition-all;
}

.action-btn:hover {
  @apply scale-105 shadow-md;
}

.action-btn-notes {
  @apply bg-blue-50 text-blue-700 border-blue-200 hover:bg-blue-100 hover:border-blue-300;
}

.action-btn-details {
  @apply bg-gray-50 text-gray-700 border-gray-200 hover:bg-gray-100 hover:border-gray-300;
}

.action-btn-options {
  @apply bg-indigo-50 text-indigo-700 border-indigo-200 hover:bg-indigo-100 hover:border-indigo-300;
}

.option-menu-panel {
  @apply absolute right-0 mt-2 w-48 bg-white border border-gray-200 rounded-lg shadow-xl z-10 p-2 space-y-1;
}

.option-menu-title {
  @apply text-xs font-semibold text-gray-500 uppercase tracking-wide px-2;
}

.option-menu-item {
  @apply w-full text-left px-2 py-1 rounded-md text-sm text-gray-700 hover:bg-indigo-50;
}

.option-menu-link {
  @apply block text-center text-xs font-semibold text-indigo-700 px-2 py-1 rounded-md hover:bg-indigo-50;
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
  .page-header {
    @apply flex-col items-start gap-4;
  }

  .stats-grid {
    @apply grid-cols-1;
  }

  .table-toolbar {
    @apply flex-col gap-4;
  }

  .search-box {
    @apply max-w-full;
  }

  .action-btn span {
    @apply hidden;
  }

  .action-buttons-group {
    @apply flex-wrap;
  }
}
</style>