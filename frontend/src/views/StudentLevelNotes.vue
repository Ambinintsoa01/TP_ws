<template>
  <div class="p-6">
    <button class="mb-4 text-sm text-gray-600 hover:underline" @click="$router.back()">← Retour</button>
    <h1 class="text-2xl font-bold mb-4">Notes — {{ levelLabel || levelParam }}</h1>
    <p class="text-sm text-gray-500 mb-2">Étudiant ID: {{ studentId }} — {{ studentName }}</p>

    <div v-if="isL2View" class="mb-4 p-4 bg-indigo-50 rounded border border-indigo-100">
      <div class="flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
        <div>
          <h2 class="text-lg font-semibold text-indigo-900">Option du semestre 4</h2>
          <p class="text-sm text-indigo-700">Choisissez une option pour afficher les notes ciblées de cet étudiant.</p>
        </div>
        <div class="flex items-center gap-2">
          <label for="levelS4OptionSelect" class="text-sm font-medium text-gray-700">Option</label>
          <select
            id="levelS4OptionSelect"
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

    <div v-if="loading" class="py-6">Chargement des notes...</div>

    <div v-else>
      <div v-if="error" class="mb-4 p-3 bg-red-100 text-red-800 rounded">{{ error }}</div>

      <div v-if="semesters.length === 0" class="text-gray-500">Aucune note trouvée pour ce niveau.</div>

      <div
        v-for="block in semesters"
        :key="block.semesterCode"
        class="mb-6"
        v-if="!shouldHideSemesterBlock(block)"
      >
        <h2 class="text-lg font-semibold mb-2">{{ block.semesterLabel || block.semesterCode }}</h2>
        <div class="semester-summary" v-if="block.summary">
          <div class="summary-pill">Moyenne: {{ getBlockAverage(block) }}</div>
          <div class="summary-pill" :class="block.summary.decision === 'Admis' ? 'summary-pill-success' : 'summary-pill-danger'">
            Décision: {{ block.summary.decision }}
          </div>
        </div>
        <table class="w-full table-auto border-collapse mb-2">
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
            <tr v-for="note in block.notes" :key="note.gradeId" class="border-t">
              <td class="p-2">{{ note.subjectCode }}</td>
              <td class="p-2">{{ note.subjectTitle }}</td>
              <td class="p-2">{{ note.sessionDate || '—' }}</td>
              <td class="p-2 text-right">{{ getNoteCredits(block, note) }}</td>
              <td class="p-2 text-right font-semibold">{{ formatGrade(resolveDisplayGrade(note)) }}</td>
              <td class="p-2 text-right">{{ getNoteAppreciation(block, note) }}</td>
            </tr>
          </tbody>
          <tfoot>
            <tr class="border-t">
              <td colspan="4" class="p-2 font-bold">Crédits capitalisés</td>
              <td class="p-2 text-right font-bold">{{ getBlockCreditsTotalDisplay(block) }}</td>
              <td></td>
            </tr>
            <tr class="border-t">
              <td colspan="4" class="p-2 font-bold">Moyenne {{ block.semesterLabel || '' }}</td>
              <td class="p-2 text-right font-bold">{{ getBlockAverage(block) }}</td>
              <td></td>
            </tr>
          </tfoot>
        </table>
      </div>

      <div v-if="isL2View && selectedS4Option" class="mb-6 p-4 bg-white rounded border border-gray-100 shadow-sm">
        <div class="flex flex-col gap-2 md:flex-row md:items-center md:justify-between mb-3">
          <div>
            <h3 class="text-lg font-semibold">Notes S4 — {{ currentS4OptionLabel }}</h3>
            <p class="text-sm text-gray-500">Affichage limité à l'étudiant sélectionné.</p>
          </div>
          <div class="text-sm text-gray-600 flex flex-wrap gap-4">
            <span>Moyenne option: <strong>{{ formatGrade(s4StudentAverage) }}</strong></span>
            <span v-if="hasS4Decision">Décision S4: <strong :class="s4DecisionClass">{{ s4DecisionLabel }}</strong></span>
          </div>
        </div>
        <div v-if="loadingS4Option" class="text-sm text-gray-500">Chargement des notes de l'option…</div>
        <div v-else-if="s4OptionError" class="text-sm text-red-600">{{ s4OptionError }}</div>
        <div v-else-if="s4OptionData.studentRecord && filteredS4Subjects.length" class="overflow-x-auto">
          <table class="w-full table-auto border-collapse text-sm">
            <thead>
              <tr class="bg-gray-50">
                <th class="p-2 text-left">Matière</th>
                <th class="p-2 text-right">Crédits</th>
                <th class="p-2 text-right">Note étudiant</th>
                <th class="p-2 text-right">Moyenne option</th>
                <th class="p-2 text-right">Appréciation</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="sub in filteredS4Subjects" :key="sub.subjectId" class="border-t">
                <td class="p-2">
                  <div class="font-medium text-gray-900">{{ sub.subjectCode }}</div>
                  <div class="text-xs text-gray-500">{{ sub.subjectTitle }}</div>
                </td>
                <td class="p-2 text-right">{{ getOptionSubjectCreditsDisplay(sub.subjectId) }}</td>
                <td class="p-2 text-right font-semibold">{{ formatGrade(getStudentOptionGrade(sub.subjectId)) }}</td>
                <td class="p-2 text-right text-gray-600">{{ formatGrade(getSubjectOptionAverage(sub.subjectId)) }}</td>
                <td class="p-2 text-right">{{ getOptionSubjectAppreciation(sub.subjectId) }}</td>
              </tr>
            </tbody>
            <tfoot>
              <tr class="border-t">
                <td colspan="4" class="p-2 font-bold">Crédits capitalisés</td>
                <td class="p-2 text-right font-bold">{{ levelS4OptionCreditsDisplay }}</td>
              </tr>
            </tfoot>
          </table>
        </div>
        <div v-else class="text-sm text-gray-500">Cet étudiant n'a pas encore de notes pour cette option.</div>
      </div>
      <div v-else-if="isL2View" class="mb-6 text-sm text-gray-500">Sélectionnez une option pour consulter les notes de S4.</div>

      <div v-if="levelSummaryRows.length" class="mb-6 p-4 bg-white rounded border border-gray-100 shadow-sm">
        <h3 class="text-lg font-semibold mb-3">Résumé du niveau</h3>
        <div class="overflow-x-auto">
          <table class="w-full table-auto text-sm">
            <thead>
              <tr class="bg-gray-50">
                <th class="p-2 text-left">Semestre</th>
                <th class="p-2 text-right">Moyenne</th>
                <th class="p-2 text-right">Décision</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in levelSummaryRows" :key="row.label" class="border-t">
                <td class="p-2">{{ row.label }}</td>
                <td class="p-2 text-right font-semibold">{{ formatGrade(row.average) }}</td>
                <td class="p-2 text-right" :class="(row.decision === 'Admis' ? 'text-green-700' : row.decision ? 'text-red-700' : '')">{{ row.decision || '—' }}</td>
              </tr>
              <tr class="border-t font-semibold">
                <td class="p-2">Moyenne générale</td>
                <td class="p-2 text-right">{{ levelDecisionDisplay }}</td>
                <td class="p-2 text-right" :class="levelDecisionClass">{{ levelDecision?.decision || '—' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import api from '../services/api/api.js'
import notesService from '../services/notesService'
import semesterService from '../services/semesterService'
import { extractDecisionSummary } from '../utils/decisionSummary'

const route = useRoute()
const studentId = Number(route.params.id || route.query.id)
const levelParam = route.params.level || route.query.level || ''
const studentName = route.query.name || ''

const semesters = ref([])
const loading = ref(true)
const error = ref(null)
const levelLabel = ref('')
const semestersCache = ref(null)
const normalizedLevelParam = (levelParam || '').toString().trim().toUpperCase()
const includeFullRecord = computed(() => {
  const primaryFlag = (route.query.view || route.query.scope || '').toString().toLowerCase()
  if (primaryFlag === 'full' || primaryFlag === 'all' || primaryFlag === 'complete') return true
  const toggles = [route.query.full, route.query.all, route.query.complete]
  return toggles.some(val => {
    if (val === undefined) return false
    if (val === true || val === 1) return true
    if (typeof val === 'string') {
      const normalized = val.toLowerCase()
      return normalized === '1' || normalized === 'true'
    }
    return false
  })
})
const creditsCache = ref(new Map())
const s4Options = ref([])
const selectedS4Option = ref('')
const loadingS4Options = ref(false)
const loadingS4Option = ref(false)
const s4OptionError = ref('')
const s4OptionData = ref({ subjects: [], subjectAverages: [], optionAverage: null, studentRecord: null })
const s4SemesterId = ref(null)
const s4SemesterSummary = ref(null)

const displayedS4Block = computed(() => {
  if (!s4SemesterSummary.value) return null
  if (isL2View.value && s4OptionDecision.value) {
    return {
      ...s4SemesterSummary.value,
      summary: s4OptionDecision.value
    }
  }
  return s4SemesterSummary.value
})

const levelSummaryRows = computed(() => {
  const rows = semesters.value.map(block => ({
    label: block.semesterLabel || block.semesterCode,
    average: block.summary?.average ?? null,
    decision: block.summary?.decision ?? null
  }))
  const s4Block = isL2View.value ? displayedS4Block.value : null
  if (s4Block) {
    rows.push({
      label: s4Block.semesterLabel || s4Block.semesterCode,
      average: s4Block.summary?.average ?? null,
      decision: s4Block.summary?.decision ?? null
    })
  }
  return rows
})

const levelDecision = computed(() => computeLevelDecisionBlocks(levelSummarySourceBlocks()))
const levelDecisionDisplay = computed(() => {
  const avg = levelDecision.value?.average
  return avg == null ? '—' : avg.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
})
const levelDecisionClass = computed(() => {
  const decision = levelDecision.value?.decision
  if (!decision) return ''
  return decision === 'Admis' ? 'text-green-700' : 'text-red-700'
})

const selectedS4OptionalSubjects = computed(() => {
  const map = new Map()
  const record = s4OptionData.value.studentRecord
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
  return [...subjects]
    .filter(sub => {
      const optGroupId = sub.optionalGroupId
      if (!optGroupId) return true
      const pick = picks.get(optGroupId)
      if (!pick) return true
      return Number(pick.subjectId) === Number(sub.subjectId)
    })
    .sort((a, b) => (a.subjectCode || '').localeCompare(b.subjectCode || ''))
})

const currentS4OptionLabel = computed(() => {
  if (!selectedS4Option.value) return ''
  const match = s4Options.value.find(opt => String(opt.id) === String(selectedS4Option.value))
  return match?.label || 'Option'
})

const studentOptionAverage = computed(() => {
  if (!s4OptionData.value.studentRecord) return null
  const values = filteredS4Subjects.value
    .map(sub => getStudentOptionGrade(sub.subjectId))
    .filter(v => v != null && !Number.isNaN(v))
  if (!values.length) return null
  const sum = values.reduce((acc, v) => acc + v, 0)
  return Math.round((sum / values.length) * 100) / 100
})

const studentOptionAverageDisplay = computed(() => {
  const avg = studentOptionAverage.value
  if (avg == null) return '—'
  return avg.toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 2 })
})

const levelS4OptionCreditsTotal = computed(() => {
  if (!isL2View.value || !selectedS4Option.value) return 0
  if (!filteredS4Subjects.value.length) return 0
  return filteredS4Subjects.value.reduce((sum, sub) => sum + getOptionSubjectCreditContribution(sub.subjectId), 0)
})

const levelS4OptionCreditsDisplay = computed(() => formatCreditsValue(levelS4OptionCreditsTotal.value))

const s4OptionSubjectCreditsMap = computed(() => {
  const map = new Map()
  // Get credits from the cache (already fetched from /grades/subjects/credits/semester/4)
  const s4CreditsList = creditsCache.value.get('S4') || []
  for (const subject of s4CreditsList) {
    const subjectId = subject.subjectId ?? subject.id
    if (subjectId == null) continue
    const credits = resolveCreditsValue(subject.credits ?? subject.credit ?? subject.coefficient)
    map.set(`id:${Number(subjectId)}`, credits)
    map.set(`id:${String(subjectId)}`, credits)
  }
  return map
})

const s4OptionDecision = computed(() => {
  if (!isL2View.value) return null
  if (!selectedS4Option.value) return s4SemesterSummary.value?.summary || null
  const creditsMap = s4OptionSubjectCreditsMap.value
  const entries = filteredS4Subjects.value.map(sub => ({
    subjectId: sub.subjectId,
    rawGrade: getStudentOptionGrade(sub.subjectId)
  }))
  if (!entries.length) return s4SemesterSummary.value?.summary || null
  return computeDecision(entries, creditsMap)
})

const hasS4Decision = computed(() => {
  if (!isL2View.value) return false
  return !!(s4OptionDecision.value?.decision)
})

const s4DecisionLabel = computed(() => {
  if (!hasS4Decision.value) return '—'
  return s4OptionDecision.value.decision
})

const s4DecisionClass = computed(() => {
  if (!hasS4Decision.value) return ''
  return s4DecisionLabel.value === 'Admis' ? 'text-green-700' : 'text-red-700'
})

const s4StudentAverage = computed(() => {
  if (!isL2View.value) return null
  if (!selectedS4Option.value) return s4OptionData.value.optionAverage
  return s4OptionDecision.value?.average ?? s4OptionData.value.optionAverage
})

function formatGrade(g) {
  return g == null ? '—' : Number(g).toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 2 })
}

function formatCreditsValue(value) {
  if (value == null || Number.isNaN(Number(value))) return '—'
  return Number(value).toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 2 })
}

function resolveDisplayGrade(note) {
  if (!note) return null
  if (note.bestOptionalGrade != null) return Number(note.bestOptionalGrade)
  if (note.rawGrade != null) return Number(note.rawGrade)
  if (note.bestGrade != null) return Number(note.bestGrade)
  if (note.grade != null) return Number(note.grade)
  return 0
}

function resolveEntryRawGrade(entry) {
  if (!entry) return null
  if (entry.rawGrade != null) return Number(entry.rawGrade)
  if (entry.grade != null) return Number(entry.grade)
  if (entry.bestGrade != null) return Number(entry.bestGrade)
  return 0
}

function dedupeBySubject(list) {
  const map = new Map()
  for (const item of list) {
    const key = (item.subjectId !== undefined && item.subjectId !== null) ? String(item.subjectId) : (item.subjectCode ?? item.subjectTitle ?? String(item.gradeId))
    const existing = map.get(key)
    if (!existing) map.set(key, item)
    else {
      const d1 = item.sessionDate ? new Date(item.sessionDate) : null
      const d2 = existing.sessionDate ? new Date(existing.sessionDate) : null
      if (d1 && d2) {
        if (d1 > d2) map.set(key, item)
      } else if (d1 && !d2) {
        map.set(key, item)
      } else if (!d1 && !d2) {
        if ((item.gradeId || 0) > (existing.gradeId || 0)) map.set(key, item)
      }
    }
  }
  return Array.from(map.values()).sort((a, b) => (a.subjectCode || a.subjectTitle || '').localeCompare(b.subjectCode || b.subjectTitle || ''))
}

function buildCreditsMap(list) {
  const map = new Map()
  for (const item of list || []) {
    const subjectId = item.subjectId ?? item.id
    if (subjectId == null) continue
    const credits = resolveCreditsValue(item.credits ?? item.credit ?? item.coefficient)
    // Use prefixed keys to match computeDecision's lookup
    map.set(`id:${Number(subjectId)}`, credits)
    map.set(`id:${String(subjectId)}`, credits)
  }
  return map
}

function resolveCreditsValue(value) {
  const num = Number(value)
  if (Number.isFinite(num) && num > 0) return num
  return 1
}

function isS4Block(block) {
  const label = normalizeSemesterLabel(block?.semesterLabel || block?.semesterCode)
  return !!label && label.includes('S4')
}

function getBlockCreditsMap(block) {
  if (block?.creditsMap instanceof Map) return block.creditsMap
  if (Array.isArray(block?.credits)) return buildCreditsMap(block.credits)
  if (isS4Block(block) && s4SemesterSummary.value?.creditsMap instanceof Map) return s4SemesterSummary.value.creditsMap
  return new Map()
}

function getBlockNotesForDecision(block) {
  if (!block) return []
  if (isS4Block(block) && isL2View.value && selectedS4Option.value) {
    return filteredS4Subjects.value.map(sub => ({
      subjectId: sub.subjectId,
      rawGrade: getStudentOptionGrade(sub.subjectId)
    }))
  }
  return Array.isArray(block.notes) ? block.notes : []
}

function computeDecision(notes, creditsMap) {
  let totalCredits = 0
  let weightedSum = 0
  let belowSix = 0
  let belowTen = 0
  const compensable = []

  for (const note of notes || []) {
    const value = resolveDisplayGrade(note)
    // Treat null/NaN as 0 instead of skipping
    const effectiveGrade = (value != null && !Number.isNaN(value)) ? value : 0
    // Try to get credits with the prefixed key format
    const subjectId = note.subjectId
    let credits = resolveCreditsValue(creditsMap.get(`id:${Number(subjectId)}`))
    if (credits === 1 && subjectId != null) {
      credits = resolveCreditsValue(creditsMap.get(`id:${String(subjectId)}`))
    }
    weightedSum += effectiveGrade * credits
    totalCredits += credits
    if (effectiveGrade < 6) {
      belowSix += 1
      belowTen += 1
    } else if (effectiveGrade < 10) {
      belowTen += 1
      compensable.push(note.subjectId)
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

function computeLevelDecisionBlocks(blocks) {
  let totalCredits = 0
  let weightedSum = 0
  let belowSix = 0
  let belowTen = 0
  const compensable = []
  const blockDecisions = []

  for (const block of blocks || []) {
    if (!block) continue
    if (block.summary?.decision) blockDecisions.push(block.summary.decision)
    const creditsMap = getBlockCreditsMap(block)
    const notesForDecision = getBlockNotesForDecision(block)
    if (!notesForDecision.length) continue
    for (const note of notesForDecision) {
      const value = resolveDisplayGrade(note)
      if (value == null || Number.isNaN(value)) continue
      const credits = resolveCreditsValue(creditsMap.get(note.subjectId))
      weightedSum += value * credits
      totalCredits += credits
      if (value < 6) {
        belowSix += 1
        belowTen += 1
      } else if (value < 10) {
        belowTen += 1
        compensable.push(note.subjectId)
      }
    }
  }

  const average = totalCredits > 0 ? Math.round((weightedSum / totalCredits) * 100) / 100 : null
  const summary = { average, decision: 'Admis', compensated: new Set() }

  if (blockDecisions.some(decision => decision === 'Ajourné')) {
    summary.decision = 'Ajourné'
    return summary
  }

  if (average == null || average < 10 || belowSix > 0 || belowTen >= 3) {
    summary.decision = 'Ajourné'
    return summary
  }

  if (compensable.length) {
    for (const subjectId of compensable.slice(0, 2)) summary.compensated.add(subjectId)
  }

  return summary
}

function resolveBlockSummary(block, notes, creditsMap) {
  const backendSummary = extractDecisionSummary(block)
  if (backendSummary?.decision) return backendSummary
  return computeDecision(notes, creditsMap)
}

function applyBackendS4Summary(summary) {
  if (!summary || !summary.decision) return
  if (s4SemesterSummary.value) {
    s4SemesterSummary.value = { ...s4SemesterSummary.value, summary }
  } else {
    s4SemesterSummary.value = {
      semesterCode: 'S4',
      semesterLabel: 'S4',
      notes: [],
      creditsMap: new Map(),
      summary
    }
  }
}

function getBlockAverage(block) {
  const avg = block?.summary?.average
  if (avg == null) return '—'
  return avg.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function getNoteCredits(block, note) {
  const creditsMap = block?.creditsMap
  if (!creditsMap) return '—'
  const subjectId = note.subjectId
  if (subjectId == null) return '—'
  // Try with prefixed keys first
  let credits = creditsMap.get(`id:${Number(subjectId)}`)
  if (credits == null) {
    credits = creditsMap.get(`id:${String(subjectId)}`)
  }
  if (credits == null) return '—'
  return credits
}

function getBlockNoteCreditContribution(block, note) {
  const creditsMap = block?.creditsMap
  if (!creditsMap) return 0
  const subjectId = note.subjectId
  if (subjectId == null) return 0
  // Try with prefixed keys first
  let credits = creditsMap.get(`id:${Number(subjectId)}`)
  if (credits == null) {
    credits = creditsMap.get(`id:${String(subjectId)}`)
  }
  if (!credits) return 0
  return getNoteAppreciation(block, note) === 'Ajournée' ? 0 : Number(credits)
}

function getBlockCreditsTotal(block) {
  if (!block || !Array.isArray(block.notes)) return 0
  return block.notes.reduce((sum, note) => sum + getBlockNoteCreditContribution(block, note), 0)
}

function getBlockCreditsTotalDisplay(block) {
  const total = getBlockCreditsTotal(block)
  return formatCreditsValue(total)
}

function getNoteAppreciation(block, note) {
  const summary = block?.summary
  const value = resolveDisplayGrade(note)
  if (!summary || value == null || Number.isNaN(value)) return '—'
  if (value < 6) return 'Ajournée'
  if (value < 10) {
    return summary.compensated.has(note.subjectId) && summary.decision === 'Admis' ? 'Compensée' : 'Ajournée'
  }
  if (value < 12) return 'Passable'
  if (value < 14) return 'Assez Bien'
  if (value < 16) return 'Bien'
  return 'Très Bien'
}

async function getSemestersCache() {
  if (semestersCache.value) return semestersCache.value
  try {
    const resp = await semesterService.getSemesters()
    semestersCache.value = resp && resp.data ? resp.data : resp
  } catch (e) {
    console.error('Unable to fetch semesters list', e)
    semestersCache.value = []
  }
  return semestersCache.value
}

function normalizeSemesterLabel(label) {
  return (label || '').toString().trim().toUpperCase()
}

async function resolveSemesterId(label) {
  if (!label) return null
  const normalized = normalizeSemesterLabel(label)
  const list = await getSemestersCache()
  const match = (list || []).find(sem => {
    const lbl = normalizeSemesterLabel(sem.label || sem.code)
    if (lbl === normalized) return true
    if (lbl.includes(normalized) || normalized.includes(lbl)) return true
    const digitsA = lbl.replace(/[^0-9]/g, '')
    const digitsB = normalized.replace(/[^0-9]/g, '')
    return digitsA && digitsB && digitsA === digitsB
  })
  return match?.id || null
}

async function getSemesterCredits(label) {
  const cacheKey = normalizeSemesterLabel(label)
  if (!cacheKey) return []
  if (creditsCache.value.has(cacheKey)) {
    return creditsCache.value.get(cacheKey)
  }
  try {
    const semId = await resolveSemesterId(label)
    if (!semId) {
      creditsCache.value.set(cacheKey, [])
      return []
    }
    const data = await notesService.getSubjectsCreditsBySemester(semId)
    creditsCache.value.set(cacheKey, data || [])
    return creditsCache.value.get(cacheKey)
  } catch (e) {
    console.error('Unable to fetch semester credits', e)
    creditsCache.value.set(cacheKey, [])
    return []
  }
}

async function load() {
  loading.value = true
  try {
    const requestedLevels = []
    const seen = new Set()
    for (const code of getTargetLevelCodes()) {
      if (!code) continue
      const normalized = code.toString().toUpperCase()
      if (seen.has(normalized)) continue
      seen.add(normalized)
      requestedLevels.push(code)
    }

    const aggregatedBlocks = []
    let detectedS4Block = null
    let derivedLabel = ''

    for (const code of requestedLevels) {
      const resp = await api.get(`/students/${studentId}/notes/levels/${code}`)
      const payload = resp.data && resp.data.data ? resp.data.data : resp.data
      if (!derivedLabel) derivedLabel = payload?.levelLabel || code
      const blocks = payload?.semesters || []
      for (const b of blocks) {
        const semesterLabelValue = b.semesterLabel || b.semesterCode || b.semester || ''
        const notes = dedupeBySubject(b.notes || [])
        const creditsList = await getSemesterCredits(semesterLabelValue)
        const creditsMap = buildCreditsMap(creditsList)
        const summary = resolveBlockSummary(b, notes, creditsMap)
        const entry = {
          semesterCode: b.semesterCode || semesterLabelValue || 'UNKNOWN',
          semesterLabel: semesterLabelValue,
          notes,
          creditsMap,
          summary
        }
        if (normalizedLevelParam === 'L2' && !detectedS4Block && blockMatchesSemester(entry, 'S4')) {
          detectedS4Block = entry
          applyBackendS4Summary(summary)
        }
        aggregatedBlocks.push(entry)
      }
    }

    const orderedBlocks = sortSemesterBlocks(aggregatedBlocks)
    const sanitized = normalizedLevelParam === 'L2'
      ? orderedBlocks.filter(block => !blockMatchesSemester(block, 'S4'))
      : orderedBlocks
    semesters.value = sanitized
    s4SemesterSummary.value = normalizedLevelParam === 'L2' ? detectedS4Block : null
    if (includeFullRecord.value && normalizedLevelParam === 'L2') {
      levelLabel.value = 'Licence 1 à 2'
    } else {
      levelLabel.value = derivedLabel || levelParam
    }
  } catch (e) {
    console.error('Failed to load level notes', e)
    error.value = e?.response?.data?.error?.message || e?.message || 'Erreur lors du chargement des notes.'
    semesters.value = []
  } finally {
    loading.value = false
  }
}

onMounted(load)

const isL2View = computed(() => normalizedLevelParam === 'L2')

const s3SemesterSummary = computed(() => {
  if (!isL2View.value) return null
  return semesters.value.find(block => {
    const label = normalizeSemesterLabel(block?.semesterLabel || block?.semesterCode || '')
    if (!label) return false
    return label.includes('S3') || label.endsWith('3')
  }) || null
})

const hasS3Average = computed(() => {
  const summary = s3SemesterSummary.value?.summary
  return summary?.average != null
})

const studentS3AverageDisplay = computed(() => {
  if (!hasS3Average.value) return '—'
  return formatGrade(s3SemesterSummary.value.summary.average)
})

function levelSummarySourceBlocks() {
  const blocks = [...semesters.value]
  if (isL2View.value && displayedS4Block.value) blocks.push(displayedS4Block.value)
  return blocks
}

watch(isL2View, (val) => {
  if (val) {
    ensureS4Options()
  } else {
    selectedS4Option.value = ''
    resetS4OptionPanel()
  }
}, { immediate: true })

watch(s4Options, (list) => {
  if (!isL2View.value || selectedS4Option.value || !list.length) return
  const devOption = list.find(opt => String(opt.label || '').toLowerCase().includes('développement'))
  const fallback = devOption || list[0]
  if (fallback) selectedS4Option.value = fallback.id
})

async function ensureS4Options() {
  if (s4Options.value.length || loadingS4Options.value) return
  loadingS4Options.value = true
  try {
    const resp = await notesService.getOptions()
    s4Options.value = (resp || []).filter(opt => opt.id !== 1)
  } catch (e) {
    console.error('Unable to load S4 options', e)
    s4Options.value = []
  } finally {
    loadingS4Options.value = false
  }
}

watch(selectedS4Option, async (optionId) => {
  if (!isL2View.value) return
  resetS4OptionPanel()
  if (!optionId) return
  await loadS4OptionDetails(optionId)
})

function resetS4OptionPanel(clearError = true) {
  s4OptionData.value = { subjects: [], subjectAverages: [], optionAverage: null, studentRecord: null }
  if (clearError) s4OptionError.value = ''
}

async function resolveS4SemesterId() {
  if (s4SemesterId.value) return s4SemesterId.value
  s4SemesterId.value = await resolveSemesterId('S4')
  return s4SemesterId.value
}

let s4OptionRequestToken = 0
async function loadS4OptionDetails(optionId) {
  const currentToken = ++s4OptionRequestToken
  loadingS4Option.value = true
  s4OptionError.value = ''
  try {
    const semesterId = await resolveS4SemesterId()
    if (!semesterId) throw new Error("Impossible d'identifier le semestre 4.")
    const data = await notesService.getOptionSubjects(semesterId, optionId)
    if (currentToken !== s4OptionRequestToken) return
    const subjects = Array.isArray(data?.subjects) ? data.subjects : []
    const subjectAverages = Array.isArray(data?.subjectAverages) ? data.subjectAverages : []
    const students = Array.isArray(data?.students) ? data.students : []
    const record = students.find(st => Number(st.studentId) === Number(studentId)) || null
    s4OptionData.value = {
      subjects,
      subjectAverages,
      optionAverage: data?.optionAverage ?? null,
      studentRecord: record ? normalizeStudentOptionRecord(record, subjects) : null
    }
    const backendSummary = (record && extractDecisionSummary(record)) || extractDecisionSummary(data)
    if (backendSummary) applyBackendS4Summary(backendSummary)
  } catch (e) {
    if (currentToken === s4OptionRequestToken) {
      console.error('Unable to load S4 option details', e)
      s4OptionError.value = e?.response?.data?.error?.message || e?.message || 'Impossible de charger les notes de cette option.'
      resetS4OptionPanel(false)
    }
  } finally {
    if (currentToken === s4OptionRequestToken) {
      loadingS4Option.value = false
    }
  }
}

function normalizeStudentOptionRecord(record, subjects) {
  if (!record) return null
  const map = new Map()
  for (const grade of record.grades || []) map.set(String(grade.subjectId), grade)
  const normalizedGrades = []
  for (const subject of subjects || []) {
    const sid = subject.subjectId ?? subject.id
    if (!sid) continue
    const existing = map.get(String(sid))
    if (existing) normalizedGrades.push(existing)
    else normalizedGrades.push({
      subjectId: sid,
      subjectCode: subject.subjectCode,
      subjectTitle: subject.subjectTitle,
      bestGrade: null,
      bestOptionalGrade: null,
      rawGrade: null
    })
  }
  return { ...record, grades: normalizedGrades }
}

function getStudentOptionGrade(subjectId) {
  const record = s4OptionData.value.studentRecord
  if (!record || !Array.isArray(record.grades)) return 0
  const entry = record.grades.find(g => Number(g.subjectId) === Number(subjectId))
  if (!entry) return 0
  if (entry.optionalGroupId) {
    const pick = selectedS4OptionalSubjects.value.get(entry.optionalGroupId)
    if (!pick || Number(pick.subjectId) !== Number(subjectId)) return 0
    if (pick.bestOptional != null) return Number(pick.bestOptional)
  }
  const direct = resolveEntryRawGrade(entry)
  if (direct != null) return direct
  if (entry.bestGrade != null) return Number(entry.bestGrade)
  return 0
}

function getSubjectOptionAverage(subjectId) {
  const list = s4OptionData.value.subjectAverages || []
  const entry = list.find(sub => String(sub.subjectId) === String(subjectId))
  return entry?.average ?? null
}

function getOptionSubjectAppreciation(subjectId) {
  const value = getStudentOptionGrade(subjectId)
  if (value == null || Number.isNaN(value)) return '—'
  if (value < 6) return 'Ajournée'
  if (value < 10) return 'Ajournée'
  if (value < 12) return 'Passable'
  if (value < 14) return 'Assez Bien'
  if (value < 16) return 'Bien'
  return 'Très Bien'
}

function getOptionSubjectCredits(subjectId) {
  const map = s4OptionSubjectCreditsMap.value
  if (!(map instanceof Map)) return null
  if (subjectId == null) return null
  // Try with prefixed keys
  let credits = map.get(`id:${Number(subjectId)}`)
  if (credits == null) {
    credits = map.get(`id:${String(subjectId)}`)
  }
  return credits == null ? null : Number(credits)
}

function getOptionSubjectCreditsDisplay(subjectId) {
  const credits = getOptionSubjectCredits(subjectId)
  if (credits == null) return '—'
  return formatCreditsValue(credits)
}

function getOptionSubjectCreditContribution(subjectId) {
  const credits = getOptionSubjectCredits(subjectId)
  if (!Number.isFinite(credits)) return 0
  return getOptionSubjectAppreciation(subjectId) === 'Ajournée' ? 0 : credits
}

function blockMatchesSemester(block, target) {
  const normalizedTarget = normalizeSemesterLabel(target)
  if (!block || !normalizedTarget) return false
  const label = normalizeSemesterLabel(block.semesterLabel || '')
  const code = normalizeSemesterLabel(block.semesterCode || '')
  const combined = `${label} ${code}`.trim()
  const tokens = [label, code, combined]
  return tokens.some(value => {
    if (!value) return false
    if (value === normalizedTarget) return true
    if (value.includes(normalizedTarget)) return true
    const digitsValue = value.replace(/[^0-9]/g, '')
    const digitsTarget = normalizedTarget.replace(/[^0-9]/g, '')
    return digitsValue && digitsTarget && digitsValue === digitsTarget
  })
}

function shouldHideSemesterBlock(block) {
  if (!isL2View.value) return false
  return blockMatchesSemester(block, 'S4') || blockMatchesSemester(block, 'SEMESTRE 4')
}

function getTargetLevelCodes() {
  if (includeFullRecord.value && normalizedLevelParam === 'L2') return ['L1', levelParam]
  return [levelParam]
}

function getSemesterOrderValue(block) {
  const tokens = [block?.semesterCode, block?.semesterLabel]
    .map(value => normalizeSemesterLabel(value || ''))
    .filter(Boolean)
  for (const token of tokens) {
    const digitMatch = token.replace(/[^0-9]/g, '')
    if (digitMatch) {
      const numeric = Number(digitMatch)
      if (!Number.isNaN(numeric)) return numeric
    }
    if (token.includes('S1')) return 1
    if (token.includes('S2')) return 2
    if (token.includes('S3')) return 3
    if (token.includes('S4')) return 4
  }
  return 99
}

function sortSemesterBlocks(blocks) {
  return [...(blocks || [])].sort((a, b) => getSemesterOrderValue(a) - getSemesterOrderValue(b))
}
</script>

<style scoped>
table { border-spacing: 0 }
.semester-summary {
  @apply flex flex-wrap gap-3 mb-3 text-sm;
}

.summary-pill {
  @apply px-3 py-1 rounded-full bg-gray-100 text-gray-700 font-semibold;
}

.summary-pill-success {
  @apply bg-green-100 text-green-800;
}

.summary-pill-danger {
  @apply bg-red-100 text-red-800;
}
</style>
