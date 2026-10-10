import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useGridAuthStore } from '../stores/gridAuth'
import { usePublicAuthStore } from '../stores/publicAuth'
import AdminLayout from '../layouts/AdminLayout.vue'
import LoginView from '../pages/LoginView.vue'
import GridLoginView from '../pages/GridLoginView.vue'
import PublicLoginView from '../pages/PublicLoginView.vue'
import FeedbackListView from '../pages/FeedbackListView.vue'
import FeedbackDetailView from '../pages/FeedbackDetailView.vue'
import FeedbackStatsView from '../pages/FeedbackStatsView.vue'
import GridTaskListView from '../pages/GridTaskListView.vue'
import GridTaskDetailView from '../pages/GridTaskDetailView.vue'
import PublicFeedbackSubmitView from '../pages/PublicFeedbackSubmitView.vue'
import PublicRegisterView from '../pages/PublicRegisterView.vue'

const routes = [
  {
    path: '/',
    redirect: '/feedback',
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { public: true },
  },
  {
    path: '/grid/login',
    name: 'gridLogin',
    component: GridLoginView,
    meta: { public: true },
  },
  {
    path: '/public/login',
    name: 'publicLogin',
    component: PublicLoginView,
    meta: { public: true },
  },
  {
    path: '/public/feedback/submit',
    name: 'publicFeedbackSubmit',
    component: PublicFeedbackSubmitView,
    meta: { requiresPublicAuth: true },
  },
  {
    path: '/public/register',
    name: 'publicRegister',
    component: PublicRegisterView,
    meta: { public: true },
  },
  {
    path: '/grid/tasks',
    name: 'gridTaskList',
    component: GridTaskListView,
    meta: { requiresGridAuth: true },
  },
  {
    path: '/grid/tasks/:afId',
    name: 'gridTaskDetail',
    component: GridTaskDetailView,
    meta: { requiresGridAuth: true },
  },
  {
    path: '/',
    component: AdminLayout,
    children: [
      {
        path: 'feedback',
        name: 'feedbackList',
        component: FeedbackListView,
      },
      {
        path: 'feedback/:afId',
        name: 'feedbackDetail',
        component: FeedbackDetailView,
      },
      {
        path: 'statistics',
        name: 'feedbackStats',
        component: FeedbackStatsView,
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const authStore = useAuthStore()
  const gridAuthStore = useGridAuthStore()
  const publicAuthStore = usePublicAuthStore()

  if (to.meta.requiresGridAuth && !gridAuthStore.isLogin) {
    return {
      path: '/grid/login',
      query: { redirect: to.fullPath },
    }
  }

  if (to.meta.requiresPublicAuth && !publicAuthStore.isLogin) {
    return {
      path: '/public/login',
      query: { redirect: to.fullPath },
    }
  }

  if (to.name === 'gridLogin' && gridAuthStore.isLogin) {
    return '/grid/tasks'
  }

  if ((to.name === 'publicLogin' || to.name === 'publicRegister') && publicAuthStore.isLogin) {
    return '/public/feedback/submit'
  }

  if (to.name === 'login') {
    if (authStore.isLogin) {
      return '/feedback'
    }

    if (gridAuthStore.isLogin) {
      return '/grid/tasks'
    }

    if (publicAuthStore.isLogin) {
      return '/public/feedback/submit'
    }
  }

  if (
    !to.meta.public &&
    !to.meta.requiresGridAuth &&
    !to.meta.requiresPublicAuth &&
    !authStore.isLogin
  ) {
    return {
      path: '/login',
      query: { redirect: to.fullPath },
    }
  }

  return true
})

export default router
