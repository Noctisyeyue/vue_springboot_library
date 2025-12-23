<template>
<!-- 我的收藏页面容器：展示用户收藏的图书列表 -->
<div style="padding: 10px">
    <!-- 搜索区域：提供按图书名称和作者搜索功能 -->
    <div style="margin: 10px 0;">
      <el-form inline="true" size="small">
        <!-- 图书名称搜索框：支持清空功能 -->
        <el-form-item label="图书名称">
          <el-input v-model="search1" placeholder="请输入图书名称" clearable>
            <template #prefix><el-icon class="el-input__icon">
                <search />  <!-- 搜索图标 -->
              </el-icon></template>
          </el-input>
        </el-form-item>
        <!-- 作者搜索框：支持清空功能 -->
        <el-form-item label="作者">
          <el-input v-model="search2" placeholder="请输入作者" clearable>
            <template #prefix><el-icon class="el-input__icon">
                <search />  <!-- 搜索图标 -->
              </el-icon></template>
          </el-input>
        </el-form-item>
        <!-- 查询按钮：触发搜索方法 -->
        <el-form-item>
          <el-button type="primary" style="margin-left: 1%" @click="load" size="mini">查询</el-button>
        </el-form-item>
        <!-- 重置按钮：清空搜索条件并重新加载数据 -->
        <el-form-item>
          <el-button size="mini" type="danger" @click="clear">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 批量操作区域：提供批量删除功能 -->
    <div style="margin: 10px 0;">
      <!-- 批量删除按钮：带二次确认弹窗 -->
      <el-popconfirm title="确认删除选中的收藏?" @confirm="deleteBatch">
        <template #reference>
          <el-button type="danger" size="mini">批量删除</el-button>
        </template>
      </el-popconfirm>
    </div>

    <!-- 收藏列表表格：展示用户收藏的所有图书 -->
    <el-table :data="tableData" stripe border @selection-change="handleSelectionChange" style="width: 100%;">
        <!-- 复选框列：支持多选操作 -->
        <el-table-column type="selection" width="55"></el-table-column>
        <!-- 图书编号列 -->
        <el-table-column prop="isbn" label="图书编号" width="150"></el-table-column>
        <!-- 图书名称列：点击可查看详情 -->
        <el-table-column prop="bookName" label="图书名称" min-width="200">
          <template v-slot="scope">
            <el-button type="text" @click="showDetail(scope.row)" style="color: #409EFF; font-weight: 500;">
              {{ scope.row.bookName }}
            </el-button>
          </template>
        </el-table-column>
        <!-- 作者列 -->
        <el-table-column prop="author" label="作者" width="150"></el-table-column>
        <!-- 收藏时间列：格式化显示 -->
        <el-table-column prop="collectionTime" label="收藏时间" width="180">
          <template v-slot="scope">
            {{ formatDate(scope.row.collectionTime) }}
          </template>
        </el-table-column>
        <!-- 操作列：固定在右侧，提供借阅和取消收藏功能 -->
        <el-table-column fixed="right" label="操作" width="200">
          <template v-slot="scope">
            <!-- 去借阅按钮：跳转到图书列表页面 -->
            <el-button size="mini" type="primary" @click="goToBorrow(scope.row)">
              去借阅
            </el-button>
            <!-- 取消收藏按钮：带二次确认弹窗 -->
            <el-popconfirm title="确认取消收藏?" @confirm="cancelCollection(scope.row.id)">
              <template #reference>
                <el-button type="danger" size="mini">取消收藏</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

    <!-- 分页组件：支持跳转、页码选择和每页数量切换 -->
    <div style="margin: 10px 0">
      <el-pagination v-model:currentPage="currentPage" :page-sizes="[5, 10, 20]" :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper" :total="total" @size-change="handleSizeChange"
        @current-change="handleCurrentChange">
      </el-pagination>
    </div>

    <!-- 图书详情对话框：展示收藏图书的详细信息 -->
    <el-dialog v-model="dialogVisible" title="图书详情" width="500px">
      <!-- 描述列表组件：展示图书详细信息 -->
      <el-descriptions :column="1" border>
        <el-descriptions-item label="图书编号">
          {{ detailBook.isbn }}
        </el-descriptions-item>
        <el-descriptions-item label="图书名称">
          {{ detailBook.bookName }}
        </el-descriptions-item>
        <el-descriptions-item label="作者">
          {{ detailBook.author }}
        </el-descriptions-item>
        <el-descriptions-item label="收藏时间">
          {{ formatDate(detailBook.collectionTime) }}
        </el-descriptions-item>
      </el-descriptions>
      <!-- 对话框底部按钮区域 -->
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="goToBorrow(detailBook)">
            去借阅
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
// 导入所需模块
import request from "../utils/request";        // HTTP 请求工具
import { ElMessage } from "element-plus";      // Element Plus 消息提示组件
import moment from "moment";                   // 日期格式化工具库

export default {
  name: 'MyCollection',
  created() {
    // 从 sessionStorage 获取当前登录用户信息
    let userStr = sessionStorage.getItem("user") || "{}";
    this.user = JSON.parse(userStr);
    this.load();  // 初始化加载数据
  },
  data() {
    return {
      user: {},           // 存储当前登录用户信息
      search1: '',        // 图书名称搜索关键词
      search2: '',        // 作者搜索关键词
      currentPage: 1,     // 当前页码
      pageSize: 10,       // 每页显示数量
      total: 0,           // 总记录数
      tableData: [],      // 收藏列表数据
      selectedIds: [],    // 选中的收藏项ID数组
      dialogVisible: false,  // 详情对话框显示状态
      detailBook: {}      // 当前查看详情的图书信息
    };
  },
  methods: {
    /**
     * 加载收藏列表数据
     * 请求后端接口获取当前用户的收藏记录，支持分页和搜索
     */
    load() {
      request.get("/bookCollection", {
        params: {
          pageNum: this.currentPage,
          pageSize: this.pageSize,
          readerId: this.user.id,
          search1: this.search1,
          search2: this.search2
        }
      }).then(res => {
        if (res.code == '0' || res.code == 0) {
          this.tableData = res.data.records;
          this.total = res.data.total;
        }
      });
    },

    /**
     * 清空搜索条件
     * 重置搜索框并重新加载所有数据
     */
    clear() {
      this.search1 = '';
      this.search2 = '';
      this.load();
    },

    /**
     * 表格复选框选择变化事件
     * @param {Array} val - 选中的行数据数组
     */
    handleSelectionChange(val) {
      this.selectedIds = val.map(v => v.id);
    },

    /**
     * 批量删除收藏
     * 删除选中的所有收藏记录
     */
    deleteBatch() {
      if (!this.selectedIds.length) {
        ElMessage.warning("请选择要删除的收藏");
        return;
      }
      request.post("/bookCollection/deleteBatch", this.selectedIds).then(res => {
        if (res.code == '0' || res.code == 0) {
          ElMessage.success("批量删除成功");
          this.load();  // 重新加载数据
        } else {
          ElMessage.error(res.msg);
        }
      });
    },

    /**
     * 取消单个收藏
     * @param {Number} id - 要取消的收藏记录ID
     */
    cancelCollection(id) {
      request.post("/bookCollection/deleteBatch", [id]).then(res => {
        if (res.code == '0' || res.code == 0) {
          ElMessage.success("取消收藏成功");
          this.load();  // 重新加载数据
        } else {
          ElMessage.error(res.msg || "操作失败");
        }
      });
    },

    /**
     * 显示图书详情
     * @param {Object} row - 当前行数据
     */
    showDetail(row) {
      this.detailBook = JSON.parse(JSON.stringify(row));//先转成 JSON 字符串，再把这个字符串解析回一个新对象
      this.dialogVisible = true;
    },

    /**
     * 跳转到图书列表页面
     * @param {Object} row - 图书行数据，包含ISBN用于定位
     */
    goToBorrow(row) {
      // 跳转到图书列表页面
      this.$router.push({
        path: '/book',
        query: { isbn: row.isbn }
      });
    },

    /**
     * 格式化日期时间
     * @param {Date/String} date - 待格式化的日期
     * @returns {String} 格式化后的日期字符串
     */
    formatDate(date) {
      if (!date) return '-';//如果为空，返回占位符 '-'
      return moment(date).format('YYYY-MM-DD HH:mm:ss');//使用moment库解析日期
    },

    /**
     * 每页数量变化事件
     * @param {Number} pageSize - 新的每页数量
     */
    handleSizeChange(pageSize) {
      this.pageSize = pageSize;
      this.load();  // 重新加载数据
    },

    /**
     * 当前页码变化事件
     * @param {Number} pageNum - 新的页码
     */
    handleCurrentChange(pageNum) {
      this.currentPage = pageNum;
      this.load();  // 重新加载数据
    }
  }
};
</script>

<style scoped>
/* 页面标题样式 */
h2 {
  color: #409EFF;     /* 蓝色字体颜色 */
  font-size: 24px;    /* 字体大小 */
}
</style>