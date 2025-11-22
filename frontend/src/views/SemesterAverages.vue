<template>
  <div class="p-6">
    <button class="mb-4 text-sm text-gray-600 hover:underline" @click="router.back()">← Retour</button>
    <h1 class="text-2xl font-bold mb-4">Moyennes — {{ label }}</h1>

    <div v-if="loading">Chargement des moyennes...</div>

    <div v-else>
      <table class="w-full table-auto border-collapse">
        <thead>
          <tr class="bg-gray-100">
            <th class="p-2 text-left">Nom</th>
            <th class="p-2 text-left">Semestre</th>
            <th class="p-2 text-left">Année</th>
            <th class="p-2 text-right">Moyenne</th>
            <th class="p-2 text-right">Décision & Mention</th>
            <th class="p-2 text-right">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in rows" :key="row.studentId" class="border-t">
            <td class="p-2">
              <button class="text-left text-blue-600 hover:underline" @click.stop="openStudentFull(row.studentId, row.name)">{{ row.name }}</button>
            </td>
            <td class="p-2">{{ row.semester }}</td>
            <td class="p-2">{{ row.year }}</td>
            <td class="p-2 text-right">
              <button class="w-full text-right font-semibold text-blue-600 hover:underline" @click.stop="openStudent(row)">
                {{ formatAverage(row.average) }}
              </button>
              <div v-if="row.__optionLabel" class="text-xs text-gray-500">Option: {{ row.__optionLabel }}</div>
            </td>
            <td class="p-2 text-right">
              <div class="flex flex-col items-end gap-1">
                <span class="decision-pill" :class="getDecisionPillClass(row)">
                  {{ getRowDecisionLabel(row) }}
                </span>
                <span class="text-xs text-gray-500">Mention: {{ getRowAppreciation(row) }}</span>
                <span
                  v-if="shouldShowDecisionWarning(row)"
                  class="text-xs text-amber-600 max-w-xs text-right"
                >
                  {{ getDecisionWarning(row) }}
                </span>
              </div>
            </td>
            <td class="p-2 text-right">
              <div class="flex items-center justify-end gap-2 relative">
                <button class="px-2 py-1 text-xs rounded bg-green-100 text-green-800 hover:bg-green-200" @click.stop="openLevel(row.studentId, 'L1', row.name)">L1</button>
                <button class="px-2 py-1 text-xs rounded bg-yellow-100 text-yellow-800 hover:bg-yellow-200" @click.stop="openLevel(row.studentId, 'L2', row.name)">L2</button>
                <div>
                  <button class="px-2 py-1 text-xs rounded bg-indigo-100 text-indigo-800 hover:bg-indigo-200" @click.stop="toggleOptionsRow(row.studentId)">Options</button>
                  <div v-if="optionMenuRow === row.studentId" class="option-menu-panel">
                    <p class="option-menu-title">S4 par option</p>
                    <button
                      v-for="opt in s4Options"
                      :key="opt.id"
                      class="option-menu-item"
                      @click.stop="openStudentOption(row, opt.id)"
                    >
                      {{ opt.label }}
                    </button>
                    <router-link class="option-menu-link" :to="{ name: 'options' }" @click="optionMenuRow = null">Vue agrégée</router-link>
                  </div>
                </div>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import semesterService from '../services/semesterService'
import notesService from '../services/notesService'
import api from '../services/api/api.js'
import { extractDecisionSummary } from '../utils/decisionSummary'

const route = useRoute()
const router = useRouter()
const label = route.params.label || route.query.semester || ''
const rows = ref([])
const loading = ref(true)
const s4Options = ref([])
const optionMenuRow = ref(null)
const normalizedLabel = (label || '').toString().trim().toUpperCase()
const shouldHydrateStudentDecisions = normalizedLabel === 'S4'
const semestersCache = ref(null)
const s4SemesterId = ref(null)
const s4SubjectsMetadata = ref([])
const currentSemesterSubjectsMetadata = ref([])
const optionPayloadCache = new Map()
const optionPayloadPromises = new Map()

async function load() {
  loading.value = true
  try {
    const data = await semesterService.getAverages(label)
    // api client returns ApiResponse wrapper; handle both wrapped and raw
    rows.value = data?.data ? data.data : data
    
    // Load semester metadata for credit calculations
    await loadCurrentSemesterSubjectsMetadata()
    
    if (shouldHydrateStudentDecisions) {
      await ensureS4Support()
      await hydrateStudentDecisions()
    } else {
      // For non-S4 semesters, still hydrate with local calculations
      await hydrateStudentDecisions()
    }
  } catch (e) {
    console.error(e)
    rows.value = []
  } finally {
    loading.value = false
  }
}

async function loadOptions(force = false) {
  if (s4Options.value.length && !force) return s4Options.value
  try {
    const resp = await notesService.getOptions()
    s4Options.value = (resp || []).filter(opt => opt.id !== 1)
  } catch (e) {
    console.error('Unable to load options', e)
    s4Options.value = []
  }
  return s4Options.value
}

async function hydrateStudentDecisions() {
  const tasks = (rows.value || []).map(async row => {
    await refreshStudentDecision(row)
    // Only refresh best option for S4
    if (shouldHydrateStudentDecisions) {
      await refreshStudentBestOption(row)
    }
  })
  try {
    await Promise.all(tasks)
  } catch (e) {
    console.error('Unable to hydrate student decisions', e)
  }
}

async function refreshStudentDecision(row) {
  if (!row?.studentId) return
  try {
    const resp = await api.get(`/students/${row.studentId}/notes/semesters/${encodeURIComponent(label)}`)
    const payload = resp.data?.data ?? resp.data
    
    // Build credits map from current semester metadata
    const creditsMap = new Map()
    for (const subject of currentSemesterSubjectsMetadata.value) {
      const subjectId = subject.subjectId ?? subject.id
      if (subjectId == null) continue
      const credits = resolveCreditsValue(subject.credits ?? subject.credit ?? subject.coefficient)
      creditsMap.set(`id:${Number(subjectId)}`, credits)
      creditsMap.set(`id:${String(subjectId)}`, credits)
      const normalizedCode = normalizeSubjectCode(subject.subjectCode || subject.code)
      if (normalizedCode) creditsMap.set(`code:${normalizedCode}`, credits)
    }
    
    // Compute decision locally with proper credits
    const notes = payload?.notes || []
    const entries = notes.map(note => ({
      subjectId: note.subjectId,
      rawGrade: note.grade ?? note.rawGrade ?? note.value
    }))
    const computedSummary = computeDecisionSummary(entries, creditsMap)
    
    if (computedSummary) {
      row.__studentDecision = computedSummary
      if (computedSummary.average != null) row.average = computedSummary.average
    }
  } catch (e) {
    console.error(`Unable to refresh decision for student ${row.studentId}`, e)
  }
}

async function refreshStudentBestOption(row) {
  if (!row?.studentId || !s4Options.value.length) return
  let best = null
  for (const opt of s4Options.value) {
    try {
      const payload = await getOptionPayload(opt.id)
      const studentRecord = (payload?.students || []).find(st => Number(st.studentId) === Number(row.studentId))
      if (!studentRecord) continue
      const { creditsMap, metadataMap } = buildSubjectContext(payload?.subjects, opt.id)
      const normalizedGrades = normalizeOptionGrades(studentRecord?.grades, metadataMap)
      const computedSummary = computeDecisionSummary(normalizedGrades, creditsMap)
      const computedAverage = computedSummary?.average ?? computeWeightedAverage(normalizedGrades, creditsMap)
      if (computedAverage == null) continue
      if (!best || computedAverage > best.average) {
        best = {
          average: computedAverage,
          optionLabel: opt.label,
          optionId: opt.id,
          summary: computedSummary || { average: computedAverage, decision: computedAverage >= 10 ? 'Admis' : 'Ajourné', compensated: new Set() }
        }
      }
    } catch (e) {
      console.error(`Unable to load option ${opt.id} for student ${row.studentId}`, e)
    }
  }
  if (best) {
    row.average = best.average
    row.__optionLabel = best.optionLabel
    row.__bestOptionId = best.optionId
    if (best.summary) {
      row.__studentDecision = {
        ...best.summary,
        average: best.average
      }
    } else if (best.average != null) {
      row.__studentDecision = {
        average: best.average,
        decision: best.average >= 10 ? 'Admis' : 'Ajourné',
        compensated: new Set()
      }
    }
  }
}

async function getSemestersCache() {
  if (semestersCache.value) return semestersCache.value
  try {
    const resp = await semesterService.getSemesters()
    semestersCache.value = resp?.data ?? resp
  } catch (e) {
    console.error('Unable to load semesters cache', e)
    semestersCache.value = []
  }
  return semestersCache.value
}

async function resolveS4SemesterId() {
  if (s4SemesterId.value) return s4SemesterId.value
  const list = await getSemestersCache()
  const match = (list || []).find(sem => (sem.label || '').toString().trim().toUpperCase() === 'S4')
  if (match?.id) s4SemesterId.value = match.id
  return s4SemesterId.value
}

async function ensureS4Support() {
  const semesterId = await resolveS4SemesterId()
  await Promise.all([
    loadOptions(),
    loadS4SubjectsMetadata(semesterId)
  ])
}

async function loadS4SubjectsMetadata(semesterId, force = false) {
  const targetId = semesterId || await resolveS4SemesterId()
  if (!targetId) return []
  if (s4SubjectsMetadata.value.length && !force) return s4SubjectsMetadata.value
  try {
    const payload = await notesService.getSubjectsCreditsBySemester(targetId)
    s4SubjectsMetadata.value = payload || []
  } catch (e) {
    console.error('Unable to load S4 subjects metadata', e)
    s4SubjectsMetadata.value = []
  }
  return s4SubjectsMetadata.value
}

async function loadCurrentSemesterSubjectsMetadata() {
  if (currentSemesterSubjectsMetadata.value.length) return currentSemesterSubjectsMetadata.value
  try {
    const list = await getSemestersCache()
    const match = (list || []).find(sem => (sem.label || '').toString().trim().toUpperCase() === normalizedLabel)
    if (!match?.id) return []
    const payload = await notesService.getSubjectsCreditsBySemester(match.id)
    currentSemesterSubjectsMetadata.value = payload || []
  } catch (e) {
    console.error('Unable to load current semester subjects metadata', e)
    currentSemesterSubjectsMetadata.value = []
  }
  return currentSemesterSubjectsMetadata.value
}

async function getOptionPayload(optionId) {
  if (optionPayloadCache.has(optionId)) return optionPayloadCache.get(optionId)
  if (optionPayloadPromises.has(optionId)) return optionPayloadPromises.get(optionId)
  const promise = (async () => {
    const semesterId = await resolveS4SemesterId()
    if (!semesterId) throw new Error('Semestre 4 introuvable')
    const payload = await notesService.getOptionSubjects(semesterId, optionId)
    optionPayloadCache.set(optionId, payload)
    optionPayloadPromises.delete(optionId)
    return payload
  })()
  optionPayloadPromises.set(optionId, promise)
  return promise
}

function getEffectiveDecision(row) {
  if (!row) return '—'
  if (row.__studentDecision?.decision) return row.__studentDecision.decision
  const avg = Number(row.average)
  const belowSix = Number(row.belowSix || 0)
  const belowTen = Number(row.belowTen || 0)
  if (Number.isFinite(avg)) {
    if (avg < 10 || belowSix > 0 || belowTen >= 3) return 'Ajourné'
    return 'Admis'
  }
  return row.decision || '—'
}

function getRowDecisionLabel(row) {
  return getEffectiveDecision(row)
}

function getRowAppreciation(row) {
  const decision = getEffectiveDecision(row)
  if (decision === 'Ajourné') return 'Ajourné'
  return getMeritLabel(row?.average)
}

function getDecisionPillClass(row) {
  const decision = getRowDecisionLabel(row)
  if (decision === 'Admis') return 'decision-pill-positive'
  if (decision === 'Ajourné') return 'decision-pill-negative'
  return 'decision-pill-negative'
}

function shouldShowDecisionWarning(row) {
  return getRowDecisionLabel(row) === 'Ajourné'
}

function getDecisionWarning(row) {
  const reasons = []
  const belowSix = Number(row?.belowSix || 0)
  const belowTen = Number(row?.belowTen || 0)
  if (belowSix > 0) reasons.push(`${belowSix} note(s) < 6`)
  if (belowTen >= 3) reasons.push(`${belowTen} notes < 10`)
  if (reasons.length) return `Ajourné : ${reasons.join(' • ')}`
  const avg = Number(row?.average)
  if (Number.isFinite(avg)) return `Ajourné malgré une moyenne de ${avg}`
  if (row?.__studentDecision?.decision === 'Ajourné') return 'Ajourné selon le relevé détaillé'
  return 'Ajourné — décision du jury'
}

function formatAverage(value) {
  if (value == null || Number.isNaN(Number(value))) return '—'
  return Number(value).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function computeWeightedAverage(grades, creditsMap = new Map()) {
  if (!grades || !grades.length) return null
  let totalCredits = 0
  let weightedSum = 0
  for (const grade of grades) {
    const gradeVal = extractDecisionGrade(grade)
    // Treat null/NaN as 0 instead of skipping
    const effectiveGrade = (gradeVal != null && !Number.isNaN(gradeVal)) ? gradeVal : 0
    const credits = getCreditsForEntry(grade, creditsMap)
    weightedSum += effectiveGrade * credits
    totalCredits += credits
  }
  if (totalCredits === 0) return null
  return Math.round((weightedSum / totalCredits) * 100) / 100
}

function resolveGradeValue(entry) {
  if (entry == null) return null
  if (typeof entry === 'number') return entry
  const candidates = ['bestOptionalGrade', 'rawGrade', 'grade', 'bestGrade']
  for (const key of candidates) {
    if (entry[key] != null) {
      const num = Number(entry[key])
      if (!Number.isNaN(num)) return num
    }
  }
  return null
}

function normalizeOptionGrades(grades, metadataMap = new Map()) {
  const enriched = Array.isArray(grades)
    ? grades
        .map(grade => enrichGradeWithMetadata(grade, metadataMap))
        .filter(grade => {
          if (!metadataMap || !metadataMap.size) return true
          const canonicalKey = grade.__canonicalKey || resolveGradeCanonicalKey(grade, metadataMap)
          return Boolean(canonicalKey && metadataMap.has(canonicalKey))
        })
    : []
  const completed = ensureSubjectsCoverage(enriched, metadataMap)
  const bestOptional = selectBestOptionalSubjects(completed)
  return completed
    .filter(grade => {
      if (!grade?.optionalGroupId) return true
      const pick = bestOptional.get(grade.optionalGroupId)
      return pick && isSameSubject(pick.subjectId, grade.subjectId)
    })
    .map(grade => {
      if (!grade?.optionalGroupId) return grade
      const pick = bestOptional.get(grade.optionalGroupId)
      if (!pick || pick.effectiveValue == null) return grade
      if (grade.bestOptionalGrade === pick.effectiveValue) return grade
      return { ...grade, bestOptionalGrade: pick.effectiveValue }
    })
}

function ensureSubjectsCoverage(grades, metadataMap = new Map()) {
  if (!metadataMap || !metadataMap.size) return grades
  const completed = Array.isArray(grades) ? [...grades] : []
  const coveredKeys = new Set()
  for (let i = 0; i < completed.length; i += 1) {
    const grade = completed[i]
    const canonicalKey = resolveGradeCanonicalKey(grade, metadataMap)
    if (canonicalKey) {
      coveredKeys.add(canonicalKey)
      if (!grade.__canonicalKey) {
        completed[i] = { ...grade, __canonicalKey: canonicalKey }
      }
    }
  }
  const emitted = new Set()
  const uniqueMetas = new Map()
  for (const meta of metadataMap.values()) {
    if (!meta?.canonicalKey || uniqueMetas.has(meta.canonicalKey)) continue
    uniqueMetas.set(meta.canonicalKey, meta)
  }
  for (const meta of uniqueMetas.values()) {
    const canonicalKey = meta?.canonicalKey
    if (!canonicalKey || coveredKeys.has(canonicalKey) || emitted.has(canonicalKey)) continue
    completed.push({
      subjectId: meta.subjectId,
      subjectCode: meta.subjectCode,
      subjectTitle: meta.subjectTitle,
      optionalGroupId: meta.optionalGroupId,
      optionalInSemester: meta.optionalInSemester,
      rawGrade: 0,
      __credits: meta.credits,
      __canonicalKey: canonicalKey,
      __synthetic: true
    })
    coveredKeys.add(canonicalKey)
    emitted.add(canonicalKey)
  }
  return completed
}

function selectBestOptionalSubjects(grades) {
  const map = new Map()
  for (const grade of grades || []) {
    if (!grade?.optionalGroupId) continue
    const value = resolveBaseGrade(grade)
    if (value == null) continue
    const numericValue = Number(value)
    const existing = map.get(grade.optionalGroupId)
    if (!existing || numericValue > existing.value) {
      map.set(grade.optionalGroupId, {
        subjectId: grade.subjectId,
        value: numericValue,
        effectiveValue: grade.bestOptionalGrade != null ? Number(grade.bestOptionalGrade) : numericValue
      })
    }
  }
  return map
}

function resolveBaseGrade(entry) {
  if (!entry) return null
  if (entry.rawGrade != null) return Number(entry.rawGrade)
  if (entry.grade != null) return Number(entry.grade)
  if (entry.bestGrade != null) return Number(entry.bestGrade)
  if (typeof entry === 'number') return entry
  return null
}

function buildSubjectContext(subjects, optionId = null) {
  const creditsMap = new Map()
  const metadataMap = new Map()

  const payloadSubjects = Array.isArray(subjects) ? subjects : []
  const fallbackAll = Array.isArray(s4SubjectsMetadata.value) ? s4SubjectsMetadata.value : []

  // Build a map by canonicalKey, merging payload with fallback to ensure commons are present and credits are accurate
  const byCanonical = new Map()

  const addOrMerge = (raw) => {
    const norm = normalizeSubjectMetadata(raw)
    if (!norm || !norm.canonicalKey) return
    const existing = byCanonical.get(norm.canonicalKey)
    if (!existing) {
      byCanonical.set(norm.canonicalKey, norm)
      return
    }
    // Merge: prefer real IDs/codes/titles, prefer non-default credits (>1) when available
    const merged = {
      ...existing,
      subjectId: existing.subjectId ?? norm.subjectId,
      subjectCode: existing.subjectCode || norm.subjectCode,
      subjectTitle: existing.subjectTitle || norm.subjectTitle,
      optionId: existing.optionId ?? norm.optionId,
      optionalGroupId: existing.optionalGroupId ?? norm.optionalGroupId,
      optionalInSemester: existing.optionalInSemester ?? norm.optionalInSemester,
      credits: pickBetterCredits(existing.credits, norm.credits),
      canonicalKey: norm.canonicalKey
    }
    byCanonical.set(norm.canonicalKey, merged)
  }

  // 1) Seed with payload subjects (often option-specific)
  for (const s of payloadSubjects) addOrMerge(s)

  // 2) Ensure we include common subjects and the current option's subjects from fallback
  const optionKey = optionId != null ? String(optionId).trim() : null
  for (const s of fallbackAll) {
    const subjOption = s.optionId ?? s.option_id ?? s.optionID ?? null
    const isCommon = subjOption == null
    const matchesOption = optionKey != null && String(subjOption ?? '').trim() === optionKey
    if (optionKey == null) {
      // No filter: include all
      addOrMerge(s)
    } else if (isCommon || matchesOption) {
      addOrMerge(s)
    }
  }

  // 3) Emit maps
  for (const meta of byCanonical.values()) {
    const { subjectId, subjectCode, credits, canonicalKey } = meta
    const keys = []
    if (subjectId != null) {
      keys.push(`id:${Number(subjectId)}`)
      keys.push(`id:${String(subjectId)}`)
    }
    if (subjectCode) keys.push(`code:${subjectCode}`)
    if (canonicalKey) keys.push(canonicalKey)
    for (const key of keys) {
      if (!key) continue
      creditsMap.set(key, credits)
      metadataMap.set(key, meta)
    }
  }

  return { creditsMap, metadataMap }
}

function pickBetterCredits(a, b) {
  const ca = Number(a)
  const cb = Number(b)
  const validA = Number.isFinite(ca) && ca > 0
  const validB = Number.isFinite(cb) && cb > 0
  if (validA && validB) return Math.max(ca, cb)
  if (validB) return cb
  if (validA) return ca
  // default to 1 if both missing/invalid
  return 1
}

function computeDecisionSummary(entries, creditsMap = new Map()) {
  if (!entries || !entries.length) return null
  let totalCredits = 0
  let weightedSum = 0
  let belowSix = 0
  let belowTen = 0
  const compensable = []

  for (const entry of entries) {
    const gradeVal = extractDecisionGrade(entry)
    // Treat null/NaN as 0 instead of skipping
    const effectiveGrade = (gradeVal != null && !Number.isNaN(gradeVal)) ? gradeVal : 0
    const credits = getCreditsForEntry(entry, creditsMap)
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

function extractDecisionGrade(entry) {
  if (!entry) return null
  if (entry.bestOptionalGrade != null) return Number(entry.bestOptionalGrade)
  if (entry.rawGrade != null) return Number(entry.rawGrade)
  if (entry.grade != null) return Number(entry.grade)
  if (entry.bestGrade != null) return Number(entry.bestGrade)
  // don't fallback to generic 'value' or cohort 'average'
  if (typeof entry === 'number') return entry
  return null
}

function resolveDecisionCredits(value) {
  const num = Number(value)
  if (Number.isFinite(num) && num > 0) return num
  return 1
}

function resolveCreditsValue(value) {
  return resolveDecisionCredits(value)
}

function getCreditsForEntry(entry, creditsMap) {
  if (entry?.__credits != null) return resolveDecisionCredits(entry.__credits)
  if (!creditsMap || !creditsMap.size) return 1
  const subjectId = entry?.subjectId ?? entry?.subject?.subjectId ?? null
  if (subjectId != null) {
    const byId = creditsMap.get(`id:${Number(subjectId)}`) ?? creditsMap.get(`id:${String(subjectId)}`)
    if (byId != null) return resolveDecisionCredits(byId)
  }
  const subjectCode = entry?.subjectCode || entry?.subject?.subjectCode
  const normalizedCode = normalizeSubjectCode(subjectCode)
  if (normalizedCode) {
    const byCode = creditsMap.get(`code:${normalizedCode}`)
    if (byCode != null) return resolveDecisionCredits(byCode)
  }
  return 1
}

function normalizeSubjectCode(code) {
  if (!code) return ''
  return String(code).trim().toUpperCase()
}

function normalizeSubjectMetadata(subject) {
  if (!subject) return null
  const subjectId = subject.subjectId ?? subject.id ?? null
  const subjectCode = normalizeSubjectCode(subject.subjectCode || subject.code)
  const credits = resolveDecisionCredits(subject.credits ?? subject.credit ?? subject.coefficient)
  const canonicalKey = resolveCanonicalSubjectKey(subjectId, subjectCode)
  if (!canonicalKey) return null
  return {
    subjectId,
    subjectCode,
    subjectTitle: subject.subjectTitle || subject.title || '',
    optionId: subject.optionId ?? subject.option_id ?? subject.optionID ?? null,
    optionalGroupId: subject.optionalGroupId ?? subject.optional_group_id ?? null,
    optionalInSemester: Boolean(subject.optionalInSemester ?? subject.optional_in_semester ?? subject.optional ?? false),
    credits,
    canonicalKey
  }
}

function enrichGradeWithMetadata(grade, metadataMap) {
  if (!grade || !metadataMap || !metadataMap.size) return grade
  const meta = findSubjectMetadata(grade, metadataMap)
  if (!meta) return grade
  return {
    ...grade,
    subjectId: grade.subjectId ?? meta.subjectId,
    subjectCode: grade.subjectCode ?? meta.subjectCode,
    subjectTitle: grade.subjectTitle ?? meta.subjectTitle,
    optionalGroupId: grade.optionalGroupId ?? meta.optionalGroupId,
    optionalInSemester: grade.optionalInSemester ?? meta.optionalInSemester,
    __credits: meta.credits,
    __canonicalKey: meta.canonicalKey
  }
}

function findSubjectMetadata(entry, metadataMap) {
  if (!entry) return null
  if (entry.__canonicalKey) {
    const metaByCanonical = metadataMap.get(entry.__canonicalKey)
    if (metaByCanonical) return metaByCanonical
  }
  const keys = buildSubjectKeys(entry.subjectId, entry.subjectCode || (entry.subject && entry.subject.subjectCode))
  for (const key of keys) {
    if (!key) continue
    const meta = metadataMap.get(key)
    if (meta) return meta
  }
  return null
}

function buildSubjectKeys(subjectId, subjectCode) {
  const keys = []
  if (subjectId != null) {
    keys.push(`id:${Number(subjectId)}`)
    keys.push(`id:${String(subjectId)}`)
  }
  const normalizedCode = normalizeSubjectCode(subjectCode)
  if (normalizedCode) keys.push(`code:${normalizedCode}`)
  return keys
}

function resolveCanonicalSubjectKey(subjectId, subjectCode) {
  if (subjectId != null) {
    const numericId = Number(subjectId)
    if (Number.isFinite(numericId)) return `id:${numericId}`
    return `id:${String(subjectId).trim()}`
  }
  const normalizedCode = normalizeSubjectCode(subjectCode)
  if (normalizedCode) return `code:${normalizedCode}`
  return null
}

function resolveGradeCanonicalKey(entry, metadataMap = new Map()) {
  if (!entry) return null
  if (entry.__canonicalKey) return entry.__canonicalKey
  const meta = metadataMap.size ? findSubjectMetadata(entry, metadataMap) : null
  if (meta?.canonicalKey) return meta.canonicalKey
  const keys = buildSubjectKeys(entry.subjectId ?? entry.subject?.subjectId, entry.subjectCode ?? entry.subject?.subjectCode)
  return keys.length ? keys[0] : null
}

function isSameSubject(a, b) {
  if (a == null || b == null) return false
  const numA = Number(a)
  const numB = Number(b)
  if (Number.isFinite(numA) && Number.isFinite(numB)) return numA === numB
  return String(a).trim() === String(b).trim()
}

function getMeritLabel(avg) {
  const value = Number(avg)
  if (Number.isNaN(value)) return '—'
  if (value < 6) return 'Ajourné'
  if (value < 10) return 'Ajourné'
  if (value < 12) return 'Passable'
  if (value < 14) return 'Assez Bien'
  if (value < 16) return 'Bien'
  return 'Très Bien'
}

function openStudent(row) {
  if (!row?.studentId) return
  const query = { name: row.name }
  if (shouldHydrateStudentDecisions && row.__bestOptionId) {
    query.option = row.__bestOptionId
  }
  router.push({ name: 'student-notes-semester', params: { id: row.studentId, semester: label }, query })
}

function openStudentFull(studentId, studentName) {
  if (!studentId) return
  router.push({
    name: 'student-notes-level',
    params: { id: studentId, level: 'L2' },
    query: { name: studentName, view: 'full', from: label }
  })
}

function openLevel(studentId, levelCode, studentName) {
  if (!studentId) return
  // navigate to the combined-level notes (e.g. L1 => S1+S2)
  router.push({ name: 'student-notes-level', params: { id: studentId, level: levelCode }, query: { name: studentName } })
}

function toggleOptionsRow(studentId) {
  optionMenuRow.value = optionMenuRow.value === studentId ? null : studentId
}

function openStudentOption(row, optionId) {
  if (!row?.studentId || !optionId) return
  router.push({
    name: 'student-notes-semester',
    params: { id: row.studentId, semester: 'S4' },
    query: { name: row.name, option: optionId }
  })
  optionMenuRow.value = null
}

onMounted(() => {
  load()
  loadOptions()
})
</script>

<style scoped>
table { border-spacing: 0; }
.decision-pill {
  @apply inline-flex items-center justify-center px-3 py-1 rounded-full text-xs font-semibold;
}

.decision-pill-positive {
  @apply bg-green-100 text-green-800;
}

.decision-pill-negative {
  @apply bg-red-100 text-red-800;
}

.option-menu-panel {
  @apply absolute right-0 mt-2 w-44 bg-white border border-gray-200 rounded-lg shadow-xl z-10 p-2 space-y-1;
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
</style>
