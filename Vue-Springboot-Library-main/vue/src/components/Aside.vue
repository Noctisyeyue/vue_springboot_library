<template>
<div style="position: fixed; top: 50px; left: 0; bottom: 0; z-index: 999;">

  <el-menu
      style="width: 200px; height: 100%;"
      :default-active="path"
      class="el-menu-vertical-demo"
      router
      background-color="#30333c" text-color="#fff"
  >
  <!--
  router  启用路由模式   
  :default-active="path"  高亮显示
  -->
    <!-- 仪表板菜单项：系统首页，所有用户都可以访问 -->
    <el-menu-item index="/dashboard">
      <svg class="icon" aria-hidden="true">
        <use xlink:href="#icondashboard" />    <!-- 仪表盘图标 -->
      </svg>
      <span>展示板</span>
    </el-menu-item>
    <!-- 个人信息子菜单：包含个人信息修改和密码修改功能 -->
    <el-sub-menu index="2" text-color="#fff">
      <template #title>                           <!-- 子菜单标题模板 -->
        <svg class="icon" aria-hidden="true">
          <use xlink:href="#icon-mingpian" />     <!-- 名片图标 -->
        </svg>
        <span>个人信息</span>
      </template>

      <!-- 修改个人信息：所有用户都可以访问自己的信息页面 -->
      <el-menu-item index="/person" style="color: white">
        <svg class="icon" aria-hidden="true">
          <use xlink:href="#icon-a-bianji1" />    <!-- 编辑图标 -->
        </svg>
        <span>修改个人信息</span>
      </el-menu-item>

      <!-- 修改密码：所有用户都可以修改自己的登录密码 -->
      <el-menu-item index="/password">
        <svg class="icon" aria-hidden="true">
          <use xlink:href="#icon-mima" />         <!-- 密码图标 -->
        </svg>
        <span>修改密码</span>
      </el-menu-item>
    </el-sub-menu>
    <!-- 读者管理：仅管理员 (role == 1) 可见的系统管理功能 -->
    <el-menu-item index="/user" v-if="user.role == 1">
      <svg class="icon" aria-hidden="true">
        <use xlink:href="#iconreader" />         <!-- 读者图标 -->
      </svg>
      <span>读者管理</span>
    </el-menu-item>

    <!-- 书籍管理：仅管理员 (role == 1) 可见的图书管理功能 -->
    <el-menu-item index="/book" v-if="user.role == 1">
      <svg class="icon" aria-hidden="true">
        <use xlink:href="#iconbook" />           <!-- 书籍图标 -->
      </svg>
      <span>书籍管理</span>
    </el-menu-item>
    <!-- 图书查询：仅普通用户 (role == 2) 可见的图书查询功能 -->
    <el-menu-item index="/book" v-if="user.role == 2">
      <svg class="icon" aria-hidden="true">
        <use xlink:href="#iconbook" />           <!-- 书籍图标 -->
      </svg>
      <span>图书查询</span>
    </el-menu-item>
    <!-- 借阅管理：仅管理员 (role == 1) 可见的借阅记录管理 -->
    <el-menu-item index="/lendrecord" v-if="user.role == 1">
      <svg class="icon" aria-hidden="true">
        <use xlink:href="#iconlend-record" />    <!-- 借阅记录图标 -->
      </svg>
      <span>借阅管理</span>
    </el-menu-item>

    <!-- 借阅信息：仅普通用户 (role == 2) 可见的个人借阅记录查询 -->
    <el-menu-item index="/lendrecord" v-if="user.role == 2">
      <svg class="icon" aria-hidden="true">
        <use xlink:href="#iconlend-record" />    <!-- 借阅记录图标 -->
      </svg>
      <span>借阅信息</span>
    </el-menu-item>
    <!-- 我的借阅：仅普通用户 (role == 2) 可见的当前借阅书籍列表 -->
    <el-menu-item index="/bookwithuser" v-if="user.role == 2">
      <el-icon><grid /></el-icon>                  <!-- 网格图标 -->
      <span>我的借阅</span>
    </el-menu-item>

    <!-- 在借书籍：仅管理员 (role == 1) 可见的系统在借书籍管理 -->
    <el-menu-item index="/bookwithuser" v-if="user.role == 1">
      <el-icon><grid /></el-icon>                  <!-- 网格图标 -->
      <span>在借书籍</span>
    </el-menu-item>

    <!-- 我的收藏：仅普通用户 (role == 2) 可见的个人收藏书籍列表 -->
    <el-menu-item index="/myCollection" v-if="user.role == 2">
      <el-icon><Star /></el-icon>                  <!-- 星星图标 -->
      <span>我的收藏</span>
    </el-menu-item>
  </el-menu>

</div>
</template>

<script>



export default {
  name: "Aside",
  components:{},
  created(){
    //页面加载时，去浏览器缓存 sessionStorage 拿用户信息
    let userStr = sessionStorage.getItem("user") ||"{}"
    this.user = JSON.parse(userStr)
  },
  data(){
    return {
      user:{},
      path: this.$route.path //获取当前浏览器地址栏的路径   实现高亮显示
    }
  }
}
</script>

<style scoped>
.icon {
  width: 30px;
  height: 30px;
  padding-top: 5px;
  padding-right: 10px;
}
</style>