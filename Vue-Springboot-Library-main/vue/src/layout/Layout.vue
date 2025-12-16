<template>
  <div>
    <Header/>
    <!--    主体-->
    <div style="display: flex">
      <!--      侧边栏-->
      <Aside/>
      <!--      内容区域-->
      <router-view style="flex: 1; padding: 20px; margin-left: 200px; margin-top: 50px; height: calc(100vh - 50px); overflow-y: auto;"/>
    </div>
  </div>
</template>

<script>

import Header from "../components/Header";
import Aside from "../components/Aside";
export default {
  name: "Layout",
  components:{
    Header,
    Aside
  }
}
</script>

<style scoped>
</style>