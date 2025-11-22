<template>
  <div class="p-8 max-w-md mx-auto bg-gray-50 min-h-screen">
    <h1 class="text-3xl font-extrabold mb-6 text-purple-700 text-center">
      Connexion à l’espace étudiant
    </h1>

    <form @submit.prevent="submit" class="space-y-4 bg-white p-6 rounded-xl shadow">
      <div>
        <label class="block text-sm font-medium mb-1 text-gray-700">Nom d'utilisateur</label>
        <input 
          v-model="username"
          class="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-purple-400 outline-none"
        />
      </div>

      <div>
        <label class="block text-sm font-medium mb-1 text-gray-700">Mot de passe</label>
        <input 
          type="password"
          v-model="password"
          class="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-purple-400 outline-none"
        />
      </div>

      <div>
        <button 
          type="submit" 
          class="w-full px-4 py-3 bg-purple-600 text-white rounded-xl shadow hover:bg-purple-700 transition font-semibold"
        >
          Se connecter
        </button>
      </div>
    </form>

    <div v-if="error" class="mt-4 text-red-600 text-center font-semibold">
      {{ error }}
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import api from '../services/api/api.js'
import { useRouter, useRoute } from 'vue-router'

// Préremplissage auto
const username = ref('admin')
const password = ref('admin123')

const error = ref('')
const router = useRouter()
const route = useRoute()

async function submit() {
  error.value = ''

  try {
    const res = await api.post('/auth/login', { 
      username: username.value, 
      password: password.value 
    })

    const token = res.data?.data?.token

    if (token) {
      localStorage.setItem('token', token)

      const dest = route.query?.redirect || '/'
      router.push(dest)
    } else {
      error.value = 'Token non reçu'
    }
  } catch (e) {
    error.value = e.response?.data?.error?.message 
      || e.message 
      || 'Erreur de connexion'
  }
}
</script>

<style scoped>
</style>
