import { createRouter, createWebHistory } from 'vue-router'
import Layout from "../layout/Layout";

const routes = [
  {
    path: '/',           // 根路径
    name: 'Layout',      // 路由名称
    redirect: (to) => {
      // 根据用户角色重定向
      const userStr = sessionStorage.getItem("user") || "{}";
      const user = JSON.parse(userStr);
      if (user && user.role == 1) {
        return '/dashboard';  // 管理员跳转到展示板
      } else {
        return '/home';       // 普通用户跳转到首页
      }
    },
    component: Layout,   // 使用 Layout 布局组件
    children:[            // 子路由（嵌套在 Layout 内显示） 都会被渲染到 Layout 组件内部的 <router-view></router-view> 标签所在的位置
      {
        path:'home',
        name:'Home',
        component:() => import("@/views/Home")
      },
      {
        path:'user',      //浏览器地址栏中看到的路径
        name:'user',
        component:() => import("@/views/User")  //路由懒加载  按需加载
      },
      {
        path: 'book',
        name: 'book',
        component: () => import("@/views/Book")
      },
      {
        path: 'person',
        name: 'Person',
        component: () => import("@/views/Person")
      },
      {
        path: 'password',
        name: 'Password',
        component: () => import("@/views/Password")
      },
      {
        path: 'lendrecord',
        name: 'LendRecord',
        component: () => import("@/views/LendRecord")
      },
      {
        path:'dashboard',
        name:'Dashboard',
        component:() => import("@/views/Dashboard")
      },
      {
        path: 'bookwithuser',
        name: 'BookWithUser',
        component: () => import("@/views/BookWithUser")
      },
      {
      path: 'myCollection',
      name: 'MyCollection',
      component: () => import('@/views/MyCollection'),
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import("@/views/Login")
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import("@/views/Register")
  },


]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),  //指定路由模式为 history 模式
  routes
})

export default router
