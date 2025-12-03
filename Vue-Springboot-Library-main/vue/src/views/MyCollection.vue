<template>
  <div class="collection" style="padding: 10px">
    <h2 style="margin-bottom: 20px">📚 我的收藏</h2>

    <!-- 搜索栏 -->
    <div style="margin: 10px 0;">
      <el-form inline="true" size="small">
        <el-form-item label="搜索">
          <el-input 
            v-model="search" 
            placeholder="请输入图书名称或作者" 
            clearable
            style="width: 300px">
            <template #prefix>
              <el-icon class="el-input__icon"><search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="load" size="mini">查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button size="mini" type="danger" @click="clear">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 批量操作按钮 -->
    <div style="margin: 10px 0;">
      <el-popconfirm title="确认删除选中的收藏?" @confirm="deleteBatch">
        <template #reference>
          <el-button type="danger" size="mini">批量删除</el-button>
        </template>
      </el-popconfirm>
    </div>

    <!-- 收藏列表 -->
    <el-table 
      :data="tableData" 
      stripe 
      border 
      @selection-change="handleSelectionChange"
      style="width: 100%">
      <el-table-column type="selection" width="55"></el-table-column>
      <el-table-column prop="isbn" label="图书编号" width="150"></el-table-column>
      <el-table-column prop="bookName" label="图书名称" min-width="200">
        <template v-slot="scope">
          <el-button 
            type="text" 
            @click="showDetail(scope.row)" 
            style="color: #409EFF; font-weight: 500;">
            {{ scope.row.bookName }}
          </el-button>
        </template>
      </el-table-column>
      <el-table-column prop="author" label="作者" width="150"></el-table-column>
      <el-table-column prop="collectionTime" label="收藏时间" width="180">
        <template v-slot="scope">
          {{ formatDate(scope.row.collectionTime) }}
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="180">
        <template v-slot="scope">
          <el-button size="mini" type="primary" @click="goToBorrow(scope.row)">
            去借阅
          </el-button>
          <el-popconfirm 
            title="确认取消收藏?" 
            @confirm="cancelCollection(scope.row.id)">
            <template #reference>
              <el-button type="danger" size="mini">取消收藏</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
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

    <!-- 图书详情对话框 -->
    <el-dialog v-model="dialogVisible" title="图书详情" width="500px">
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
import request from "../utils/request";
import { ElMessage } from "element-plus";
import moment from "moment";

export default {
  name: 'MyCollection',
  created() {
    let userStr = sessionStorage.getItem("user") || "{}";
    this.user = JSON.parse(userStr);
    this.load();
  },
  data() {
    return {
      user: {},
      search: '',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      tableData: [],
      selectedIds: [],
      dialogVisible: false,
      detailBook: {}
    };
  },
  methods: {
    load() {
      request.get("/bookCollection", {
        params: {
          pageNum: this.currentPage,
          pageSize: this.pageSize,
          readerId: this.user.id,
          search: this.search
        }
      }).then(res => {
        if (res.code == '0' || res.code == 0) {
          this.tableData = res.data.records;
          this.total = res.data.total;
        }
      });
    },

    clear() {
      this.search = '';
      this.load();
    },

    handleSelectionChange(val) {
      this.selectedIds = val.map(v => v.id);
    },

    deleteBatch() {
      if (!this.selectedIds.length) {
        ElMessage.warning("请选择要删除的收藏");
        return;
      }
      request.post("/bookCollection/deleteBatch", this.selectedIds).then(res => {
        if (res.code == '0' || res.code == 0) {
          ElMessage.success("批量删除成功");
          this.load();
        } else {
          ElMessage.error(res.msg);
        }
      });
    },

    cancelCollection(id) {
      request.post("/bookCollection/deleteBatch", [id]).then(res => {
        if (res.code == '0' || res.code == 0) {
          ElMessage.success("取消收藏成功");
          this.load();
        } else {
          ElMessage.error(res.msg || "操作失败");
        }
      });
    },

    showDetail(row) {
      this.detailBook = JSON.parse(JSON.stringify(row));
      this.dialogVisible = true;
    },

    goToBorrow(row) {
      // 跳转到图书列表页面，并定位到该书
      this.$router.push({
        path: '/book',
        query: { isbn: row.isbn }
      });
    },

    formatDate(date) {
      if (!date) return '-';
      return moment(date).format('YYYY-MM-DD HH:mm:ss');
    },

    handleSizeChange(pageSize) {
      this.pageSize = pageSize;
      this.load();
    },

    handleCurrentChange(pageNum) {
      this.currentPage = pageNum;
      this.load();
    }
  }
};
</script>

<style scoped>
h2 {
  color: #409EFF;
  font-size: 24px;
}
</style>