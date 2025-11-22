<template>
  <div class="semesters-page p-6">
    <h1 class="text-2xl font-bold mb-4">Semestres</h1>

    <div v-if="loading" class="py-6">Chargement des semestres...</div>

    <div v-else>
      <ul class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
        <li v-for="sem in semesters" :key="sem.id">
          <button @click="openSemester(sem)" class="w-full text-left p-3 rounded-lg border hover:shadow">
            <div class="font-semibold">{{ sem.label }}</div>
            <div class="text-sm text-gray-500">Niveau: {{ sem.level || '—' }}</div>
          </button>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import semesterService from '../services/semesterService'

const semesters = ref([])
const loading = ref(true)
const router = useRouter()

async function load() {
  loading.value = true
  try {
    semesters.value = await semesterService.getSemesters()
  } catch (e) {
    console.error(e)
    semesters.value = []
  } finally {
    loading.value = false
  }
}

function openSemester(sem) {
  // Navigate to semester averages view
  router.push({ name: 'semester-averages', params: { label: sem.label } })
}

onMounted(load)
</script>

<style scoped>
.semesters-page { }
.hover\:shadow:hover { box-shadow: 0 6px 18px rgba(0,0,0,0.08); }
</style>
