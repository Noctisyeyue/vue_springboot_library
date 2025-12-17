<template>
  <div>
    <Header/> <!-- 顶部导航栏 -->
    <div style="display: flex"> <!-- 弹性布局容器 -->
      <Aside/>   <!-- 左侧边栏 -->
      <router-view style="flex: 1;              /* 占据剩余空间 */
                          padding: 20px;
                          margin-left: 200px;    /* 左边距，给侧边栏留位置 */
                          margin-top: 50px;       /* 上边距，给顶部栏留位置 */
                          height: calc(100vh - 50px); /* 高度 = 屏幕高度 - 顶部高度 */
                          overflow-y: auto;/* 内容超出时显示滚动条 */"/> <!-- 右侧内容区域 -->
    </div>
  </div>
</template>

<script>

import Header from "../components/Header";
import Aside from "../components/Aside";
export default {
  name: "Layout",
  components:{//注册子组件 Layout组件可以使用Header和Aside组件
    Header,
    Aside
  }
}
</script>

<style scoped>
</style>