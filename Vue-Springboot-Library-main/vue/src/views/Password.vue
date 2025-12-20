<template>
<!-- 密码修改页面容器 -->
<div>
  <!-- Element Plus 卡片组件：包含表单内容 -->
  <el-card style="width: 40%; margin-left: 120px; margin-top: 40px">
    <!-- Element Plus 表单组件：包含数据绑定和验证规则 -->
    <el-form
        ref="form"
        :model="form"
        status-icon
        :rules="rules"
        label-width="100px"
    >
    <!--
     status-icon  显示验证状态图标 
    -->
      <!-- 老密码输入框：用于验证用户当前密码 -->
      <el-form-item label="老密码" prop="password2">
        <el-input
            v-model="form.password2"
            type="password"      
            autocomplete="off"   
        ></el-input>
      </el-form-item>
      <!-- autocomplete="off" 关闭自动完成功能 -->

      <!-- 新密码输入框：设置新的登录密码 -->
      <el-form-item label="新密码" prop="password">
        <el-input
            v-model="form.password"
            type="password"
            autocomplete="off"
        ></el-input>
      </el-form-item>

      <!-- 确认新密码输入框：验证新密码输入一致性 -->
      <el-form-item label="确认新密码" prop="checkpassword">
        <el-input
            v-model="form.checkpassword"
            type="password"      
            autocomplete="off"   
        ></el-input>
      </el-form-item>

      <!-- 操作按钮组：提交和重置 -->
      <el-form-item>
        <el-button type="primary" @click="submitForm" style="text-align: center">提交</el-button>
        <el-button @click="resetForm('form')" style="text-align: center">重置</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</div>
</template>

<script>
// 导入所需模块
import request from "../utils/request";        // HTTP 请求工具
import {ElMessage} from "element-plus";        // Element Plus 消息提示组件

export default {
  name: "Password",
  data() {
    /**
     * 老密码验证器：验证当前密码是否正确
     * @param {Object} rule - 验证规则对象
     * @param {string} value - 输入的密码值
     * @param {Function} callback - 验证结果回调函数
     */
    const validatePass2 = (rule, value, callback) => {
      if (value == '') {
        callback(new Error('请输入老密码'))  // 空值验证
      } else {
        // 验证老密码是否正确
        if (this.form.password2 !== this.form.truepassword) {
          callback(new Error('密码错误'))    // 密码不匹配
        }
        callback()  // 验证通过
      }
    }

    /**
     * 新密码验证器：验证新密码是否符合要求
     * @param {Object} rule - 验证规则对象
     * @param {string} value - 输入的密码值
     * @param {Function} callback - 验证结果回调函数
     */
    const validatePass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入新密码'))  // 空值验证
      } else if (value.length < 3) {
        callback(new Error('新密码长度不能少于3位'))  // 最小长度验证
      } else if (value.length > 12) {
        callback(new Error('新密码长度不能超过12位'))  // 最大长度验证
      } else if (value === this.form.password2) {
        callback(new Error('新密码不能与老密码相同'))  // 新老密码不能相同
      } else {
        callback()  // 验证通过
      }
    }

    /**
     * 确认密码验证器：验证两次密码输入是否一致
     * @param {Object} rule - 验证规则对象
     * @param {string} value - 确认密码值
     * @param {Function} callback - 验证结果回调函数
     */
    const validatePass3 = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'))  // 空值验证
      } else if (value !== this.form.password) {
        callback(new Error("两次输入密码不匹配"))  // 密码一致性验证
      } else {
        callback()  // 验证通过
      }
    }

    return {
      form: {
        password2: '',         // 存储老密码输入
        password: '',          // 存储新密码输入
        checkpassword: '',     // 存储确认密码输入
        truepassword: '',      // 存储用户真实密码（从sessionStorage获取）
      },
      form2: {                 //发送后端
        password: '',          // 存储新密码
        id: 0                 // 用户ID，用于后端更新
      },
      rules: {                // Element Plus 表单验证规则配置
        password: [// 新密码验证
            { 
              validator: validatePass, 
              trigger: 'blur', 
              required: true 
            }
          ],      
        checkpassword: [// 确认密码验证
          {
            validator: validatePass3,
            trigger: 'blur',
            required: true 
          }
        ], 
        password2: [// 老密码验证
          { 
            validator: validatePass2,
            trigger: 'blur',
            required: true 
          }
        ],    
      },
    }
  },


  created() {
    let user = JSON.parse(sessionStorage.getItem("user"))  // 获取缓存的用户信息
    this.form.truepassword = user.password                  // 设置用户真实密码用于验证
    this.form2.id = user.id                                // 设置用户ID用于更新
  },

  methods: {
    /**
     * 密码修改提交方法
     * 1. 验证表单输入
     * 2. 发送密码修改请求
     * 3. 处理修改结果
     * 4. 清空缓存并跳转登录页面
     */
    submitForm() {
      // 使用 Element Plus 表单验证方法验证所有字段
      this.$refs.form.validate((valid) => {
        if (valid) {  // 表单验证通过
          // 同步新密码到form2对象
          this.form2.password = this.form.password
          // 发送密码修改请求到后端
          request.put("/user", this.form2).then(res => {
            if (res.code == 0) {  // 修改成功
              ElMessage.success("密码修改成功,请重新登录")

              // 清空缓存的用户信息
              sessionStorage.removeItem("user")
              // 跳转登录界面
              this.$router.push("/login")
            } else {  // 修改失败
              ElMessage.error(res.msg)  // 显示后端返回的错误信息
            }
          })
        }
      })
    },

    /**
     * 重置表单方法
     * 重置所有密码输入框和相关数据
     */
    resetForm(formName) {
      // 重置Element Plus表单字段
      this.$refs[formName].resetFields()
      // 清空form2对象中的密码
      this.form2.password = ''
    },
  },
}
</script>

<style scoped>
/* 密码修改页面样式：使用 Element Plus 默认样式 */
</style>
