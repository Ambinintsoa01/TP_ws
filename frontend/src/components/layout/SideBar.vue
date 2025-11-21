<!-- frontend/src/components/layout/SideBar.vue -->
<template>
  <aside class="sidebar">
    <!-- Header avec logo -->
    <div class="sidebar-header">
      <div class="flex items-center space-x-4">
        <div class="logo-container">
          <Flag class="w-7 h-7 text-white" />
        </div>
        <div class="flex-1">
          <h1 class="text-2xl font-bold text-white tracking-tight">Student Notes</h1>
          <p class="text-sm text-gray-400 font-medium">Gestion des Notes Étudiantes</p>
        </div>
      </div>
    </div>

    <!-- Navigation principale -->
    <nav class="sidebar-nav">
      <!-- Section Principale -->
      <div class="nav-section">
        <h3 class="nav-section-title">
          <span class="flex items-center gap-2">
            <div class="w-1 h-4 bg-white rounded-full"></div>
            PRINCIPAL
          </span>
        </h3>
        <router-link
          v-for="item in mainMenuItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ 'active': isActive(item.path) }"
        >
          <div class="nav-item-icon">
            <component :is="item.icon" class="w-5 h-5" />
          </div>
          <span class="nav-item-label">{{ item.label }}</span>
          <span v-if="item.badge" class="badge-notification">{{ item.badge }}</span>
          <div class="nav-item-indicator"></div>
        </router-link>
      </div>

      <!-- Section Gestion avec menu déroulant -->
      <div class="nav-section">
        <h3 class="nav-section-title">
          <span class="flex items-center gap-2">
            <div class="w-1 h-4 bg-white rounded-full"></div>
            GESTION
          </span>
        </h3>

        <!-- Menu Parent: Gestion des Étudiants -->
        <div class="nav-group">
          <button 
            @click="toggleStudentMenu"
            class="nav-item nav-item-parent"
            :class="{ 'active': isStudentActive, 'expanded': studentMenuOpen }"
          >
            <div class="nav-item-icon">
              <Users class="w-5 h-5" />
            </div>
            <span class="nav-item-label">Gestion des Étudiants</span>
            <ChevronDown 
              class="nav-chevron" 
              :class="{ 'rotated': studentMenuOpen }"
            />
          </button>

          <!-- Sous-menu (dropdown) -->
          <Transition name="dropdown">
            <div v-if="studentMenuOpen" class="nav-submenu">
              <router-link
                to="/students"
                class="nav-subitem"
                :class="{ 'active': isActive('/students') }"
              >
                <div class="nav-subitem-dot"></div>
                <User class="w-4 h-4" />
                <span>Liste des Étudiants</span>
              </router-link>

              <router-link
                to="/notes"
                class="nav-subitem"
                :class="{ 'active': isActive('/notes') }"
              >
                <div class="nav-subitem-dot"></div>
                <FileText class="w-4 h-4" />
                <span>Consultation des Notes</span>
              </router-link>
            </div>
          </Transition>
        </div>
      </div>
    </nav>

    <!-- Footer avec profil utilisateur -->
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
          <p class="user-role">Super Admin</p>
        </div>
        <button class="logout-btn" @click="logout" title="Se déconnecter">
          <LogOut class="w-5 h-5" />
        </button>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import {
  Flag,
  Home,
  Users,
  User,
  LogOut,
  ChevronDown,
  CalendarCheck,
  CalendarX,
  Clock,
  FileText
} from 'lucide-vue-next'

const route = useRoute()
const studentMenuOpen = ref(false)

const mainMenuItems = [
  { path: '/', label: 'Tableau de bord', icon: Home, title: 'Tableau de bord' },
  { path: '/students', label: 'Étudiants', icon: Users, title: 'Gestion des Étudiants' },
  { path: '/notes', label: 'Notes', icon: FileText, title: 'Gestion des Notes' },
]

// Vérifier si on est sur une page étudiant
const isStudentActive = computed(() => {
  return route.path.startsWith('/students') || route.path.startsWith('/notes')
})

// Auto-ouvrir le menu si on est sur une sous-page
watch(() => route.path, (newPath) => {
  if (newPath.startsWith('/students') || newPath.startsWith('/notes')) {
    studentMenuOpen.value = true
  }
}, { immediate: true })

const isActive = computed(() => (path) => {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
})

function toggleStudentMenu() {
  studentMenuOpen.value = !studentMenuOpen.value
}

const logout = () => {
  localStorage.removeItem('token')
  window.location.href = '/login'
}
</script>

<style scoped>
/* Sidebar principale */
.sidebar {
  @apply fixed left-0 top-0 h-screen w-72 bg-gray-900 shadow-2xl flex flex-col z-50;
  background: linear-gradient(180deg, #111827 0%, #000000 100%);
}

/* Header */
.sidebar-header {
  @apply p-6 border-b border-gray-800;
}

.logo-container {
  @apply w-14 h-14 bg-white rounded-2xl flex items-center justify-center shadow-xl;
  background: linear-gradient(135deg, #ffffff 0%, #f3f4f6 100%);
}

/* Navigation */
.sidebar-nav {
  @apply flex-1 overflow-y-auto py-6 px-4 space-y-8;
}

.sidebar-nav::-webkit-scrollbar {
  @apply w-1.5;
}

.sidebar-nav::-webkit-scrollbar-track {
  @apply bg-gray-900;
}

.sidebar-nav::-webkit-scrollbar-thumb {
  @apply bg-gray-700 rounded-full;
}

.sidebar-nav::-webkit-scrollbar-thumb:hover {
  @apply bg-gray-600;
}

/* Section de navigation */
.nav-section {
  @apply space-y-2;
}

.nav-section-title {
  @apply text-xs font-bold text-gray-400 uppercase tracking-wider px-4 mb-4;
}

/* Groupe de navigation (parent + enfants) */
.nav-group {
  @apply space-y-1;
}

/* Items de navigation */
.nav-item {
  @apply flex items-center space-x-3 px-4 py-3.5 rounded-xl;
  @apply text-gray-400 hover:text-white cursor-pointer relative;
  @apply overflow-hidden transition-all;
}

.nav-item::before {
  content: '';
  @apply absolute inset-0 bg-white transition-opacity;
  opacity: 0;
}

.nav-item:hover::before {
  opacity: 0.05;
}

.nav-item-icon {
  @apply relative z-10 flex items-center justify-center;
  @apply w-10 h-10 rounded-lg bg-gray-800 transition-all;
}

.nav-item:hover .nav-item-icon {
  @apply bg-gray-700;
}

.nav-item.active .nav-item-icon {
  @apply bg-white text-gray-900;
}

.nav-item-label {
  @apply relative z-10 font-semibold text-sm flex-1;
}

.nav-item.active {
  @apply text-white bg-gray-800;
}

.nav-item.active::after {
  content: '';
  @apply absolute right-0 top-1/2 -translate-y-1/2 w-1 h-8 bg-white rounded-l-full;
}

.nav-item-indicator {
  @apply ml-auto w-2 h-2 rounded-full bg-gray-700 transition-all;
  opacity: 0;
}

.nav-item:hover .nav-item-indicator {
  opacity: 1;
}

.nav-item.active .nav-item-indicator {
  @apply bg-white;
  opacity: 1;
}

/* Menu parent avec chevron */
.nav-item-parent {
  @apply relative;
}

.nav-item-parent.expanded {
  @apply bg-gray-800/50;
}

.nav-chevron {
  @apply relative z-10 w-5 h-5 text-gray-400 transition-transform;
}

.nav-chevron.rotated {
  @apply rotate-180 text-white;
}

/* Sous-menu */
.nav-submenu {
  @apply mt-1 ml-4 space-y-1 border-l-2 border-gray-800 pl-2;
}

.nav-subitem {
  @apply flex items-center gap-3 px-4 py-3 rounded-lg;
  @apply text-gray-400 hover:text-white hover:bg-gray-800/50;
  @apply transition-all relative;
}

.nav-subitem-dot {
  @apply w-1.5 h-1.5 rounded-full bg-gray-600;
}

.nav-subitem.active .nav-subitem-dot {
  @apply bg-white;
}

.nav-subitem.active {
  @apply text-white bg-gray-800 font-semibold;
}

.nav-subitem::before {
  content: '';
  @apply absolute left-0 top-1/2 -translate-y-1/2;
  @apply w-0.5 h-0 bg-white rounded-full transition-all;
}

.nav-subitem.active::before {
  @apply h-6;
}

.badge-notification {
  @apply ml-auto px-2.5 py-1 bg-red-500 text-white;
  @apply text-xs rounded-full font-bold shadow-lg animate-pulse;
}

/* Dropdown transition */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  max-height: 0;
  transform: translateY(-10px);
}

.dropdown-enter-to,
.dropdown-leave-from {
  opacity: 1;
  max-height: 200px;
  transform: translateY(0);
}

/* Footer */
.sidebar-footer {
  @apply p-4 border-t border-gray-800;
}

.user-card {
  @apply flex items-center space-x-4 p-4 rounded-xl;
  @apply bg-gray-800 hover:bg-gray-700 cursor-pointer;
  @apply transition-all;
}

.user-avatar-container {
  @apply relative;
}

.user-avatar {
  @apply w-12 h-12 rounded-xl bg-white flex items-center justify-center shadow-lg;
}

.status-indicator {
  @apply absolute -bottom-1 -right-1 w-4 h-4;
  @apply bg-green-500 border-2 border-gray-900 rounded-full;
}

.user-details {
  @apply flex-1;
}

.user-name {
  @apply text-sm font-bold text-white;
}

.user-role {
  @apply text-xs text-gray-400 font-medium;
}

.logout-btn {
  @apply p-2.5 rounded-lg bg-gray-900 text-gray-400;
  @apply hover:bg-red-600 hover:text-white;
  @apply transition-all shadow-md;
}

.user-card:hover .logout-btn {
  @apply scale-110;
}

/* Responsive */
@media (max-width: 1024px) {
  .sidebar {
    @apply w-20;
  }
  
  .sidebar-header,
  .user-details,
  .nav-item-label,
  .nav-section-title,
  .badge-notification,
  .nav-chevron {
    @apply hidden;
  }
  
  .nav-item,
  .nav-subitem {
    @apply justify-center px-2;
  }
  
  .nav-item.active::after {
    @apply hidden;
  }
  
  .nav-submenu {
    @apply ml-0 pl-0 border-l-0;
  }
  
  .user-card {
    @apply justify-center space-x-0;
  }
  
  .logout-btn {
    @apply hidden;
  }
}
</style>