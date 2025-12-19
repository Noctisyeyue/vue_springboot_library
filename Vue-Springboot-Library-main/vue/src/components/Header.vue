<template>
 <div style="height: 50px; line-height:50px; border-bottom: 1px solid #ccc; display: flex; position: fixed; top: 0; left: 0; right: 0; z-index: 1000; background-color: white;">
   <div style="width: 200px; padding-left:30px; font-weight: bold; color:dodgerblue">
     <img :src="imgUrl" class="icon" >
     图书馆管理系统</div>
   <div style="flex: 1"></div><!--把用户名推到最右边-->
   <div style="width: 100px">
     <el-dropdown><!--下拉菜单-->
      <span class="el-dropdown-link">
        {{user.nickName}} <el-icon class="el-icon--right">
          <arrow-down />
          </el-icon>
      </span>
       <template #dropdown>
         <el-dropdown-menu>
           <el-dropdown-item @click="exit">退出系统</el-dropdown-item>
         </el-dropdown-menu>
       </template>
     </el-dropdown>
   </div>
 </div>
</template>

<script>
import {ElMessage} from "element-plus";

export default {
  name: "Header",
  props: ['user'],
  data(){
    return{
      user:[],
      imgUrl:require("../assets/icon/login.png")
    }
  },
  created(){
    this.loadUserInfo()
  },
  mounted() {
    // 定时检查用户信息更新（每5秒检查一次）
    this.timer = setInterval(() => {
      this.loadUserInfo()
    }, 5000)
  },
  beforeUnmount() {
    // 组件销毁时清除定时器
    if (this.timer) {
      clearInterval(this.timer)
    }
  },
  methods:{
    exit(){
      sessionStorage.removeItem("user")
      this.$router.push("/login")
      ElMessage.success("退出系统成功")
    },
    // 加载用户信息方法
    loadUserInfo() {
      let userStr = sessionStorage.getItem("user") || "{}"
      this.user = JSON.parse(userStr)
    }
  }

}
</script>

<style scoped>
.icon {
  width: 40px;
  height: 40px;
  padding-top: 5px;
  padding-right: 10px;
}
</style>