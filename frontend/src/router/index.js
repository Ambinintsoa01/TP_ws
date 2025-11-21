import { createRouter, createWebHistory } from "vue-router";
import Dashboard from "../views/Dashboard.vue";
import Personnels from "../views/Personnels.vue";
import Notes from "../views/Notes.vue";
import Students from "../views/Students.vue";
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: "/", name: "dashboard", component: Dashboard },
    { path: "/personnels", name: "personnels", component: Personnels },
    { path: "/notes", name: "notes", component: Notes },
    { path: "/students", name: "students", component: Students },
  ],
});

export default router;
