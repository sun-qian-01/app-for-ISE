import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "../stores/auth";

const routes = [
  {
    path: "/",
    name: "root",
    component: () => import("../views/RootRedirectView.vue"),
  },
  {
    path: "/login",
    name: "login",
    component: () => import("../views/LoginView.vue"),
    meta: { public: true },
  },
  {
    path: "/403",
    name: "forbidden",
    component: () => import("../views/ForbiddenView.vue"),
    meta: { public: true },
  },
  {
    path: "/student",
    component: () => import("../layouts/StudentLayout.vue"),
    meta: { requiresAuth: true, allowedRoles: ["student", "class_cadre"] },
    children: [
      {
        path: "dashboard",
        name: "student-dashboard",
        component: () => import("../views/student/StudentDashboardView.vue"),
      },
      {
        path: "kb",
        name: "student-kb",
        component: () => import("../views/student/StudentKnowledgeView.vue"),
      },
      {
        path: "kb/articles/:articleId",
        name: "student-kb-article",
        component: () => import("../views/shared/KnowledgeArticleDetailView.vue"),
      },
      {
        path: "party",
        name: "student-party",
        component: () => import("../views/student/StudentPartyView.vue"),
      },
      {
        path: "notices",
        name: "student-notices",
        component: () => import("../views/student/StudentNoticesView.vue"),
      },
      {
        path: "notices/:noticeId",
        name: "student-notice-detail",
        component: () => import("../views/shared/NoticeDetailView.vue"),
      },
      {
        path: "applications",
        name: "student-applications",
        component: () => import("../views/student/StudentApplicationsView.vue"),
      },
      {
        path: "profile",
        name: "student-profile",
        component: () => import("../views/student/StudentProfileView.vue"),
      },
      {
        path: "honors",
        name: "student-honors",
        component: () => import("../views/student/StudentHonorsView.vue"),
      },
    ],
  },
  {
    path: "/admin",
    component: () => import("../layouts/AdminLayout.vue"),
    meta: {
      requiresAuth: true,
      allowedRoles: ["teacher_admin", "college_leader", "system_admin"],
    },
    children: [
      {
        path: "dashboard",
        name: "admin-dashboard",
        component: () => import("../views/admin/AdminDashboardView.vue"),
      },
      {
        path: "students",
        name: "admin-students",
        component: () => import("../views/admin/AdminStudentsView.vue"),
      },
      {
        path: "kb",
        name: "admin-kb",
        component: () => import("../views/admin/AdminKnowledgeView.vue"),
      },
      {
        path: "kb/articles/:articleId",
        name: "admin-kb-article",
        component: () => import("../views/shared/KnowledgeArticleDetailView.vue"),
      },
      {
        path: "party",
        name: "admin-party",
        component: () => import("../views/admin/AdminPartyView.vue"),
      },
      {
        path: "notices",
        name: "admin-notices",
        component: () => import("../views/admin/AdminNoticesView.vue"),
      },
      {
        path: "applications",
        name: "admin-applications",
        component: () => import("../views/admin/AdminApplicationsView.vue"),
      },
      {
        path: "honors",
        name: "admin-honors",
        component: () => import("../views/admin/AdminHonorsView.vue"),
      },
      {
        path: "audit-logs",
        name: "admin-audit-logs",
        component: () => import("../views/admin/AdminAuditLogsView.vue"),
      },
      {
        path: "system-logs",
        name: "admin-system-logs",
        component: () => import("../views/admin/AdminSystemLogsView.vue"),
      },
    ],
  },
  {
    path: "/leader/dashboard",
    name: "leader-dashboard",
    component: () => import("../views/LeaderDashboardView.vue"),
    meta: { requiresAuth: true, allowedRoles: ["college_leader"] },
  },
  {
    path: "/:pathMatch(.*)*",
    name: "not-found",
    component: () => import("../views/NotFoundView.vue"),
    meta: { public: true },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to) => {
  const authStore = useAuthStore();

  if (!authStore.initialized) {
    await authStore.restoreSession();
  }

  if (to.meta.public) {
    return true;
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return { name: "login", query: { redirect: to.fullPath } };
  }

  const allowedRoles = to.meta.allowedRoles ?? [];
  if (allowedRoles.length > 0 && !allowedRoles.some((role) => authStore.roles.includes(role))) {
    return { name: "forbidden" };
  }

  return true;
});

export default router;
