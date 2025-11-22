<!-- src/views/Dashboard.vue -->
<template>
  <div class="min-h-screen bg-gray-50 p-10 text-gray-800">
    <!-- Header -->
    <div class="mb-8">
      <h1 class="text-4xl font-extrabold mb-2 text-purple-700">Aurora Dashboard</h1>
      <p class="text-lg text-gray-600">
        Bienvenue sur votre espace étudiant. Explorez vos informations académiques de façon claire et simple.
      </p>
    </div>

    <!-- Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-10">
      <div class="dashboard-card border-l-4 border-purple-500">
        <h2 class="text-2xl font-semibold mb-2">Mes Semestres</h2>
        <p>Consultez la liste de vos semestres et matières pour suivre votre progression.</p>
      </div>

      <div class="dashboard-card border-l-4 border-pink-500">
        <h2 class="text-2xl font-semibold mb-2">Mes Notes</h2>
        <p>Visualisez vos notes et résultats pour chaque matière et semestre.</p>
      </div>

      <div class="dashboard-card border-l-4 border-blue-500">
        <h2 class="text-2xl font-semibold mb-2">Notifications</h2>
        <p>Recevez les annonces importantes concernant votre parcours académique.</p>
      </div>
    </div>

    <!-- Button -->
    <router-link
      to="/semesters"
      class="inline-block bg-purple-600 text-white font-bold px-6 py-3 rounded-lg shadow hover:bg-purple-700 transition"
    >
      Accéder aux semestres
    </router-link>
  </div>
</template>

<script setup>
</script>

<style scoped>
.dashboard-card {
  background: white;
  padding: 1.5rem;
  border-radius: 1rem;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.dashboard-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
}
</style>
