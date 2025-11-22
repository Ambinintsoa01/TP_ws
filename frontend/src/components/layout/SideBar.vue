<!-- frontend/src/components/layout/SideBar.vue -->
<template>
  <aside class="sidebar">
    <!-- Header -->
    <div class="sidebar-header">
      <div class="flex items-center space-x-4">
        <div class="logo-container">
          <Flag class="w-7 h-7 text-white" />
        </div>
        <div class="flex-1">
          <h1 class="text-2xl font-extrabold text-white tracking-tight">Student Notes</h1>
          <p class="text-sm text-purple-200 font-medium">Aurora Dashboard</p>
        </div>
      </div>
    </div>

    <!-- Menu -->
    <nav class="sidebar-nav">
      <div class="nav-section">
        <h3 class="nav-section-title">MENU</h3>
        <router-link
          to="/"
          class="nav-item"
          :class="{ 'active': isActive('/') }"
        >
          <div class="nav-item-icon">
            <Home class="w-5 h-5" />
          </div>
          <span class="nav-item-label">Accueil</span>
        </router-link>
      </div>
    </nav>

    <!-- Footer -->
    <div class="sidebar-footer">
      <div class="user-card">
        <div class="user-avatar-container">
          <div class="user-avatar">
            <User class="w-6 h-6 text-gray-900" />
          </div>
          <div class="status-indicator"></div>
        </div>
        <div class="user-details">
          <p class="user-name">Administrateur</p>
          <p class="user-role">Connecté</p>
        </div>
        <button class="logout-btn" @click="logout" title="Se déconnecter">
          <LogOut class="w-5 h-5" />
        </button>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { Flag, Home, User, LogOut } from 'lucide-vue-next'

const route = useRoute()

const isActive = (path) => route.path === path

const logout = () => {
  localStorage.removeItem('token')
  window.location.href = '/login'
}
</script>

<style scoped>
/* Sidebar Aurora Pro */
.sidebar {
  @apply fixed left-0 top-0 h-screen w-72 flex flex-col shadow-2xl z-50;
  background: linear-gradient(180deg, #3b82f6 0%, #8b5cf6 50%, #ec4899 100%);
}

/* Header */
.sidebar-header {
  @apply p-6 border-b border-purple-700;
}

.logo-container {
  @apply w-14 h-14 rounded-2xl flex items-center justify-center shadow-xl;
  background: linear-gradient(135deg, #a855f7 0%, #f472b6 100%);
}

/* Navigation */
.sidebar-nav {
  @apply flex-1 overflow-y-auto py-6 px-4 space-y-6;
}

.nav-section-title {
  @apply text-xs font-bold text-purple-200 uppercase tracking-wider px-4 mb-3;
}

.nav-item {
  @apply flex items-center space-x-3 px-4 py-3 rounded-xl cursor-pointer transition-all text-purple-100;
}
.nav-item:hover {
  @apply bg-purple-800/40 text-white;
}
.nav-item.active {
  @apply bg-purple-700 text-white;
}
.nav-item-icon {
  @apply w-10 h-10 flex items-center justify-center rounded-lg bg-purple-900;
}
.nav-item.active .nav-item-icon {
  @apply bg-white text-purple-900;
}

/* Footer */
.sidebar-footer {
  @apply p-4 border-t border-purple-700;
}
.user-card {
  @apply flex items-center space-x-4 p-4 rounded-xl bg-purple-900/60 hover:bg-purple-800 transition-all;
}
.user-avatar {
  @apply w-12 h-12 rounded-xl bg-white flex items-center justify-center shadow-lg;
}
.status-indicator {
  @apply absolute -bottom-1 -right-1 w-4 h-4 bg-green-500 border-2 border-purple-900 rounded-full;
}
.user-name {
  @apply text-sm font-bold text-white;
}
.user-role {
  @apply text-xs text-purple-200 font-medium;
}
.logout-btn {
  @apply p-2.5 rounded-lg bg-purple-900 text-purple-200 hover:bg-red-600 hover:text-white transition-all;
}
</style>
