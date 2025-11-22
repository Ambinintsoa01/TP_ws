import { createRouter, createWebHistory } from "vue-router";
import Dashboard from "../views/Dashboard.vue";
import Personnels from "../views/Personnels.vue";
import Notes from "../views/Notes.vue";
import Students from "../views/Students.vue";
import Semesters from "../views/Semesters.vue";
import Login from "../views/Login.vue";
import SemesterAverages from "../views/SemesterAverages.vue";
import StudentGrades from "../views/StudentGrades.vue";
import StudentLevelNotes from "../views/StudentLevelNotes.vue";
import OptionS4 from "../views/OptionS4.vue";
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: "/", name: "dashboard", component: Dashboard },
    { path: "/personnels", name: "personnels", component: Personnels },
    { path: "/notes", name: "notes", component: Notes },
    { path: "/students", name: "students", component: Students },
    { path: "/semesters", name: "semesters", component: Semesters },
    { path: "/semesters/:label", name: "semester-averages", component: SemesterAverages, props: true },
    { path: "/students/:id/notes/semesters/:semester", name: "student-notes-semester", component: StudentGrades, props: true },
    { path: "/students/:id/notes/levels/:level", name: "student-notes-level", component: StudentLevelNotes, props: true },
    { path: "/options", name: "options", component: OptionS4 },
    { path: "/login", name: "login", component: Login },
  ],
});

// simple auth check (token stored in localStorage)
function isAuthenticated() {
  try {
    const token = localStorage.getItem('token');
    return !!token;
  } catch (e) {
    return false;
  }
}

// Protect routes: redirect to /login when not authenticated
router.beforeEach((to, from, next) => {
  if (to.path === '/login') {
    // if already logged in, go to dashboard
    if (isAuthenticated()) {
      return next({ path: '/' });
    }
    return next();
  }

  // allow public assets or api calls handled by server; protect SPA routes
  if (!isAuthenticated()) {
    return next({ path: '/login', query: { redirect: to.fullPath } });
  }

  next();
});

export default router;
