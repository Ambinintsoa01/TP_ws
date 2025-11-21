<template>
  <div class="personnels-container">
    <!-- Header avec titre et actions -->
    <div class="page-header">
      <div>
        <h1 class="page-title">Gestion des Personnels</h1>
        <p class="page-subtitle">Gérez votre équipe et les demandes de congés</p>
      </div>
      <div class="header-actions">
        <button class="btn-outline">
          <Download class="w-5 h-5" />
          <span>Exporter</span>
        </button>
        <button class="btn-primary btn-shine">
          <UserPlus class="w-5 h-5" />
          <span>Nouveau Personnel</span>
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
          <p class="stat-label">Total Personnel</p>
          <p class="stat-value">{{ personnels.length }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon bg-green-500">
          <CheckCircle class="w-6 h-6 text-white" />
        </div>
        <div>
          <p class="stat-label">Actifs</p>
          <p class="stat-value">{{ personnels.length }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon bg-yellow-500">
          <Calendar class="w-6 h-6 text-white" />
        </div>
        <div>
          <p class="stat-label">En Congé</p>
          <p class="stat-value">0</p>
        </div>
      </div>
    </div>

    <!-- Tableau des personnels -->
    <div class="table-card">
      <!-- Barre de recherche et filtres -->
      <div class="table-toolbar">
        <div class="search-box">
          <Search class="w-5 h-5 text-gray-400" />
          <input type="text" placeholder="Rechercher un personnel..." class="search-input" v-model="searchQuery" />
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
      <div v-else-if="personnels.length > 0" class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th class="th-cell">
                <input type="checkbox" class="checkbox" />
              </th>
              <th class="th-cell">Personnel</th>
              <th class="th-cell">CIN</th>
              <th class="th-cell">Contact</th>
              <th class="th-cell">Salaire</th>
              <th class="th-cell">Statut</th>
              <th class="th-cell text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in filteredPersonnels" :key="p.id" class="table-row">
              <td class="td-cell">
                <input type="checkbox" class="checkbox" />
              </td>
              <td class="td-cell">
                <div class="personnel-info">
                  <div class="personnel-avatar">
                    <User class="w-5 h-5 text-gray-600" />
                  </div>
                  <div>
                    <p class="personnel-name">{{ p.nom }} {{ p.prenom }}</p>
                    <p class="personnel-id">ID: #{{ p.id }}</p>
                  </div>
                </div>
              </td>
              <td class="td-cell">
                <span class="text-gray-700 font-medium">{{ p.cin }}</span>
              </td>
              <td class="td-cell">
                <div class="flex items-center gap-2">
                  <Phone class="w-4 h-4 text-gray-400" />
                  <span class="text-gray-700">{{ p.contact }}</span>
                </div>
              </td>
              <td class="td-cell">
                <span class="salary-badge">{{ formatSalary(p.salaire) }}</span>
              </td>
              <td class="td-cell">
                <span class="status-badge status-active">Actif</span>
              </td>
              <td class="td-cell text-right">
                <div class="action-buttons-group">
                  <button class="action-btn action-btn-conge" @click="openCongeModal(p)" title="Congé annuel">
                    <Palmtree class="w-4 h-4" />
                    <span>Congé</span>
                  </button>
                  <button class="action-btn action-btn-absence" @click="openAbsenceModal(p)" title="Absence spéciale">
                    <CalendarOff class="w-4 h-4" />
                    <span>Absence</span>
                  </button>
                  <button class="action-btn action-btn-urgence" @click="openUrgenceModal(p)" title="Urgence">
                    <AlertTriangle class="w-4 h-4" />
                    <span>Urgence</span>
                  </button>
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
        <h3 class="empty-title">Aucun personnel</h3>
        <p class="empty-text">Commencez par ajouter votre premier membre d'équipe</p>
        <button class="btn-primary mt-4">
          <UserPlus class="w-5 h-5" />
          <span>Ajouter un personnel</span>
        </button>
      </div>
    </div>

    <!-- Modal de demande de congé -->
    <Transition name="modal">
      <div v-if="showCongeModal" class="modal-overlay" @click.self="closeCongeModal">
        <div class="modal-container">
          <!-- Header -->
          <div class="modal-header modal-header-conge">
            <div class="flex items-center gap-4">
              <div class="modal-icon modal-icon-conge">
                <Palmtree class="w-6 h-6" />
              </div>
              <div>
                <h3 class="modal-title">Demande de Congé</h3>
                <p v-if="selectedPersonnel" class="modal-subtitle">
                  {{ selectedPersonnel.nom }} {{ selectedPersonnel.prenom }} • ID #{{ selectedPersonnel.id }}
                </p>
              </div>
            </div>
            <button @click="closeCongeModal" class="modal-close">
              <X class="w-5 h-5" />
            </button>
          </div>

          <!-- Body -->
          <div class="modal-body">
            <!-- Error message -->
            <div v-if="errorMsgConge" class="error-alert">
              <AlertCircle class="w-5 h-5" />
              <span>{{ errorMsgConge }}</span>
            </div>

            <!-- Form -->
            <div class="form-grid">

              <div class="form-group col-span-2">
                <label class="form-label">
                  <FileText class="w-4 h-4" />
                  <span>Motif du congé</span>
                </label>
                <textarea v-model="formConge.motif" placeholder="Décrivez le motif de votre demande..." class="input"
                  rows="3"></textarea>
              </div>

              <div class="form-group">
                <label class="form-label">
                  <Calendar class="w-4 h-4" />
                  <span>Date demande</span>
                </label>
                <input v-model="formConge.date_demande" type="date" class="input" />
              </div>

              <div class="form-group">
                <label class="form-label">
                  <Calendar class="w-4 h-4" />
                  <span>Date de début</span>
                </label>
                <input v-model="formConge.date_debut" type="date" class="input" @change="handleDateDebutChange" />
              </div>

              <div class="form-group">
                <label class="form-label">
                  <Calendar class="w-4 h-4" />
                  <span>Date de fin</span>
                  <span v-if="congeType === 'maternite'" class="text-xs text-green-600">(Auto-calculée)</span>
                </label>
                <input v-model="formConge.date_fin" type="date" class="input" :readonly="congeType === 'maternite'"
                  :class="{ 'bg-green-50': congeType === 'maternite' }" />
              </div>

              <!-- Durée calculée -->
              <div v-if="calculatedCongeDays > 0" class="col-span-2 info-box info-box-conge">
                <Clock class="w-5 h-5" />
                <div class="flex-1">
                  <span class="font-bold text-lg">{{ calculatedCongeDays }} jour(s)</span>
                  <span v-if="congeType === 'maternite'" class="ml-2 text-sm text-green-700">• 3 mois de congé
                    maternité</span>
                  <span v-if="congeType === 'paternite'" class="ml-2 text-sm text-blue-700">• 10 jours de congé
                    paternité</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Footer -->
          <div class="modal-footer">
            <button @click="closeCongeModal" class="btn-outline">
              Annuler
            </button>
            <button @click="submitConge" :disabled="submittingConge" class="btn-primary btn-shine">
              <Send class="w-4 h-4" />
              <span v-if="!submittingConge">Envoyer la demande</span>
              <span v-else class="flex items-center gap-2">
                <div class="loading-spinner-small"></div>
                Envoi en cours...
              </span>
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- Modal d'absence spéciale -->
    <Transition name="modal">
      <div v-if="showAbsenceModal" class="modal-overlay" @click.self="closeAbsenceModal">
        <div class="modal-container">
          <div class="modal-header modal-header-absence">
            <div class="flex items-center gap-4">
              <div class="modal-icon modal-icon-absence">
                <CalendarOff class="w-6 h-6" />
              </div>
              <div>
                <h3 class="modal-title">Absence Spéciale</h3>
                <p v-if="selectedPersonnel" class="modal-subtitle">
                  {{ selectedPersonnel.nom }} {{ selectedPersonnel.prenom }} • ID #{{ selectedPersonnel.id }}
                </p>
              </div>
            </div>
            <button @click="closeAbsenceModal" class="modal-close">
              <X class="w-5 h-5" />
            </button>
          </div>

          <div class="modal-body">
            <div v-if="errorMsgAbsence" class="error-alert">
              <AlertCircle class="w-5 h-5" />
              <span>{{ errorMsgAbsence }}</span>
            </div>

            <div class="form-grid">
              <div class="form-group col-span-2">
                <label class="form-label">
                  <FileText class="w-4 h-4" />
                  <span>Motif de l'absence</span>
                </label>
                <textarea v-model="formAbsence.motif" placeholder="Ex: Événement familial, Mariage, Décès..."
                  class="input" rows="3"></textarea>
              </div>

              <div class="form-group">
                <label class="form-label">
                  <Calendar class="w-4 h-4" />
                  <span>Date demande</span>
                </label>
                <input v-model="formAbsence.date_demande" type="date" class="input" />
              </div>

              <div class="form-group">
                <label class="form-label">
                  <Calendar class="w-4 h-4" />
                  <span>Date de début</span>
                </label>
                <input v-model="formAbsence.date_debut" type="date" class="input" />
              </div>

              <div class="form-group">
                <label class="form-label">
                  <Calendar class="w-4 h-4" />
                  <span>Date de fin</span>
                </label>
                <input v-model="formAbsence.date_fin" type="date" class="input" />
              </div>

              <div v-if="calculatedAbsenceDays > 0" class="col-span-2 info-box info-box-absence">
                <Clock class="w-5 h-5" />
                <div class="flex-1">
                  <span class="font-bold text-lg">{{ calculatedAbsenceDays }} jour(s)</span>
                  <span class="ml-2 text-sm text-yellow-700">• Maximum 10 jours par période</span>
                </div>
              </div>
            </div>
          </div>

          <div class="modal-footer">
            <button @click="closeAbsenceModal" class="btn-outline">Annuler</button>
            <button @click="submitAbsence" :disabled="submittingAbsence" class="btn-primary btn-shine">
              <Send class="w-4 h-4" />
              <span v-if="!submittingAbsence">Envoyer la demande</span>
              <span v-else class="flex items-center gap-2">
                <div class="loading-spinner-small"></div>
                Envoi en cours...
              </span>
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- Modal de demande d'urgence -->
    <Transition name="modal">
      <div v-if="showUrgenceModal" class="modal-overlay" @click.self="closeUrgenceModal">
        <div class="modal-container">
          <div class="modal-header modal-header-urgence">
            <div class="flex items-center gap-4">
              <div class="modal-icon modal-icon-urgence">
                <AlertTriangle class="w-6 h-6" />
              </div>
              <div>
                <h3 class="modal-title">Demande d'Urgence</h3>
                <p v-if="selectedPersonnel" class="modal-subtitle">
                  {{ selectedPersonnel.nom }} {{ selectedPersonnel.prenom }} • ID #{{ selectedPersonnel.id }}
                </p>
              </div>
            </div>
            <button @click="closeUrgenceModal" class="modal-close">
              <X class="w-5 h-5" />
            </button>
          </div>

          <div class="modal-body">
            <div v-if="errorMsgUrgence" class="error-alert">
              <AlertCircle class="w-5 h-5" />
              <span>{{ errorMsgUrgence }}</span>
            </div>

            <div class="form-grid">
              <div class="form-group col-span-2">
                <label class="form-label">
                  <Tag class="w-4 h-4" />
                  <span>Type d'urgence</span>
                </label>
                <select v-model.number="formUrgence.id_type_urgence" @change="handleUrgenceTypeChange" class="input">
                  <option disabled :value="null">Sélectionner un type d'urgence</option>
                  <option v-for="t in urgenceTypes" :key="t.id" :value="t.id">
                    {{ t.libelle }}
                  </option>
                </select>
                <div v-if="!urgenceTypes.length" class="text-xs text-gray-500 mt-1 flex items-center gap-1">
                  <div class="loading-spinner-tiny"></div>
                  <span>Chargement des types d'urgence...</span>
                </div>

              </div>

              <div class="form-group col-span-2">
                <label class="form-label">
                  <FileText class="w-4 h-4" />
                  <span>Description de l'urgence</span>
                </label>
                <textarea v-model="formUrgence.motif" placeholder="Décrivez la situation d'urgence en détail..."
                  class="input" rows="3"></textarea>
              </div>

              <div class="form-group col-span-2">
                <label class="form-label">
                  <Calendar class="w-4 h-4" />
                  <span>Date de la demande</span>
                </label>
                <input v-model="formUrgence.date_demande" type="date" class="input" />
              </div>

              <div class="form-group">
                <label class="form-label">
                  <Clock class="w-4 h-4" />
                  <span>Début</span>
                  <span v-if="isMaterniteUrgence" class="text-xs text-green-600">(Date seulement)</span>
                </label>
                <input v-model="formUrgence.date_heure_debut" :type="isMaterniteUrgence ? 'date' : 'datetime-local'"
                  class="input" @change="handleUrgenceDebutChange" />
              </div>

              <div class="form-group">
                <label class="form-label">
                  <Clock class="w-4 h-4" />
                  <span>Fin</span>
                  <span v-if="isMaterniteUrgence" class="text-xs text-green-600 font-bold">✓ Auto (3 mois)</span>
                </label>
                <input v-model="formUrgence.date_heure_fin" :type="isMaterniteUrgence ? 'date' : 'datetime-local'"
                  class="input" :readonly="isMaterniteUrgence"
                  :class="{ 'bg-green-50 border-green-300': isMaterniteUrgence }" />
              </div>

              <div v-if="urgenceDurationText" class="col-span-2 info-box info-box-urgence">
                <Clock class="w-5 h-5" />
                <div class="flex-1">
                  <span class="font-bold text-lg">Durée: {{ urgenceDurationText }}</span>
                  <span v-if="isMaterniteUrgence" class="ml-2 text-sm text-green-700">🤰 Congé de maternité (90
                    jours)</span>
                </div>
              </div>
            </div>
          </div>

          <div class="modal-footer">
            <button @click="closeUrgenceModal" class="btn-outline">Annuler</button>
            <button @click="submitUrgence" :disabled="submittingUrgence" class="btn-primary btn-shine">
              <Send class="w-4 h-4" />
              <span v-if="!submittingUrgence">Envoyer la demande</span>
              <span v-else class="flex items-center gap-2">
                <div class="loading-spinner-small"></div>
                Envoi en cours...
              </span>
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import {
  Users, UserPlus, Download, Search, Filter, RefreshCw,
  User, Phone, MoreVertical, X, AlertCircle, FileText,
  Calendar, Clock, CheckCircle, Palmtree, CalendarOff,
  AlertTriangle, Tag, Send
} from 'lucide-vue-next'
import personnelService from '../services/personnelService'
import congeDemandeService from '../services/congeDemandeService'
import urgenceDemandeService from '../services/urgenceDemandeService'

const personnels = ref([])
const loading = ref(true)
const searchQuery = ref('')
const showCongeModal = ref(false)
const showAbsenceModal = ref(false)
const showUrgenceModal = ref(false)
const selectedPersonnel = ref(null)

const submittingConge = ref(false)
const submittingAbsence = ref(false)
const submittingUrgence = ref(false)
const errorMsgConge = ref('')
const errorMsgAbsence = ref('')
const errorMsgUrgence = ref('')

const formConge = ref({ motif: '', date_demande: '', date_debut: '', date_fin: '' })
const formAbsence = ref({ motif: '', date_demande: '', date_debut: '', date_fin: '' })
const formUrgence = ref({ motif: '', id_type_urgence: null, date_demande: '', date_heure_debut: '', date_heure_fin: '' })
const urgenceTypes = ref([])

const filteredPersonnels = computed(() => {
  if (!searchQuery.value) return personnels.value
  const query = searchQuery.value.toLowerCase()
  return personnels.value.filter(p =>
    p.nom.toLowerCase().includes(query) ||
    p.prenom.toLowerCase().includes(query) ||
    p.cin.includes(query) ||
    p.contact.includes(query)
  )
})

// Vérifier si c'est un congé de maternité dans le modal URGENCE (id_type_urgence === 1)
const isMaterniteUrgence = computed(() => {
  return formUrgence.value.id_type_urgence === 1
})

const calculatedCongeDays = computed(() => {
  if (!formConge.value.date_debut || !formConge.value.date_fin) return 0
  const start = new Date(formConge.value.date_debut)
  const end = new Date(formConge.value.date_fin)
  const diff = Math.ceil((end - start) / (1000 * 60 * 60 * 24))
  return diff + 1
})

const calculatedAbsenceDays = computed(() => {
  if (!formAbsence.value.date_debut || !formAbsence.value.date_fin) return 0
  const start = new Date(formAbsence.value.date_debut)
  const end = new Date(formAbsence.value.date_fin)
  const diff = Math.ceil((end - start) / (1000 * 60 * 60 * 24))
  return diff + 1
})

const urgenceDurationText = computed(() => {
  if (!formUrgence.value.date_heure_debut || !formUrgence.value.date_heure_fin) return ''

  if (isMaterniteUrgence.value) {
    const start = new Date(formUrgence.value.date_heure_debut)
    const end = new Date(formUrgence.value.date_heure_fin)
    const days = Math.ceil((end - start) / (1000 * 60 * 60 * 24))
    return `${days} jours`
  }

  const start = new Date(formUrgence.value.date_heure_debut)
  const end = new Date(formUrgence.value.date_heure_fin)
  const ms = end - start
  if (ms <= 0) return ''
  const minutes = Math.floor(ms / 60000)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  const remHours = hours % 24
  const remMinutes = minutes % 60

  if (days > 0) return `${days}j ${remHours}h ${remMinutes}min`
  if (hours > 0) return `${hours}h ${remMinutes}min`
  return `${remMinutes}min`
})

function handleUrgenceTypeChange() {
  // Si congé de maternité (id = 1) et date début existe
  if (isMaterniteUrgence.value && formUrgence.value.date_heure_debut) {
    calculateMaterniteUrgenceEndDate()
  }
}

function handleUrgenceDebutChange() {
  if (isMaterniteUrgence.value) {
    calculateMaterniteUrgenceEndDate()
  }
}

function calculateMaterniteUrgenceEndDate() {
  if (!formUrgence.value.date_heure_debut) return
  const startDate = new Date(formUrgence.value.date_heure_debut)
  startDate.setDate(startDate.getDate() + 90)
  formUrgence.value.date_heure_fin = startDate.toISOString().split('T')[0]
}

async function load() {
  loading.value = true
  try {
    personnels.value = await personnelService.getPersonnels()
  } catch (e) {
    console.error(e)
    personnels.value = []
  } finally {
    loading.value = false
  }
}

function formatSalary(amount) {
  return new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency: 'MGA',
    minimumFractionDigits: 0
  }).format(amount)
}

function openCongeModal(p) {
  selectedPersonnel.value = p
  const today = new Date()
  const todayStr = new Date(today.getTime() - today.getTimezoneOffset() * 60000).toISOString().slice(0, 10)
  formConge.value = { motif: '', date_demande: todayStr, date_debut: '', date_fin: '' }
  errorMsgConge.value = ''
  showCongeModal.value = true
}

function closeCongeModal() {
  showCongeModal.value = false
}

function openAbsenceModal(p) {
  selectedPersonnel.value = p
  const today = new Date()
  const todayStr = new Date(today.getTime() - today.getTimezoneOffset() * 60000).toISOString().slice(0, 10)
  formAbsence.value = { motif: '', date_demande: todayStr, date_debut: '', date_fin: '' }
  errorMsgAbsence.value = ''
  showAbsenceModal.value = true
}

function closeAbsenceModal() {
  showAbsenceModal.value = false
}

function openUrgenceModal(p) {
  selectedPersonnel.value = p
  const today = new Date()
  const todayStr = new Date(today.getTime() - today.getTimezoneOffset() * 60000).toISOString().slice(0, 10)
  formUrgence.value = { motif: '', id_type_urgence: null, date_demande: todayStr, date_heure_debut: '', date_heure_fin: '' }
  errorMsgUrgence.value = ''
  showUrgenceModal.value = true
  if (!urgenceTypes.value.length) {
    urgenceDemandeService.fetchUrgenceTypes()
      .then(types => { urgenceTypes.value = types })
      .catch(() => { urgenceTypes.value = [] })
  }
}

function closeUrgenceModal() {
  showUrgenceModal.value = false
}

function validateCongeForm() {
  const { motif, date_demande, date_debut, date_fin } = formConge.value
  if (!motif || !date_demande || !date_debut || !date_fin) return 'Tous les champs sont requis'
  if (date_fin < date_debut) return 'La date de fin doit être postérieure ou égale à la date de début'
  return null
}

function validateAbsenceForm() {
  const { motif, date_demande, date_debut, date_fin } = formAbsence.value
  if (!motif || !date_demande || !date_debut || !date_fin) return 'Tous les champs sont requis'
  if (date_fin < date_debut) return 'La date de fin doit être postérieure ou égale à la date de début'
  const days = calculatedAbsenceDays.value
  if (days > 10) return 'Les absences spéciales sont limitées à 10 jours maximum'
  return null
}

function validateUrgenceForm() {
  const { motif, id_type_urgence, date_demande, date_heure_debut, date_heure_fin } = formUrgence.value
  if (!motif || !id_type_urgence || !date_demande || !date_heure_debut || !date_heure_fin) return 'Tous les champs sont requis'
  if (new Date(date_heure_fin) <= new Date(date_heure_debut)) return "La date/heure de fin doit être postérieure à la date/heure de début"
  return null
}

async function submitConge() {
  errorMsgConge.value = ''
  const err = validateCongeForm()
  if (err) {
    errorMsgConge.value = err
    return
  }
  if (!selectedPersonnel.value) return
  submittingConge.value = true
  try {
    const response = await congeDemandeService.createDemandeConge(selectedPersonnel.value.id, { ...formConge.value })
    alert(response.message || 'Demande de congé créée avec succès')
    closeCongeModal()
  } catch (e) {
    console.error(e)
    errorMsgConge.value = e?.response?.data?.error || 'Erreur lors de la création de la demande'
  } finally {
    submittingConge.value = false
  }
}

async function submitAbsence() {
  errorMsgAbsence.value = ''
  const err = validateAbsenceForm()
  if (err) {
    errorMsgAbsence.value = err
    return
  }
  if (!selectedPersonnel.value) return
  submittingAbsence.value = true
  try {
    const response = await congeDemandeService.createAbsenceSpecialeDemande(selectedPersonnel.value.id, { ...formAbsence.value })
    alert(response.message || "Absence spéciale créée avec succès")
    closeAbsenceModal()
  } catch (e) {
    console.error(e)
    errorMsgAbsence.value = e?.response?.data?.error || "Erreur lors de la création de l'absence spéciale"
  } finally {
    submittingAbsence.value = false
  }
}

async function submitUrgence() {
  errorMsgUrgence.value = ''
  const err = validateUrgenceForm()
  if (err) {
    errorMsgUrgence.value = err
    return
  }
  if (!selectedPersonnel.value) return
  submittingUrgence.value = true
  try {
    const payload = { ...formUrgence.value }
    if (payload.date_heure_debut && payload.date_heure_debut.indexOf('T') > -1) {
      payload.date_heure_debut = new Date(payload.date_heure_debut).toISOString()
    }
    if (payload.date_heure_fin && payload.date_heure_fin.indexOf('T') > -1) {
      payload.date_heure_fin = new Date(payload.date_heure_fin).toISOString()
    }
    const response = await congeDemandeService.createUrgenceDemande(selectedPersonnel.value.id, payload)
    alert(response.message || "Demande d'urgence créée avec succès")
    closeUrgenceModal()
  } catch (e) {
    console.error(e)
    errorMsgUrgence.value = e?.response?.data?.error || "Erreur lors de la création de la demande d'urgence"
  } finally {
    submittingUrgence.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.personnels-container {
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

.personnel-info {
  @apply flex items-center gap-3;
}

.personnel-avatar {
  @apply w-11 h-11 rounded-xl bg-gray-100 flex items-center justify-center border border-gray-200;
}

.personnel-name {
  @apply font-semibold text-gray-900 text-base;
}

.personnel-id {
  @apply text-xs text-gray-500 font-medium;
}

.salary-badge {
  @apply inline-flex items-center px-3 py-1.5 rounded-lg bg-gray-900 text-white text-sm font-bold;
}

.status-badge {
  @apply inline-flex items-center px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wide;
}

.status-active {
  @apply bg-green-100 text-green-800 border border-green-200;
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

.action-btn-conge {
  @apply bg-blue-50 text-blue-700 border-blue-200 hover:bg-blue-100 hover:border-blue-300;
}

.action-btn-absence {
  @apply bg-yellow-50 text-yellow-700 border-yellow-200 hover:bg-yellow-100 hover:border-yellow-300;
}

.action-btn-urgence {
  @apply bg-red-50 text-red-700 border-red-200 hover:bg-red-100 hover:border-red-300;
}

.loading-state {
  @apply flex flex-col items-center justify-center py-20;
}

.loading-spinner {
  @apply w-12 h-12 border-4 border-gray-200 border-t-gray-900 rounded-full animate-spin;
}

.loading-spinner-small {
  @apply w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin;
}

.loading-spinner-tiny {
  @apply w-3 h-3 border-2 border-gray-300 border-t-gray-600 rounded-full animate-spin;
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

/* Modal amélioré */
.modal-overlay {
  @apply fixed inset-0 z-50 flex items-center justify-center p-4;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(8px);
}

.modal-container {
  @apply w-full max-w-2xl bg-white rounded-2xl shadow-2xl overflow-hidden;
}

.modal-header {
  @apply flex items-start justify-between p-6 border-b border-gray-100;
}

.modal-header-conge {
  @apply bg-gradient-to-r from-blue-50 to-white;
}

.modal-header-absence {
  @apply bg-gradient-to-r from-yellow-50 to-white;
}

.modal-header-urgence {
  @apply bg-gradient-to-r from-red-50 to-white;
}

.modal-icon {
  @apply w-12 h-12 rounded-xl flex items-center justify-center shadow-lg;
}

.modal-icon-conge {
  @apply bg-blue-500 text-white;
}

.modal-icon-absence {
  @apply bg-yellow-500 text-white;
}

.modal-icon-urgence {
  @apply bg-red-500 text-white;
}

.modal-title {
  @apply text-2xl font-bold text-gray-900;
}

.modal-subtitle {
  @apply text-sm text-gray-500 mt-1 font-medium;
}

.modal-close {
  @apply p-2 rounded-xl hover:bg-gray-100 text-gray-500 hover:text-gray-900;
  transition: all 0.2s ease;
}

.modal-body {
  @apply p-6;
}

.modal-footer {
  @apply flex items-center justify-end gap-3 p-6 border-t border-gray-100 bg-gray-50;
}

.form-grid {
  @apply grid grid-cols-2 gap-5;
}

.form-group {
  @apply space-y-2;
}

.form-label {
  @apply flex items-center gap-2 text-sm font-semibold text-gray-700;
}

textarea.input {
  @apply resize-none;
}

.error-alert {
  @apply flex items-center gap-3 p-4 rounded-xl bg-red-50 text-red-800;
  @apply border border-red-200 mb-5;
}

.info-box {
  @apply flex items-center gap-3 p-4 rounded-xl border;
}

.info-box-conge {
  @apply bg-blue-50 border-blue-200 text-blue-900;
}

.info-box-absence {
  @apply bg-yellow-50 border-yellow-200 text-yellow-900;
}

.info-box-urgence {
  @apply bg-red-50 border-red-200 text-red-900;
}

/* Transitions */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-active .modal-container,
.modal-leave-active .modal-container {
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-container {
  transform: scale(0.95) translateY(-20px);
  opacity: 0;
}

.modal-leave-to .modal-container {
  transform: scale(0.95) translateY(20px);
  opacity: 0;
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

  .form-grid {
    @apply grid-cols-1;
  }

  .action-btn span {
    @apply hidden;
  }

  .action-buttons-group {
    @apply flex-wrap;
  }
}
</style>