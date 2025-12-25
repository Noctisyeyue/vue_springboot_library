<template>
<!-- 个人信息页面容器 -->
<div>
  <!-- Element Plus 卡片组件：包含个人信息表单 -->
  <el-card style="width: 40%; margin-left: 120px; margin-top: 40px">
    <!-- 页面标题 -->
    <h2 style="padding: 30px">个人信息</h2>
    <!-- Element Plus 表单组件：包含用户数据绑定 -->
    <el-form :model="form" ref="form" label-width="80px">
      <!-- 用户名输入框：只读显示，不可修改 -->
      <el-form-item label="用户名">
        <el-input style="width: 80%" v-model="form.username" disabled></el-input>
      </el-form-item>

      <!-- 姓名输入框：可编辑的用户真实姓名 -->
      <el-form-item label="姓名">
        <el-input style="width: 80%" v-model="form.nickName"></el-input>
      </el-form-item>

      <!-- 权限显示：根据角色显示管理员或读者 -->
      <el-form-item label="权限">
        <span v-if="form.role==1" style="margin:5px">管理员</span>
        <span v-if="form.role==2" style="margin:5px">读者</span>
      </el-form-item>

      <!-- 电话号码输入框：可编辑的联系方式，限制最多11位数字 -->
      <el-form-item label="电话号码">
        <el-input
          style="width: 80%"
          v-model="form.phone"
          maxlength="11"
          show-word-limit
          @input="form.phone = form.phone.replace(/\D/g, '')"
          placeholder="请输入电话号码"
        ></el-input>
        <!--
        限制输入最多11个字符
        显示字数统计（如 0/11）
        实时过滤非数字字符，只保留数字
        -->
      </el-form-item>

      <!-- 性别选择：单选按钮组 -->
      <el-form-item label="性别">
        <div>
          <el-radio v-model="form.sex" label="男">男</el-radio>
          <el-radio v-model="form.sex" label="女">女</el-radio>
        </div>
      </el-form-item>

      <!-- 地址输入框：多行文本输入，限制最多50字 -->
      <el-form-item label="地址">
        <el-input
          type="textarea"
          style="width: 80%"
          v-model="form.address"
          maxlength="50"
          show-word-limit
          placeholder="请输入地址（最多50字）"
        ></el-input>
      </el-form-item>
    </el-form>

    <!-- 操作按钮：保存个人信息 -->
    <div style="text-align: center">
      <el-button type="primary" @click="update">保存</el-button>
    </div>
  </el-card>
</div>
</template>

<script>
// 导入所需模块
import request from "@/utils/request";        // HTTP 请求工具
import {ElMessage} from "element-plus";        // Element Plus 消息提示组件

export default {
  name: "Person",
  data() {
    return {
      form: {}  // 存储用户个人信息数据
    }
  },

  /**
   * 组件创建时执行：从会话存储中获取用户信息
   * 初始化表单数据为当前登录用户的个人信息
   */
  created() {
    let str = sessionStorage.getItem("user") || "{}"  // 获取缓存的用户信息，默认为空对象
    this.form = JSON.parse(str)  // 解析JSON字符串为对象，赋值给表单数据
  },

  methods: {
    /**
     * 个人信息更新方法
     * 1. 发送更新请求到后端
     * 2. 处理更新结果
     * 3. 更新本地缓存
     * 4. 通知父组件更新用户信息显示
     */
    update() {
      // 发送个人信息更新请求到后端
      request.put("/user", this.form).then(res => {
        console.log(res)  // 调试输出后端响应
        if (res.code === '0') {  // 更新成功
          ElMessage.success("更新成功")  // 显示成功提示

          // 更新会话存储中的用户信息
          sessionStorage.setItem("user", JSON.stringify(this.form))

          // 触发自定义事件，通知Layout组件更新用户信息显示
          this.$emit("userInfo")
        } else {  // 更新失败
          ElMessage.error(res.msg)  // 显示后端返回的错误信息
        }
      })
    }
  }
}
</script>

<style scoped>
</style>
