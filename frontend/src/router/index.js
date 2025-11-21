import { createRouter, createWebHistory } from "vue-router";
import Personnels from "../views/personnel/Personnels.vue";
import PersonnelAbsence from "../views/personnel/PersonnelAbsence.vue";
import Dashboard from "../views/Dashboard.vue";
import PersonnelAbsenceDetails from "../views/personnel/PersonnelAbsenceDetails.vue";
import HeuresSupp from "../views/personnel/HeuresSupp.vue";
import HeuresSuppDetails from "../views/personnel/HeuresSuppDetails.vue";
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: "/", name: "dashboard", component: Dashboard },
    { path: "/personnels", name: "personnels", component: Personnels },
    { path: "/personnelAbsences", name: "personnelAbcences", component: PersonnelAbsence },
    {
      path: "/personnel/:id/absences-details",
      name: "personnel-absences-details",
      component: PersonnelAbsenceDetails,
    },
    { path: "/heuresSupp", name: "heuresSupp", component: HeuresSupp },
    {
      path: "/heures-supp/details/:id",
      name: "HeuresSuppDetails",
      component: HeuresSuppDetails,
    },
  ],
});

export default router;
