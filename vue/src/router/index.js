import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'root', component: HomeView },
    { path: '/home', name: 'home', component: HomeView },
    { path: '/login', name: 'login', beforeEnter() {location.href = '/login'} },
    { path: '/texts', component: () => import('../views/Texts.vue') },
    { path: '/texts/:textKey', component: () => import('../views/EditText.vue') },
    { path: '/questions', component: () => import('../views/Questions.vue') },
    { path: '/newquestion', component: () => import('../views/EditQuestion.vue') },
    { path: '/questions/:questionId', component: () => import('../views/EditQuestion.vue') },
    { path: '/translates/:translatesId', component: () => import('../views/EditQuestion.vue') },
    { path: '/topics', component: () => import('../views/Topics.vue') },
    { path: '/requirements', component: () => import('../views/Requirements.vue') },
    { path: '/newrequirement', component: () => import('../views/EditRequirement.vue') },
    { path: '/requirements/:requirementId', component: () => import('../views/EditRequirement.vue') },
    { path: '/exams', component: () => import('../views/Exams.vue') },
    { path: '/examQuestions/:examId', component: () => import('../components/TableExam.vue') },
    { path: '/examQuestion/:examQuestionId', component: () => import('../views/EditExamQuestion.vue') },
    { path: '/exam/:examId', component: () => import('../views/EditExam.vue') },
    { path: '/newexam', component: () => import('../views/NewExam.vue') },
    { path: '/pictures', component: () => import('../views/Pictures.vue') },
    { path: '/newpicture', component: () => import('../views/UploadPicture.vue') },
    { path: '/pictures/:pictureId', component: () => import('../views/UploadPicture.vue') },
    { path: '/users', component: () => import('../views/users/Users.vue') },
    { path: '/newuser', component: () => import('../views/users/NewUser.vue') },
    { path: '/myaccount', component: () => import('../views/users/MyAccount.vue') },
  ]
})

export default router
