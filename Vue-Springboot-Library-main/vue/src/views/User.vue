<template>
<!-- 用户管理页面容器 -->
<div style="padding: 10px">
  <!-- 搜索表单区域：多条件搜索用户 -->
  <div style="margin: 10px 0;">
    <el-form inline="true" size="small">
      <!-- 读者编号搜索输入框 -->
      <el-form-item label="读者编号">
        <el-input v-model="search1" placeholder="请输入读者编号" clearable>
          <template #prefix>
            <el-icon class="el-input__icon"><search/></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <!-- 姓名搜索输入框 -->
      <el-form-item label="姓名">
        <el-input v-model="search2" placeholder="请输入姓名" clearable>
          <template #prefix>
            <el-icon class="el-input__icon"><search /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <!-- 电话号码搜索输入框 -->
      <el-form-item label="电话号码">
        <el-input v-model="search3" placeholder="请输入电话号码" clearable>
          <template #prefix>
            <el-icon class="el-input__icon"><search /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <!-- 查询按钮：执行搜索操作 -->
      <el-form-item>
        <el-button type="primary" style="margin-left: 1%" @click="load" size="mini">查询</el-button>
      </el-form-item>

      <!-- 重置按钮：清空搜索条件 -->
      <el-form-item>
        <el-button size="mini" type="danger" @click="clear">重置</el-button>
      </el-form-item>
    </el-form>
  </div>

  <!-- 操作按钮区域：批量删除功能（仅管理员可见） -->
  <div style="margin: 10px 0;">
    <el-popconfirm title="确认删除?" @confirm="deleteBatch" v-if="user.role == 1">
      <template #reference>
        <el-button type="danger" size="mini">批量删除</el-button>
      </template>
    </el-popconfirm>
  </div>
<!-- 用户数据表格：显示用户列表信息 -->
<el-table :data="tableData" stripe border="true" @selection-change="handleSelectionChange" style="width: 100%;">
  <!-- 多选列：仅管理员可见，用于批量删除 -->
  <el-table-column v-if="user.role == 1"
                   type="selection"
                   width="55">
  </el-table-column>

  <!-- 读者编号列：支持排序 -->
  <el-table-column prop="id" label="读者编号" sortable />

  <!-- 用户名列 -->
  <el-table-column prop="username" label="用户名" />

  <!-- 姓名列 -->
  <el-table-column prop="nickName" label="姓名" />

  <!-- 电话号码列 -->
  <el-table-column prop="phone" label="电话号码" />

  <!-- 性别列 -->
  <el-table-column prop="sex" label="性别" />

  <!-- 地址列 -->
  <el-table-column prop="address" label="地址" />

  <!-- 操作列：固定在右侧，包含编辑和删除按钮 -->
  <el-table-column fixed="right" label="操作" width="180">
    <template v-slot="scope">
      <!-- 编辑按钮：打开编辑对话框 -->
      <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>

      <!-- 删除按钮：带确认弹窗 -->
      <el-popconfirm title="确认删除?" @confirm="handleDelete(scope.row.id)">
        <template #reference>
          <el-button type="danger" size="mini">删除</el-button>
        </template>
      </el-popconfirm>
    </template>
  </el-table-column>
</el-table>
<!-- 分页组件：控制数据分页显示 -->
<div style="margin: 10px 0">
  <el-pagination
      v-model:currentPage="currentPage"
      :page-sizes="[5, 10, 20]"
      :page-size="pageSize"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange">
  </el-pagination>
</div>

<!-- 编辑对话框：用于新增或编辑用户信息 -->
<el-dialog v-model="dialogVisible" title="编辑读者信息" width="30%">
  <el-form :model="form" label-width="120px">
    <!-- 用户名输入框 -->
    <el-form-item label="用户名">
        <el-input
          style="width: 80%"
          v-model="form.username"
          maxlength="20"
          show-word-limit
          placeholder="请输入姓名（最多20字）"
        ></el-input>
    </el-form-item>

    <!-- 昵称输入框 -->
    <el-form-item label="昵称">
        <el-input
          style="width: 80%"
          v-model="form.nickName"
          maxlength="20"
          show-word-limit
          placeholder="请输入姓名（最多20字）"
        ></el-input>
    </el-form-item>

    <!-- 电话号码输入框 -->
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

    <!-- 地址输入框：多行文本 -->
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

  <!-- 对话框底部操作按钮 -->
  <template #footer>
    <span class="dialog-footer">
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="save">确 定</el-button>
    </span>
  </template>
</el-dialog>
</div>
</template>

<script>
// 导入所需模块
import request from "../utils/request";        // HTTP 请求工具
import {ElMessage} from "element-plus";        // Element Plus 消息提示组件

export default {
  name: "User",

  /**
   * 组件创建时执行：
   * 1. 加载用户数据
   * 2. 获取当前登录用户信息
   */
  created() {
    this.load()  // 加载用户列表数据
    let userStr = sessionStorage.getItem("user") || "{}"  // 获取当前用户信息
    this.user = JSON.parse(userStr)  // 解析为对象
  },

  methods: {
    /**
     * 表格多选处理方法
     * 当用户选择表格行时触发，收集选中用户的ID
     * @param {Array} val - 选中的行数据数组
     */
    handleSelectionChange(val) {
      this.ids = val.map(v => v.id)  // 提取所有选中行的ID
    },

    /**
     * 批量删除方法
     * 删除选中的多个用户记录
     */
    deleteBatch() {
      // 检查是否选择了要删除的数据
      if (!this.ids.length) {
        ElMessage.warning("请选择数据！")
        return
      }

      // 发送批量删除请求，优化：一次性发送ID数组而非逐个删除
      request.post("/user/deleteBatch", this.ids).then(res => {
        if (res.code === '0') {
          ElMessage.success("批量删除成功")
          this.load()  // 重新加载数据
        } else {
          ElMessage.error(res.msg)
        }
      })
    },

    /**
     * 加载用户数据方法
     * 根据分页和搜索条件获取用户列表
     */
    load() {
      request.get("user/usersearch", {
        params: {
          pageNum: this.currentPage,    // 当前页码
          pageSize: this.pageSize,      // 每页大小
          search1: this.search1,        // 读者编号搜索条件
          search2: this.search2,        // 姓名搜索条件
          search3: this.search3,        // 电话号码搜索条件
        }
      }).then(res => {
        console.log(res)  // 调试输出
        this.tableData = res.data.records  // 设置表格数据
        this.total = res.data.total        // 设置总记录数
      })
    },

    /**
     * 清空搜索条件方法
     * 重置所有搜索字段并重新加载数据
     */
    clear() {
      this.search1 = ""  // 清空读者编号
      this.search2 = ""  // 清空姓名
      this.search3 = ""  // 清空电话号码
      this.load()        // 重新加载数据
    },

    /**
     * 单个用户删除方法
     * 删除指定ID的用户记录
     * @param {Number} id - 要删除的用户ID
     */
    handleDelete(id) {
      request.delete("user/" + id).then(res => {
        console.log(res)
        if (res.code == 0) {
          ElMessage.success("删除成功")
        } else {
          ElMessage.error(res.msg)
        }
        this.load()  // 重新加载数据
      })
    },


    /**
     * 保存用户信息方法（仅更新操作）
     */
    save() {
      if (!this.form.id) {
        ElMessage.error("非法操作：无法新增用户")
        return
      }
      // 更新操作
      request.put("/user", this.form).then(res => {
        console.log(res)
        if (res.code == 0) {
          ElMessage({
            message: '更新成功',
            type: 'success',
          })
        } else {
          ElMessage.error(res.msg)
        }
        this.load()           // 重新加载数据
        this.dialogVisible = false  // 关闭对话框
      })
    },

    /**
     * 编辑用户方法
     * 打开编辑对话框，填充要编辑的用户数据
     * @param {Object} row - 要编辑的用户行数据
     */
    handleEdit(row) {
      // 深拷贝避免直接修改原数据
      this.form = JSON.parse(JSON.stringify(row))//先转成 JSON 字符串，再把这个字符串解析回一个新对象
      this.dialogVisible = true  // 显示对话框
    },

    /**
     * 分页大小变化处理方法
     * 当用户改变每页显示数量时触发
     * @param {Number} pageSize - 新的页面大小
     */
    handleSizeChange(pageSize) {
      this.pageSize = pageSize  // 更新页面大小
      this.load()              // 重新加载数据
    },

    /**
     * 当前页变化处理方法
     * 当用户切换页面时触发
     * @param {Number} pageNum - 新的页码
     */
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum  // 更新页码
      this.load()             // 重新加载数据
    },
  },

  data() {
    return {
      form: {},                // 存储表单数据（新增或编辑的用户信息）
      dialogVisible: false,    // 控制编辑对话框的显示/隐藏状态
      search1: '',             // 读者编号搜索条件
      search2: '',             // 姓名搜索条件
      search3: '',             // 电话号码搜索条件
      total: 10,               // 数据总记录数（用于分页）
      currentPage: 1,          // 当前页码
      pageSize: 10,            // 每页显示记录数
      tableData: [],           // 表格显示的用户数据数组
      user: {},                // 当前登录用户信息（用于权限控制）
      ids: [],                 // 批量删除选中的用户ID数组
    }
  }
}
</script>
<style scoped>
</style>
