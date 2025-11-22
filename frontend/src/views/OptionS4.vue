<template>
  <div class="p-6">
    <h1 class="text-2xl font-bold mb-4">Matières & Notes — Semestre 4 (par option)</h1>

    <div class="mb-4 flex items-center gap-3">
      <label class="font-medium">Option:</label>
      <select v-model="selectedOption" @change="handleOptionChange" class="p-2 border rounded">
        <option value="">-- Choisir une option --</option>
        <option v-for="opt in options" :key="opt.id" :value="opt.id">{{ opt.label }} (id:{{opt.id}})</option>
      </select>
    </div>

    <div v-if="loading">Chargement…</div>

    <div v-else>
      <div v-if="error" class="text-red-600">{{ error }}</div>

      <div v-if="subjects.length">
        <table class="w-full table-auto border-collapse mb-4">
          <thead>
            <tr>
              <th class="p-2 text-left">Étudiant</th>
              <th v-for="sub in subjects" :key="sub.subjectId" class="p-2 text-left">{{ sub.subjectCode }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="stu in students" :key="stu.studentId">
              <td class="p-2">{{ stu.firstName }} {{ stu.lastName }}</td>
                          <td v-for="sub in subjects" :key="sub.subjectId" class="p-2 text-center">
                            <span>{{ getGrade(stu, sub.subjectId) !== null ? getGrade(stu, sub.subjectId) : 0 }}</span>
                          </td>
            </tr>
          </tbody>
          <tfoot>
            <tr class="bg-gray-100">
              <td class="p-2 font-bold">Moyennes</td>
              <td v-for="avg in subjectAverages" :key="avg.subjectId" class="p-2 text-center font-semibold">{{ avg.average !== null ? avg.average : '—' }}</td>
            </tr>
            <tr>
              <td class="p-2 font-bold">Moyenne option</td>
              <td :colspan="subjects.length" class="p-2 text-center font-bold">{{ optionAverage !== null ? optionAverage : '—' }}</td>
            </tr>
          </tfoot>
        </table>
      </div>
      <div v-else class="text-sm text-gray-500">Aucune matière trouvée pour cette option / semestre.</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import notesService from '@/services/notesService'

const options = ref([])
const selectedOption = ref('')
const subjects = ref([])
const students = ref([])
const subjectAverages = ref([])
const optionAverage = ref(null)
const loading = ref(false)
const error = ref(null)
const route = useRoute()
const router = useRouter()

async function loadOptions() {
  try {
    const resp = await notesService.getOptions()
    options.value = resp || []
  } catch (e) {
    console.error(e)
  }
}

function handleOptionChange() {
  const nextQuery = { ...route.query }
  if (selectedOption.value) nextQuery.option = selectedOption.value
  else delete nextQuery.option
  router.replace({ query: nextQuery })
}

function buildStudents(payload) {
  subjects.value = payload.subjects || []
  students.value = payload.students || []
  // compute averages using credits and treating missing grades as 0
  try {
    const creditsMap = new Map()
    for (const s of (subjects.value || [])) creditsMap.set(s.subjectId, s.credits || 0)

    // prepare per-subject accumulator
    const subjAgg = new Map()
    for (const s of (subjects.value || [])) subjAgg.set(s.subjectId, { sum: 0, count: students.value.length })

    let totalNumer = 0
    let totalDenom = 0

    for (const stu of students.value) {
      let numer = 0
      let denom = 0
      for (const sub of (subjects.value || [])) {
        const sid = sub.subjectId
        const cred = creditsMap.get(sid) || 0
        denom += cred
        const gobj = (stu.grades || []).find(x => x.subjectId === sid)
        const g = gobj && (gobj.bestGrade !== null && gobj.bestGrade !== undefined) ? Number(gobj.bestGrade) : 0
        numer += g * cred
        const agg = subjAgg.get(sid)
        if (agg) agg.sum += g
      }
      if (denom > 0) {
        totalNumer += numer
        totalDenom += denom
      }
    }

    subjectAverages.value = (subjects.value || []).map(sub => {
      const agg = subjAgg.get(sub.subjectId)
      const avg = agg && agg.count > 0 ? Math.round((agg.sum / agg.count) * 100) / 100 : null
      return { subjectId: sub.subjectId, subjectCode: sub.subjectCode, subjectTitle: sub.subjectTitle, average: avg }
    })

    optionAverage.value = (totalDenom > 0) ? Math.round((totalNumer / totalDenom) * 100) / 100 : null
  } catch (e) {
    console.error('Failed to compute averages in OptionS4:', e)
    subjectAverages.value = payload.subjectAverages || []
    optionAverage.value = payload.optionAverage ?? null
  }
}

async function load() {
  if (!selectedOption.value) return
  loading.value = true
  error.value = null
  try {
    const payload = await notesService.getOptionSubjects(4, selectedOption.value)
    // payload contains subjects, students, subjectAverages, optionAverage
    buildStudents(payload)
  } catch (e) {
    console.error(e)
    error.value = 'Erreur lors du chargement'
  } finally {
    loading.value = false
  }
}

function getGrade(stu, subjectId) {
  const g = (stu.grades || []).find(x => x.subjectId === subjectId)
  if (!g) return null
  if (g.optionalGroupId && g.bestOptionalGrade != null) return g.bestOptionalGrade
  if (g.bestGrade !== null && g.bestGrade !== undefined) return g.bestGrade
  if (g.rawGrade !== null && g.rawGrade !== undefined) return g.rawGrade
  return g.grade ?? null
}

onMounted(() => {
  loadOptions()
})

watch(() => route.query.option, (val) => {
  const normalized = val ? String(val) : ''
  if (!normalized) {
    if (selectedOption.value) {
      selectedOption.value = ''
      subjects.value = []
      students.value = []
      subjectAverages.value = []
      optionAverage.value = null
    }
    return
  }
  if (String(selectedOption.value) === normalized) {
    if (!subjects.value.length) load()
    return
  }
  selectedOption.value = normalized
  load()
}, { immediate: true })
</script>

<style scoped>
.p-2 { padding: 0.5rem }
.text-center { text-align: center }
.table-auto { border-collapse: collapse }
</style>
