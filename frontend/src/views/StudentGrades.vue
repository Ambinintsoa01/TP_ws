<template>
  <div class="p-6">
    <button class="mb-4 text-sm text-gray-600 hover:underline" @click="$router.back()">← Retour</button>
    <h1 class="text-2xl font-bold mb-2">Notes de l'étudiant — {{ semesterLabel || semester }}</h1>
    <p class="text-sm text-gray-500 mb-1">ID étudiant: {{ studentId }}</p>
    <p class="text-sm text-gray-700 font-medium mb-3">{{ studentName }}</p>

    <div class="mb-4">
      <h2 class="text-lg font-semibold mb-2">Moyennes par semestre</h2>
      <div v-if="loadingAverages">Chargement des moyennes...</div>
      <div v-else>
        <table class="w-full table-auto border-collapse mb-2">
          <thead>
            <tr class="bg-gray-100">
              <th class="p-2 text-left">Semestre</th>
              <th class="p-2 text-right">Moyenne</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="s in ['S1','S2','S3','S4']" :key="s" class="border-t">
              <td class="p-2">{{ s }}</td>
              <td class="p-2 text-right font-semibold">{{ displayedAverages[s] == null ? '—' : displayedAverages[s] }}</td>
            </tr>
          </tbody>
        </table>

        <div class="flex gap-4">
          <button class="px-3 py-1 rounded bg-blue-600 text-white" @click="router.push({ name: 'student-notes-level', params: { id: studentId, level: 'L1' } })">L1 (S1+S2)</button>
          <button class="px-3 py-1 rounded bg-blue-600 text-white" @click="router.push({ name: 'student-notes-level', params: { id: studentId, level: 'L2' } })">L2 (S3+S4)</button>
        </div>
      </div>
    </div>

    <div v-if="isS4View" class="mb-4 p-4 bg-indigo-50 rounded border border-indigo-100">
      <div class="flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
        <div>
          <h2 class="text-lg font-semibold text-indigo-900">Option du semestre 4</h2>
          <p class="text-sm text-indigo-700">Choisissez une option pour afficher les notes ciblées de cet étudiant.</p>
        </div>
        <div class="flex items-center gap-2">
          <label for="studentOptionSelect" class="text-sm font-medium text-gray-700">Option</label>
          <select
            id="studentOptionSelect"
            class="px-3 py-2 border border-gray-300 rounded-md bg-white text-gray-800 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            v-model="selectedS4Option"
            :disabled="loadingS4Options || !s4Options.length"
          >
            <option value="">— Choisir —</option>
            <option v-for="opt in s4Options" :key="opt.id" :value="opt.id">{{ opt.label }}</option>
          </select>
        </div>
      </div>
      <p v-if="loadingS4Options" class="mt-2 text-sm text-gray-600">Chargement des options…</p>
      <p v-else-if="!s4Options.length" class="mt-2 text-sm text-gray-600">Aucune option disponible pour le semestre 4.</p>
    </div>

    <div v-if="isS4View && selectedS4Option" class="mb-6 p-4 bg-white rounded border border-gray-100 shadow-sm">
      <div class="flex flex-col gap-2 md:flex-row md:items-center md:justify-between mb-3">
        <div>
          <h3 class="text-lg font-semibold">Notes S4 — {{ currentS4OptionLabel }}</h3>
          <p class="text-sm text-gray-500">Affichage limité à l'étudiant sélectionné.</p>
        </div>
        <div class="text-sm text-gray-600 flex flex-wrap gap-4">
            <span>Moyenne option: <strong>{{ s4StudentAverageDisplay }}</strong></span>
          <span>Décision S4: <strong :class="s4DecisionClass">{{ s4DecisionLabel }}</strong></span>
        </div>
      </div>
      <div v-if="loadingS4Option" class="text-sm text-gray-500">Chargement des notes de l'option…</div>
      <div v-else-if="s4OptionError" class="text-sm text-red-600">{{ s4OptionError }}</div>
      <div v-else-if="s4OptionData.studentRecord && visibleS4Subjects.length" class="overflow-x-auto">
          <table class="w-full table-auto border-collapse text-sm">
          <thead>
            <tr class="bg-gray-50">
                <th class="p-2 text-left">Matière</th>
                <th class="p-2 text-right">Crédits</th>
                <th class="p-2 text-right">Note étudiant</th>
                <th class="p-2 text-right">Moyenne option</th>
            </tr>
          </thead>
          <tbody>
              <tr v-for="sub in visibleS4Subjects" :key="sub.subjectId || sub.subjectCode" class="border-t">
              <td class="p-2">
                <div class="font-medium text-gray-900">{{ sub.subjectCode }}</div>
                <div class="text-xs text-gray-500">{{ sub.subjectTitle }}</div>
              </td>
                <td class="p-2 text-right">{{ formatSubjectCredits(sub) }}</td>
                <td class="p-2 text-right font-semibold">{{ formatGrade(getStudentOptionGrade(sub)) }}</td>
                <td class="p-2 text-right text-gray-600">{{ formatGrade(getSubjectOptionAverage(sub.subjectId)) }}</td>
            </tr>
          </tbody>
            <tfoot>
              <tr class="border-t">
                <td colspan="3" class="p-2 font-bold">Crédits capitalisés</td>
                <td class="p-2 text-right font-bold">{{ s4OptionCreditsDisplay }}</td>
              </tr>
            </tfoot>
        </table>
      </div>
      <div v-else class="text-sm text-gray-500">Cet étudiant n'a pas encore de notes pour cette option.</div>
    </div>

    <div v-if="loading">Chargement des notes...</div>

    <div v-else-if="!isS4View">
      <div class="mb-4 flex flex-wrap gap-4 text-sm text-gray-700">
        <div class="px-3 py-2 bg-gray-100 rounded-lg">
          <span class="font-medium">Moyenne générale:</span>
          <span class="ml-2 font-bold">{{ semesterAverageDisplay }}</span>
        </div>
        <div class="px-3 py-2 bg-gray-100 rounded-lg">
          <span class="font-medium">Décision:</span>
          <span class="ml-2 font-bold" :class="semesterDecision.decision === 'Admis' ? 'text-green-700' : 'text-red-700'">
            {{ semesterDecision.decision }}
          </span>
        </div>
      </div>
      <div v-if="error" class="mb-4 p-3 bg-red-100 text-red-800 rounded">{{ error }} <button v-if="error && error.includes('Authentification')" class="ml-3 underline" @click="$router.push('/login')">Se connecter</button></div>
      <table class="w-full table-auto border-collapse">
        <thead>
          <tr class="bg-gray-100">
            <th class="p-2 text-left">Code</th>
            <th class="p-2 text-left">Matière</th>
            <th class="p-2 text-left">Session</th>
            <th class="p-2 text-right">Crédits</th>
            <th class="p-2 text-right">Note</th>
            <th class="p-2 text-right">Appréciation</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="note in notes" :key="note.gradeId" class="border-t">
            <td class="p-2">{{ note.subjectCode }}</td>
            <td class="p-2">{{ note.subjectTitle }}</td>
            <td class="p-2">{{ note.sessionDate || '—' }}</td>
            <td class="p-2 text-right">{{ formatSubjectCredits(note) }}</td>
            <td class="p-2 text-right font-semibold">{{ formatGrade(note.grade) }}</td>
            <td class="p-2 text-right">{{ getSubjectAppreciation(note) }}</td>
          </tr>
        </tbody>
        <tfoot>
          <tr class="border-t">
            <td colspan="4" class="p-2 font-bold">Crédits capitalisés</td>
            <td class="p-2 text-right font-bold">{{ semesterCreditsTotalDisplay }}</td>
          </tr>
          <tr class="border-t">
            <td colspan="4" class="p-2 font-bold">Moyenne</td>
            <td class="p-2 text-right font-bold">{{ semesterAverageDisplay }}</td>
          </tr>
        </tfoot>
      </table>

      <div v-if="notes.length === 0 && !error" class="mt-4 text-sm text-gray-500">Aucune note trouvée pour cet étudiant et ce semestre.</div>
    </div>
    <div v-else-if="!selectedS4Option" class="text-sm text-gray-500">Sélectionnez une option pour consulter les notes de S4.</div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../services/api/api.js'
import notesService from '../services/notesService'
import semesterService from '../services/semesterService'
import { extractDecisionSummary } from '../utils/decisionSummary'

const route = useRoute()
const studentId = Number(route.params.id || route.query.id)
const semester = route.params.semester || route.query.semester || ''
const studentName = route.query.name || ''
const router = useRouter()

const notes = ref([])
const loading = ref(true)
const semesterLabel = ref('')
const error = ref(null)
const averages = ref({})
const loadingAverages = ref(false)
const s4Options = ref([])
const lastS4OptionStorageKey = `student:${studentId}:s4-option`
const storedS4Option = readStoredS4Option()
const routeOptionParam = route.query.option ? String(route.query.option) : ''
const selectedS4Option = ref(routeOptionParam || storedS4Option || '')
const loadingS4Options = ref(false)
const loadingS4Option = ref(false)
const s4OptionError = ref(null)
const s4OptionData = ref({ subjects: [], subjectAverages: [], optionAverage: null, studentRecord: null })
const s4SemesterId = ref(null)
const semesterSubjects = ref([])
const backendSemesterSummary = ref(null)
const backendS4OptionSummary = ref(null)
const semestersCache = ref(null)
const s4AverageOverride = ref(null)

function readStoredS4Option() {
  if (typeof window === 'undefined') return ''
  try {
    return window.localStorage.getItem(lastS4OptionStorageKey) || ''
  } catch (e) {
    console.warn('Unable to read stored S4 option', e)
    return ''
  }
}

function persistS4Option(value) {
  if (typeof window === 'undefined') return
  try {
    if (value == null || value === '') {
      window.localStorage.removeItem(lastS4OptionStorageKey)
    } else {
      window.localStorage.setItem(lastS4OptionStorageKey, String(value))
    }
  } catch (e) {
    console.warn('Unable to persist S4 option', e)
  }
}

const preferredS4Option = ref(selectedS4Option.value || storedS4Option || '')

function formatGrade(g) {
  return g == null ? '—' : Number(g).toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 2 })
}

function formatCreditsValue(value) {
  if (value == null || Number.isNaN(Number(value))) return '—'
  return Number(value).toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 2 })
}

const average = computed(() => {
  if (!notes.value || notes.value.length === 0) return null
  const nums = notes.value.map(n => Number(n.grade)).filter(n => !isNaN(n))
  if (nums.length === 0) return null
  const sum = nums.reduce((s, v) => s + v, 0)
  return sum / nums.length
})

const averageDisplay = computed(() => average.value == null ? '—' : average.value.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }))
const isS4View = computed(() => (semester || '').toString().toUpperCase() === 'S4')
const currentS4OptionLabel = computed(() => {
  const opt = s4Options.value.find(o => String(o.id) === String(selectedS4Option.value))
  return opt ? opt.label : ''
})

const displayedAverages = computed(() => {
  const base = averages.value || {}
  const result = { ...base }
  
  // Override S4 with computed value if viewing S4
  if (s4AverageOverride.value != null) {
    const override = Number(s4AverageOverride.value)
    if (!Number.isNaN(override)) {
      result.S4 = Math.round(override * 100) / 100
    }
  }
  
  // Override current semester with locally computed average
  const currentSemester = semesterLabel.value || semester
  if (currentSemester && semesterDecision.value?.average != null) {
    result[currentSemester] = Math.round(semesterDecision.value.average * 100) / 100
  }
  
  return result
})

const semesterSubjectCreditsMap = computed(() => {
  const map = new Map()
  for (const subject of semesterSubjects.value || []) {
    const subjectId = subject.subjectId ?? subject.id
    if (subjectId == null) continue
    const credits = resolveDecisionCredits(subject.credits ?? subject.credit ?? subject.coefficient)
    map.set(`id:${Number(subjectId)}`, credits)
    map.set(`id:${String(subjectId)}`, credits)
    const normalizedCode = normalizeSubjectCode(subject.subjectCode || subject.code)
    if (normalizedCode) map.set(`code:${normalizedCode}`, credits)
  }
  return map
})

const semesterCreditsTotal = computed(() => {
  if (!notes.value || !notes.value.length) return 0
  return notes.value.reduce((sum, note) => sum + getNoteCreditContribution(note), 0)
})

const semesterCreditsTotalDisplay = computed(() => formatCreditsValue(semesterCreditsTotal.value))

const s4OptionCreditsTotal = computed(() => {
  if (!isS4View.value || !selectedS4Option.value) return 0
  if (!visibleS4Subjects.value.length) return 0
  return visibleS4Subjects.value.reduce((sum, subject) => sum + getOptionSubjectCreditContribution(subject), 0)
})

const s4OptionCreditsDisplay = computed(() => formatCreditsValue(s4OptionCreditsTotal.value))

const selectedS4OptionalSubjects = computed(() => {
  const record = s4OptionData.value.studentRecord
  const map = new Map()
  if (!record || !Array.isArray(record.grades)) return map
  for (const grade of record.grades) {
    if (!grade.optionalGroupId) continue
    const gradeVal = resolveEntryRawGrade(grade)
    if (gradeVal == null) continue
    const existing = map.get(grade.optionalGroupId)
    if (!existing || gradeVal > existing.value) {
      map.set(grade.optionalGroupId, {
        subjectId: grade.subjectId,
        value: gradeVal,
        bestOptional: grade.bestOptionalGrade != null ? Number(grade.bestOptionalGrade) : gradeVal
      })
    }
  }
  return map
})

const filteredS4Subjects = computed(() => {
  const picks = selectedS4OptionalSubjects.value
  const subjects = s4OptionData.value.subjects || []
  return subjects
    .filter(sub => {
      if (!sub.optionalGroupId) return true
      const pick = picks.get(sub.optionalGroupId)
      if (!pick) return true
      return isSameSubject(pick.subjectId, sub.subjectId)
    })
    .sort((a, b) => (a.subjectCode || '').localeCompare(b.subjectCode || ''))
})

const visibleS4Subjects = computed(() => {
  if (filteredS4Subjects.value.length) return filteredS4Subjects.value
  const record = s4OptionData.value.studentRecord
  if (!record || !Array.isArray(record.grades)) return []
  const picks = selectedS4OptionalSubjects.value
  const byKey = new Map()
  record.grades
    .filter(grade => {
      if (!grade?.optionalGroupId) return true
      const pick = picks.get(grade.optionalGroupId)
      if (!pick) return false
      return isSameSubject(pick.subjectId, grade.subjectId)
    })
    .forEach((grade, index) => {
      const key = grade.subjectId != null ? `id:${grade.subjectId}` : `idx:${index}`
      if (byKey.has(key)) return
      byKey.set(key, {
        subjectId: grade.subjectId ?? `tmp-${index}`,
        subjectCode: grade.subjectCode || `MAT-${index + 1}`,
        subjectTitle: grade.subjectTitle || grade.subjectName || grade.subjectCode || `Matière ${index + 1}`,
        optionalGroupId: grade.optionalGroupId || null
      })
    })
  return Array.from(byKey.values())
})

const fallbackSemesterDecision = computed(() => computeDecisionSummary(notes.value || [], semesterSubjectCreditsMap.value))
const semesterDecision = computed(() => backendSemesterSummary.value || fallbackSemesterDecision.value)

const computedS4OptionDecision = computed(() => {
  if (!isS4View.value) return null
  if (!selectedS4Option.value) return null
  const entries = filteredS4Subjects.value.map(sub => ({
    subjectId: sub.subjectId,
    rawGrade: getStudentOptionGrade(sub)
  }))
  if (!entries.length) return null
  return computeDecisionSummary(entries, semesterSubjectCreditsMap.value)
})

const s4DecisionSummary = computed(() => {
  if (!isS4View.value) return semesterDecision.value
  // Always use locally computed decision for S4 option view
  if (selectedS4Option.value && computedS4OptionDecision.value) {
    return computedS4OptionDecision.value
  }
  return computedS4OptionDecision.value || backendS4OptionSummary.value || semesterDecision.value
})

const s4DecisionLabel = computed(() => {
  if (!isS4View.value) return '—'
  return s4DecisionSummary.value?.decision || '—'
})

const s4DecisionClass = computed(() => {
  if (!isS4View.value) return ''
  return s4DecisionLabel.value === 'Admis' ? 'text-green-700' : 'text-red-700'
})

const s4StudentAverage = computed(() => {
  if (!isS4View.value) return null
  return s4DecisionSummary.value?.average ?? null
})

const s4StudentAverageDisplay = computed(() => {
  if (s4StudentAverage.value == null) return '—'
  return Number(s4StudentAverage.value).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
})

const semesterAverageDisplay = computed(() => {
  const avg = semesterDecision.value.average
  if (avg == null) return '—'
  return avg.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
})

async function load() {
  loading.value = true
  try {
    const levelParam = route.params.level || route.query.level
    const resp = levelParam
      ? await api.get(`/students/${studentId}/notes/levels/${levelParam}`)
      : await api.get(`/students/${studentId}/notes/semesters/${semester}`)
    // handle ApiResponse wrapper or raw
    const payload = resp.data && resp.data.data ? resp.data.data : resp.data
    // payload can be SemesterNotesResponse (with .notes) or LevelNotesResponse (with .semesters)
    let raw = []
    const isLevelView = !!levelParam
    if (payload?.notes) {
      raw = payload.notes
    } else if (payload?.semesters) {
      // when viewing by level we want to show all semester blocks (S1+S2), not only the latest per subject
      raw = payload.semesters.flatMap(s => (s.notes || []).map(n => ({ ...n, __semesterLabel: s.semesterLabel || s.semesterCode })))
    }

    if (isLevelView) {
      // do not dedupe across semesters for level view — show all notes (S1 and S2)
      notes.value = raw
      backendSemesterSummary.value = null
    } else {
      // keep only the latest note per subject (by sessionDate). If sessionDate missing, use gradeId as fallback
      const deduped = dedupeLatestBySubject(raw)
      notes.value = deduped
      backendSemesterSummary.value = extractDecisionSummary(payload)
    }

    semesterLabel.value = payload?.semesterLabel ?? payload?.semesterCode ?? payload?.levelLabel ?? ''
    await loadSemesterSubjectsMetadata(semesterLabel.value || semester)
  } catch (e) {
    console.error('Failed to load student notes:', e)
    notes.value = []
    backendSemesterSummary.value = null
    // try to extract friendly message
    error.value = e?.response?.data?.error?.message || e?.response?.data || e?.message || 'Erreur lors du chargement des notes.'
    // if unauthorized, suggest login
    if (e?.response?.status === 401) {
      error.value = 'Authentification requise. Veuillez vous connecter.'
    }
  } finally {
    loading.value = false
  }
}

async function loadAverages() {
  loadingAverages.value = true
  try {
    const resp = await api.get(`/students/${studentId}/averages`)
    const payload = resp.data && resp.data.data ? resp.data.data : resp.data
    averages.value = payload || {}
  } catch (e) {
    console.error('Failed to load averages:', e)
    averages.value = {}
  } finally {
    loadingAverages.value = false
  }
}

onMounted(load)
onMounted(loadAverages)
onMounted(async () => {
  if (!isS4View.value || !selectedS4Option.value) return
  try {
    await ensureS4Options()
    await loadS4OptionDetails()
  } catch (e) {
    console.error('Unable to preload S4 option details', e)
  }
})
watch(() => isS4View.value, (val) => {
  if (val) {
    ensureS4Options()
    setTimeout(() => {
      if (!selectedS4Option.value) {
        const devOption = s4Options.value.find(opt => String(opt.label || '').toLowerCase().includes('développement'))
        if (devOption) selectedS4Option.value = devOption.id
      }
    }, 0)
  } else {
    selectedS4Option.value = ''
    resetS4OptionPanel()
  }
}, { immediate: true })

watch(s4Options, (list) => {
  if (!isS4View.value || !list.length) return
  if (selectedS4Option.value) return
  const storedId = preferredS4Option.value
  const storedMatch = storedId ? list.find(opt => String(opt.id) === String(storedId)) : null
  if (storedMatch) {
    selectedS4Option.value = storedMatch.id
    return
  }
  const devOption = list.find(opt => String(opt.label || '').toLowerCase().includes('développement'))
  const fallback = devOption || list[0]
  if (fallback) selectedS4Option.value = fallback.id
})
watch(() => route.query.option, (val) => {
  if (!isS4View.value) return
  const normalized = val ? String(val) : ''
  if (!normalized) {
    if (selectedS4Option.value) selectedS4Option.value = ''
    return
  }
  if (String(selectedS4Option.value) !== normalized) {
    selectedS4Option.value = normalized
  }
}, { immediate: true })

watch(selectedS4Option, (val) => {
  if (!isS4View.value) return
  if (val) {
    preferredS4Option.value = String(val)
    persistS4Option(val)
    loadS4OptionDetails()
    const nextQuery = { ...route.query, option: val }
    router.replace({ query: nextQuery })
  } else {
    resetS4OptionPanel()
    preferredS4Option.value = ''
    persistS4Option('')
    if (route.query.option) {
      const nextQuery = { ...route.query }
      delete nextQuery.option
      router.replace({ query: nextQuery })
    }
  }
})

watch(
  () => ({ avg: s4StudentAverage.value, isS4: isS4View.value, option: selectedS4Option.value }),
  ({ avg, isS4, option }) => {
    if (!isS4 || !option || avg == null || Number.isNaN(Number(avg))) {
      s4AverageOverride.value = null
      return
    }
    s4AverageOverride.value = Number(avg)
  },
  { immediate: true }
)

function dedupeLatestBySubject(list) {
  const map = new Map()
  for (const item of list) {
      // prefer numeric subjectId as dedupe key when available (more stable than codes)
      const key = (item.subjectId !== undefined && item.subjectId !== null) ? String(item.subjectId) : (item.subjectCode ?? item.subjectTitle ?? String(item.gradeId))
    const existing = map.get(key)
    // parse dates; fallback to gradeId numeric comparison
    const d1 = item.sessionDate ? new Date(item.sessionDate) : null
    const d2 = existing && existing.sessionDate ? new Date(existing.sessionDate) : null
    if (!existing) {
      map.set(key, item)
    } else {
      if (d1 && d2) {
        if (d1 > d2) map.set(key, item)
      } else if (d1 && !d2) {
        map.set(key, item)
      } else if (!d1 && !d2) {
        // use gradeId as fallback (assume larger = newer)
        if ((item.gradeId || 0) > (existing.gradeId || 0)) map.set(key, item)
      }
    }
  }
  // return array sorted by subject code or title
  return Array.from(map.values()).sort((a, b) => (a.subjectCode || a.subjectTitle || '').localeCompare(b.subjectCode || b.subjectTitle || ''))
}

function resetS4OptionPanel() {
  s4OptionData.value = { subjects: [], subjectAverages: [], optionAverage: null, studentRecord: null }
  s4OptionError.value = null
  loadingS4Option.value = false
  backendS4OptionSummary.value = null
}

async function ensureS4Options() {
  if (s4Options.value.length || loadingS4Options.value) return
  loadingS4Options.value = true
  try {
    const resp = await notesService.getOptions()
    // option id 1 = tronc commun, ignorer pour S4
    s4Options.value = (resp || []).filter(opt => opt.id !== 1)
    if (isS4View.value && !selectedS4Option.value) {
      const devOption = s4Options.value.find(opt => String(opt.label || '').toLowerCase().includes('développement'))
      if (devOption) selectedS4Option.value = devOption.id
    }
  } catch (e) {
    console.error('Unable to load S4 options', e)
    s4Options.value = []
  } finally {
    loadingS4Options.value = false
  }
}

async function resolveS4SemesterId() {
  if (s4SemesterId.value) return s4SemesterId.value
  try {
    const list = await getSemestersCache()
    const match = (list || []).find(s => {
      const label = (s.label || '').toString().toUpperCase()
      const code = (s.code || '').toString().toUpperCase()
      return label.includes('S4') || code === 'S4' || label.includes('SEMESTRE 4')
    })
    if (match?.id) {
      s4SemesterId.value = match.id
      return match.id
    }
  } catch (e) {
    console.error('Unable to resolve semester list', e)
  }
  return null
}

async function getSemestersCache() {
  if (semestersCache.value) return semestersCache.value
  try {
    const resp = await semesterService.getSemesters()
    semestersCache.value = resp && resp.data ? resp.data : resp
  } catch (e) {
    console.error('Unable to load semesters cache', e)
    semestersCache.value = []
  }
  return semestersCache.value
}

async function resolveSemesterIdByLabel(label) {
  if (!label) return null
  const normalized = label.toString().trim().toUpperCase()
  const list = await getSemestersCache()
  const match = (list || []).find(s => {
    const sl = (s.label || '').toString().trim().toUpperCase()
    if (sl === normalized) return true
    if (sl.includes(normalized) || normalized.includes(sl)) return true
    const digitsA = sl.replace(/[^0-9]/g, '')
    const digitsB = normalized.replace(/[^0-9]/g, '')
    return digitsA && digitsB && digitsA === digitsB
  })
  return match?.id || null
}

async function loadSemesterSubjectsMetadata(label) {
  if (!label) {
    semesterSubjects.value = []
    return
  }
  try {
    const semId = await resolveSemesterIdByLabel(label)
    if (!semId) {
      semesterSubjects.value = []
      return
    }
    const data = await notesService.getSubjectsCreditsBySemester(semId)
    semesterSubjects.value = data || []
  } catch (e) {
    console.error('Unable to load semester credits', e)
    semesterSubjects.value = []
  }
}

async function loadS4OptionDetails() {
  if (!selectedS4Option.value) return
  loadingS4Option.value = true
  s4OptionError.value = null
  try {
    const semId = await resolveS4SemesterId()
    if (!semId) throw new Error('Semestre S4 introuvable')
    const optionId = Number(selectedS4Option.value)
    const payload = await notesService.getOptionSubjects(semId, Number.isNaN(optionId) ? selectedS4Option.value : optionId)
    const studentRecord = (payload?.students || []).find(st => Number(st.studentId) === Number(studentId)) || null
    s4OptionData.value = {
      subjects: payload?.subjects || [],
      subjectAverages: payload?.subjectAverages || [],
      optionAverage: payload?.optionAverage ?? null,
      studentRecord
    }
    const backendSummary = (studentRecord && extractDecisionSummary(studentRecord)) || extractDecisionSummary(payload)
    if (backendSummary) {
      backendS4OptionSummary.value = backendSummary
      if (isS4View.value) backendSemesterSummary.value = backendSummary
    }
  } catch (e) {
    console.error('Failed to load S4 option details', e)
    s4OptionError.value = "Impossible de charger les notes pour cette option."
    s4OptionData.value = { subjects: [], subjectAverages: [], optionAverage: null, studentRecord: null }
  } finally {
    loadingS4Option.value = false
  }
}

function getStudentOptionGrade(subject) {
  const subjectId = subject?.subjectId ?? subject
  const record = s4OptionData.value.studentRecord
  if (!record || !Array.isArray(record.grades)) return 0
  const entry = record.grades.find(g => subjectsMatch(g, subject))
  if (!entry) return 0
  if (entry.optionalGroupId) {
    const pick = selectedS4OptionalSubjects.value.get(entry.optionalGroupId)
    if (!pick || !isSameSubject(pick.subjectId, subjectId)) return 0
    if (pick.bestOptional != null) return Number(pick.bestOptional)
  }
  const direct = resolveEntryRawGrade(entry)
  if (direct != null) return direct
  if (entry.bestGrade != null) return Number(entry.bestGrade)
  return 0
}

function subjectsMatch(grade, subject) {
  if (!grade || !subject) return false
  const targetId = subject.subjectId ?? subject
  if (grade.subjectId != null && targetId != null && isSameSubject(grade.subjectId, targetId)) return true
  const targetCode = subject.subjectCode || subject.code
  if (targetCode && grade.subjectCode) {
    return String(grade.subjectCode).trim().toUpperCase() === String(targetCode).trim().toUpperCase()
  }
  if (subject.subjectTitle && grade.subjectTitle) {
    return String(grade.subjectTitle).trim().toUpperCase() === String(subject.subjectTitle).trim().toUpperCase()
  }
  return false
}

function isSameSubject(a, b) {
  if (a == null || b == null) return false
  const numA = Number(a)
  const numB = Number(b)
  if (Number.isFinite(numA) && Number.isFinite(numB)) return numA === numB
  return String(a).trim() === String(b).trim()
}

function getSubjectOptionAverage(subjectId) {
  const entry = (s4OptionData.value.subjectAverages || []).find(avg => Number(avg.subjectId) === Number(subjectId))
  return entry && entry.average != null ? Number(entry.average) : null
}

function resolveEntryRawGrade(entry) {
  if (!entry) return null
  if (entry.rawGrade != null) return Number(entry.rawGrade)
  if (entry.grade != null) return Number(entry.grade)
  if (entry.bestGrade != null) return Number(entry.bestGrade)
  return 0
}

function getSubjectCredits(subject) {
  const credits = resolveSubjectCredits(subject)
  return credits == null ? null : Number(credits)
}

function formatSubjectCredits(subject) {
  const credits = getSubjectCredits(subject)
  if (credits == null || Number.isNaN(credits)) return '—'
  return formatCreditsValue(credits)
}

function getSubjectAppreciation(note) {
  const gradeVal = Number(note.grade)
  if (Number.isNaN(gradeVal)) return '—'
  if (gradeVal < 6) return 'Ajournée'
  if (gradeVal < 10) {
    return semesterDecision.value.compensated.has(note.subjectId) && semesterDecision.value.decision === 'Admis'
      ? 'Compensée'
      : 'Ajournée'
  }
  if (gradeVal < 12) return 'Passable'
  if (gradeVal < 14) return 'Assez Bien'
  if (gradeVal < 16) return 'Bien'
  return 'Très Bien'
}

function getNoteCreditsValue(note) {
  const credits = resolveSubjectCredits(note)
  if (credits == null) return null
  return Number(credits)
}

function getNoteCreditContribution(note) {
  const credits = getNoteCreditsValue(note)
  if (!Number.isFinite(credits)) return 0
  return isNoteAjounee(note) ? 0 : credits
}

function isNoteAjounee(note) {
  return getSubjectAppreciation(note) === 'Ajournée'
}

function resolveSubjectCredits(subject) {
  const map = semesterSubjectCreditsMap.value
  if (!map || !map.size) return null
  const subjectId = typeof subject === 'object' ? (subject?.subjectId ?? subject?.id) : subject
  if (subjectId != null) {
    const numericKey = `id:${Number(subjectId)}`
    if (map.has(numericKey)) return map.get(numericKey)
    const rawKey = `id:${String(subjectId)}`
    if (map.has(rawKey)) return map.get(rawKey)
  }
  const subjectCode = typeof subject === 'object' ? (subject.subjectCode || subject.code) : null
  const normalizedCode = normalizeSubjectCode(subjectCode)
  if (normalizedCode) {
    const codeKey = `code:${normalizedCode}`
    if (map.has(codeKey)) return map.get(codeKey)
  }
  return null
}

function normalizeSubjectCode(code) {
  if (!code) return ''
  return String(code).trim().toUpperCase()
}

function extractDecisionGrade(entry) {
  if (!entry) return null
  if (entry.rawGrade != null) return Number(entry.rawGrade)
  if (entry.bestOptionalGrade != null) return Number(entry.bestOptionalGrade)
  if (entry.grade != null) return Number(entry.grade)
  if (entry.bestGrade != null) return Number(entry.bestGrade)
  // Don't fallback to generic 'value' or cohort average
  if (typeof entry === 'number') return entry
  return null
}

function computeDecisionSummary(entries, creditsMap = new Map()) {
  let totalCredits = 0
  let weightedSum = 0
  let belowSix = 0
  let belowTen = 0
  const compensable = []

  for (const entry of entries || []) {
    const gradeVal = extractDecisionGrade(entry)
    // Treat null/NaN as 0 instead of skipping
    const effectiveGrade = (gradeVal != null && !Number.isNaN(gradeVal)) ? gradeVal : 0
    // Try to get credits with the prefixed key format used by semesterSubjectCreditsMap
    const subjectId = entry.subjectId
    let credits = resolveDecisionCredits(creditsMap.get(`id:${Number(subjectId)}`))
    if (credits === 1 && subjectId != null) {
      // Fallback to string key
      credits = resolveDecisionCredits(creditsMap.get(`id:${String(subjectId)}`))
    }
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

  const average = totalCredits > 0 ? Math.round((weightedSum / totalCredits) * 100) / 100 : null
  const summary = {
    average,
    decision: 'Admis',
    compensated: new Set()
  }

  if (average == null || average < 10 || belowSix > 0 || belowTen >= 3) {
    summary.decision = 'Ajourné'
    return summary
  }

  for (const subjectId of compensable.slice(0, 2)) {
    summary.compensated.add(subjectId)
  }

  return summary
}

function resolveDecisionCredits(value) {
  const num = Number(value)
  if (Number.isFinite(num) && num > 0) return num
  return 1
}

function getOptionSubjectCreditContribution(subject) {
  const credits = resolveSubjectCredits(subject)
  if (credits == null || Number.isNaN(Number(credits))) return 0
  return isOptionSubjectAjounee(subject) ? 0 : Number(credits)
}

function isOptionSubjectAjounee(subject) {
  const grade = getStudentOptionGrade(subject)
  if (!Number.isFinite(grade)) return true
  if (grade >= 10) return false
  const compensated = getS4CompensatedSet()
  const subjectId = subject?.subjectId ?? subject?.id
  if (subjectId != null && compensated.has(Number(subjectId))) return false
  return true
}

function getS4CompensatedSet() {
  const summary = s4DecisionSummary.value
  if (!summary?.compensated) return new Set()
  if (summary.compensated instanceof Set) return summary.compensated
  return new Set(Array.from(summary.compensated))
}
</script>

<style scoped>
table { border-spacing: 0; }
</style>
