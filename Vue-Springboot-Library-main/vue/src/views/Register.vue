<template>
<!-- 注册页面容器：全屏背景和居中布局 -->
<div class="login-container">
    <!-- Element Plus 表单组件：包含数据绑定和验证规则 -->
    <el-form ref="form" :model="form" :rules="rules" class="login-page">
      <!-- 页面标题 -->
      <h2 class="title" style="margin-bottom: 20px">用户注册</h2>

      <!-- 用户名输入框：包含图标和验证规则 -->
      <el-form-item prop="username">
        <el-input v-model="form.username" placeholder="请输入用户名" clearable>
          <template #prefix>  <!-- 输入框前缀图标插槽 -->
            <el-icon class="el-input__icon"><User/></el-icon>  <!-- 用户图标 -->
          </template>
        </el-input>
      </el-form-item>

      <!-- 密码输入框：包含图标、显示/隐藏功能 -->
      <el-form-item prop="password">
        <el-input v-model="form.password" placeholder="请输入密码" clearable show-password>
          <template #prefix>
            <el-icon class="el-input__icon"><Lock /></el-icon>  <!-- 锁图标 -->
          </template>
        </el-input>
      </el-form-item>

      <!-- 确认密码输入框：用于验证密码一致性 -->
      <el-form-item prop="confirm">
        <el-input v-model="form.confirm" placeholder="请再次确认密码" clearable show-password>
          <template #prefix>
            <el-icon class="el-input__icon"><Lock /></el-icon>  <!-- 锁图标 -->
          </template>
        </el-input>
      </el-form-item>

      <!-- 用户角色选择：单选按钮组 -->
      <el-form-item prop="role">
        <el-radio v-model="form.role" label="2">读者</el-radio>
        <el-radio v-model="form.role" label="1">管理员</el-radio>
      </el-form-item>

      <!-- 管理员注册码输入框：仅在角色为管理员时显示 -->
      <el-form-item prop="authorize" v-if="form.role==1">
        <el-input v-model="form.authorize" placeholder="请输入管理员注册码" clearable show-password>
          <template #prefix>
            <el-icon class="el-input__icon"><Lock /></el-icon>  <!-- 锁图标 -->
          </template>
        </el-input>
      </el-form-item>

      <!-- 验证码输入区域：输入框 + 验证码生成组件 -->
      <el-form-item>
        <div style="display: flex">
          <!-- 验证码输入框 -->
          <el-input v-model="form.validCode" style="width: 45%;" placeholder="请输入验证码"></el-input>
          <!-- 自定义验证码组件：生成图片验证码 -->
          <ValidCode @input="createValidCode" style="width: 50%"/>
        </div>
      </el-form-item>

      <!-- 注册按钮：触发注册方法 -->
      <el-form-item>
        <el-button type="primary" style=" width: 100%" @click="register">注 册</el-button>
      </el-form-item>

      <!-- 登录入口：跳转到登录页面 -->
      <el-form-item><el-button type="text" @click="$router.push('/login')">前往登录>> </el-button></el-form-item>
    </el-form>
</div>

</template>

<script>
// 导入所需模块
import request from "../utils/request";        // HTTP 请求工具
import {ElMessage} from "element-plus";        // Element Plus 消息提示组件
import ValidCode from "../components/Validate";  // 自定义验证码组件

export default {
  name: "Register",  // 组件名称修改为Register以更准确反映功能
  components: {      // 注册自定义组件
    ValidCode
  },
  data() {
    return {
      form: {},           // 存储用户输入的表单数据：用户名、密码、确认密码、角色、注册码、验证码
      validCode: '',      // 存储验证码组件生成的正确验证码
      rules: {            // Element Plus 表单验证规则配置
        username: [
          {
            required: true,        // 必填字段
            message: '请输入用户名',  // 验证失败时的提示信息
            trigger: 'blur',       // 失去焦点时触发验证
          },
          {
            min: 3,               // 最小长度
            max: 20,              // 最大长度
            message: '长度要求为3到20位',  // 长度验证失败时的提示信息
            trigger: 'blur',      // 失去焦点时触发验证
          },
        ],
        password: [
          {
            required: true,        // 必填字段
            message: '请输入密码',  // 验证失败时的提示信息
            trigger: 'blur',       // 失去焦点时触发验证
          },
          {
            min: 3,               // 最小长度
            max: 12,              // 最大长度
            message: '长度要求为3到12位',  // 长度验证失败时的提示信息
            trigger: 'blur',      // 失去焦点时触发验证
          }
        ],
        confirm: [
          {
            required: true,        // 必填字段
            message: "请确认密码",  // 验证失败时的提示信息
            trigger: "blur"        // 失去焦点时触发验证
          }
        ],
        authorize: [
          {
            required: true,        // 必填字段
            message: "请输入注册码",  // 验证失败时的提示信息
            trigger: "blur"        // 失去焦点时触发验证
          }
        ],
      }
    }
  },

  methods: {
    createValidCode(data) {
      this.validCode = data  // 存储正确的验证码，用于后续验证
    },

    /**
     * 用户注册处理方法
     * 1. 验证表单输入
     * 2. 验证验证码
     * 3. 验证密码一致性
     * 4. 验证管理员注册码
     * 5. 发送注册请求
     * 6. 处理注册结果
     */
    register() {
      // 使用 Element Plus 表单验证方法验证所有字段
      this.$refs.form.validate((valid) => {
        if (valid) {  // 表单验证通过
          // 检查是否填写了验证码
          if (!this.form.validCode) {
            ElMessage.error("请填写验证码")
            return
          }

          // 验证码比对：不区分大小写
          if(this.form.validCode.toLowerCase() !== this.validCode.toLowerCase()) {
            ElMessage.error("验证码错误")
            return
          }

          // 验证两次密码输入是否一致
          if(this.form.password != this.form.confirm) {
            ElMessage.error("两次密码输入不一致")
            return
          }

          // 验证管理员注册码：仅在角色为管理员时验证
          if(this.form.role == 1 && this.form.authorize != "1234") {
            ElMessage.error("请输入正确的注册码")
            return
          }

          // 发送注册请求到后端
          request.post("user/register", this.form).then(res => {
            if(res.code == 0) {  // 注册成功
              ElMessage.success("注册成功")

              // 跳转到登录页面
              this.$router.push("/login")
            } else {  // 注册失败
              ElMessage.error(res.msg)  // 显示后端返回的错误信息
            }
          })
        }
      })
    }
  }
}

</script>

<style scoped>
/* 注册页面容器样式：全屏背景 */
.login-container {
  position: fixed;           /* 固定定位，覆盖整个视口 */
  width: 100%;               /* 宽度占满屏幕 */
  height: 100vh;             /* 高度占满视口高度 */
  background: url('../img/bg2.svg');  /* 背景图片 */
  background-size: cover;    /* 背景图片覆盖整个容器，填满无空白 */
  background-position: center; /* 背景图片居中显示 */
  background-repeat: no-repeat;  /* 禁止背景图片重复 */
  overflow: hidden;          /* 隐藏溢出内容 */
}

/* 注册表单卡片样式：居中显示 */
.login-page {
  border-radius: 5px;        /* 圆角边框 */
  margin: 180px auto;        /* 上下边距180px，左右居中 */
  width: 350px;             /* 固定宽度 */
  padding: 35px 35px 15px;  /* 内边距：上35px，左右35px，下15px */
  background: #fff;         /* 白色背景 */
  border: 1px solid #eaeaea;  /* 浅灰色边框 */
  box-shadow: 0 0 25px #cac6c6;  /* 阴影效果 */
}
</style>