<template>
<!-- 图书管理页面容器：包含搜索、表格展示、分页和对话框 -->
<div style="padding: 10px">

    <!-- 图书搜索区域：支持多条件查询 -->
    <div style="margin: 10px 0;">
      <el-form inline="true" size="small">
        <!-- 图书编号搜索框：包含图标 -->
        <el-form-item label="图书编号">
          <el-input v-model="search1" placeholder="请输入图书编号" clearable>
            <template #prefix>
              <el-icon class="el-input__icon">
                <search />  <!-- 搜索图标 -->
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <!-- 图书名称搜索框：包含图标 -->
        <el-form-item label="图书名称">
          <el-input v-model="search2" placeholder="请输入图书名称" clearable>
            <template #prefix><el-icon class="el-input__icon">
                <search />  <!-- 搜索图标 -->
              </el-icon></template>
          </el-input>
        </el-form-item>
        <!-- 作者搜索框：包含图标 -->
        <el-form-item label="作者">
          <el-input v-model="search3" placeholder="请输入作者" clearable>
            <template #prefix><el-icon class="el-input__icon">
                <search />  <!-- 搜索图标 -->
              </el-icon></template>
          </el-input>
        </el-form-item>
        <!-- 查询按钮：触发搜索方法 -->
        <el-form-item>
          <el-button type="primary" style="margin-left: 1%" @click="load" size="mini">
            <svg-icon iconClass="search" />查询
          </el-button>
        </el-form-item>
        <!-- 重置按钮：清空搜索条件 -->
        <el-form-item>
          <el-button size="mini" type="danger" @click="clear">重置</el-button>
        </el-form-item>
        <!-- 逾期通知按钮：仅在有逾期图书时显示 -->
        <el-form-item style="float: right" v-if="numOfOutDataBook != 0">
          <el-popconfirm confirm-button-text="查看" cancel-button-text="取消" :icon="InfoFilled" icon-color="red"
            title="您有图书已逾期，请尽快归还" @confirm="toLook"><!--	@confirm点击确认按钮后执行的方法-->
            <template #reference>
              <el-button type="warning">逾期通知</el-button>
            </template>
          </el-popconfirm>
        </el-form-item>
      </el-form>
    </div>
    <!-- 操作按钮区域：上架、批量删除等 -->
    <div style="margin: 10px 0;">
      <!-- 上架按钮：仅管理员可见 -->
      <el-button type="primary" @click="add" v-if="user.role == 1">上架</el-button>
      <!-- 批量删除按钮：仅管理员可见 -->
      <el-popconfirm title="确认删除?" @confirm="deleteBatch" v-if="user.role == 1">
        <template #reference>
          <el-button type="danger">批量删除</el-button>
        </template>
      </el-popconfirm>
    </div>
    <!-- 图书数据表格：展示图书列表 -->
    <el-table :data="tableData" stripe border="true" @selection-change="handleSelectionChange" style="width: 100%;">
        <!-- 复选框列：仅管理员可见，用于批量操作 -->
        <el-table-column v-if="user.role == 1" type="selection" width="55">
        </el-table-column>
        <!-- 封面列：显示图书封面图片，支持预览 -->
        <el-table-column label="封面" width="80">
          <template v-slot="scope">
            <el-image
              v-if="scope.row.bookPicture"
              :src="getImageUrl(scope.row.bookPicture)"
              style="width: 60px; height: 80px; border-radius: 4px;"
              fit="cover"
              :preview-src-list="[getImageUrl(scope.row.bookPicture)]"
              preview-teleported>
            </el-image>
            <span v-else style="color: #909399; font-size: 12px;">无封面</span>
          </template>
        </el-table-column>
        <!-- 图书编号列：支持排序 -->
        <el-table-column prop="isbn" label="图书编号" sortable />
        <!-- 图书名称列：可点击查看详情 -->
        <el-table-column prop="name" label="图书名称">
          <template v-slot="scope">
            <el-button type="text" @click="showDetail(scope.row)" style="color: #409EFF; font-weight: 500;">
              {{ scope.row.name }}
            </el-button>
          </template>
        </el-table-column>
        <!-- 作者列 -->
        <el-table-column prop="author" label="作者" />
        <!-- 出版社列 -->
        <el-table-column prop="publisher" label="出版社" />
        <!-- 出版日期列：只显示年份 -->
        <el-table-column label="出版日期" width="100">
          <template v-slot="scope">
            {{ getYear(scope.row.createTime) }}
          </template>
        </el-table-column>
        <!-- 剩余可借列：动态计算并显示库存状态 -->
        <el-table-column label="剩余可借" width="100">
          <template v-slot="scope">
            <el-tag :type="getAvailableQuantity(scope.row) > 0 ? 'success' : 'info'">
              {{ getAvailableQuantity(scope.row) }} 本
            </el-tag>
          </template>
        </el-table-column>
        <!-- 状态列：显示图书是否可借阅 -->
        <el-table-column prop="status" label="状态" width="100">
          <template v-slot="scope">
            <el-tag v-if="getAvailableQuantity(scope.row) <= 0" type="warning">不可借阅</el-tag>
            <el-tag v-else type="success">可借阅</el-tag>
          </template>
        </el-table-column>
        <!-- 操作列：包含修改、删除、借阅、还书、收藏按钮 -->
        <el-table-column fixed="right" label="操作" width="200">
          <template v-slot="scope">
            <!-- 修改按钮：仅管理员可见 -->
            <el-button size="mini" @click="handleEdit(scope.row)" v-if="user.role == 1">修改</el-button>
            <!-- 删除按钮：仅管理员可见，需确认 -->
            <el-popconfirm title="确认删除?" @confirm="handleDelete(scope.row.id)" v-if="user.role == 1">
              <template #reference>
                <el-button type="danger" size="mini">删除</el-button>
              </template>
            </el-popconfirm>
            <!-- 借阅按钮：仅普通用户可见，无库存时禁用 -->
            <el-button size="mini" @click="handlelend(scope.row)" v-if="user.role == 2"
              :disabled="getAvailableQuantity(scope.row) <= 0">借阅</el-button>
            <!-- 还书按钮：仅普通用户可见，需确认，已借阅时启用 -->
            <el-popconfirm title="确认还书?" @confirm="handleReturn(scope.row)" v-if="user.role == 2">
              <template #reference>
                <el-button type="danger" size="mini" :disabled="(this.isbnArray.indexOf(scope.row.isbn)) == -1">还书</el-button>
              </template>
            </el-popconfirm>
            <!-- 收藏图标：仅普通用户可见，点击可切换收藏状态 -->
            <el-tooltip :content="scope.row.isCollected ? '取消收藏' : '收藏'" placement="top">
              <div @click="toggleCollection(scope.row)"
                style="display: inline-block; margin: 0 15px; vertical-align: middle; line-height: 1; position: relative; top: -1px;">
                <el-icon :style="{
                  fontSize: '25px',
                  width: '25px',
                  height: '25px',
                  color: scope.row.isCollected ? '#ffc107' : '#dcdfe6',  /* 已收藏为黄色，未收藏为灰色 */
                  transition: 'all 0.3s ease',
                  cursor: 'pointer',
                  borderRadius: '4px',
                  verticalAlign: 'middle',
                  display: 'inline-flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  boxShadow: 'none !important',
                  position: 'relative',
                  top: 0
                }" :class="{ 'icon-hover': !scope.row.isCollected, 'icon-fixed': true }" v-if="user.role == 2">
                  <StarFilled v-if="scope.row.isCollected" />  <!-- 实心星星：已收藏 -->
                  <Star v-else />  <!-- 空心星星：未收藏 -->
                </el-icon>
              </div>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

    <!-- 图书详情对话框：展示图书完整信息 -->
    <el-dialog v-model="dialogVisibleDetail" title="图书详情" width="800px">
      <div class="book-detail-container">
        <!-- 左侧图片区域：显示图书封面 -->
        <div class="book-image-section"><!--去除字符串首尾的空格-->
          <el-image
            v-if="detailBook.bookPicture && detailBook.bookPicture.trim()"
            :src="getImageUrl(detailBook.bookPicture)"
            class="book-cover-image"
            fit="contain"
            :preview-src-list="[getImageUrl(detailBook.bookPicture)]"
            preview-teleported>
          </el-image>
          <div v-else class="book-cover-placeholder">
            <el-icon style="font-size: 48px; color: #c0c4cc;"><Picture /></el-icon>  <!-- 图片占位图标 -->
            <span>暂无封面图片</span>
          </div>
        </div>
        <!-- 右侧信息区域：显示图书详细属性 -->
        <div class="book-info-section">
          <el-descriptions :column="1" border>
        <el-descriptions-item label="图书编号" label-class-name="detail-label">
          {{ detailBook.isbn }}
        </el-descriptions-item>
        <el-descriptions-item label="图书名称" label-class-name="detail-label">
          {{ detailBook.name }}
        </el-descriptions-item>
        <el-descriptions-item label="价格" label-class-name="detail-label">
          ¥{{ detailBook.price }}
        </el-descriptions-item>
        <el-descriptions-item label="作者" label-class-name="detail-label">
          {{ detailBook.author }}
        </el-descriptions-item>
        <el-descriptions-item label="出版社" label-class-name="detail-label">
          {{ detailBook.publisher }}
        </el-descriptions-item>
        <el-descriptions-item label="出版时间" label-class-name="detail-label">
          {{ detailBook.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="馆藏总数" label-class-name="detail-label">
          {{ detailBook.totalQuantity || 0 }} 本
        </el-descriptions-item>
        <el-descriptions-item label="已借出" label-class-name="detail-label">
          {{ detailBook.borrowedQuantity || 0 }} 本
        </el-descriptions-item>
        <el-descriptions-item label="可借阅" label-class-name="detail-label">
          <el-tag :type="getAvailableQuantity(detailBook) > 0 ? 'success' : 'warning'">
            {{ getAvailableQuantity(detailBook) }} 本
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="累计借阅次数" label-class-name="detail-label">
          {{ detailBook.borrowNum || 0 }} 次
        </el-descriptions-item>
        <el-descriptions-item label="当前状态" label-class-name="detail-label">
          <el-tag v-if="getAvailableQuantity(detailBook) <= 0" type="warning">不可借阅</el-tag>
          <el-tag v-else type="success">可借阅</el-tag>
        </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="dialogVisibleDetail = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 逾期详情对话框：展示用户逾期的借阅记录 -->
    <el-dialog v-model="dialogVisible3" v-if="numOfOutDataBook != 0" title="逾期详情" width="50%"
      :before-close="handleClose">
      <el-table :data="outDateBook" style="width: 100%">
        <el-table-column prop="isbn" label="图书编号" />
        <el-table-column prop="bookName" label="书名" />
        <el-table-column prop="lendTime" label="借阅日期" />
        <el-table-column prop="deadTime" label="截至日期" />
      </el-table>

      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="dialogVisible3 = false">确认</el-button>
        </span>
      </template>
    </el-dialog>
    <!-- 分页组件：支持切换每页数量和页码 -->
    <div style="margin: 10px 0">
      <el-pagination v-model:currentPage="currentPage" :page-sizes="[5, 10, 20]" :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper" :total="total" @size-change="handleSizeChange"
        @current-change="handleCurrentChange">
      </el-pagination>

      <!-- 上架书籍对话框：新增图书信息 -->
      <el-dialog v-model="dialogVisible" title="上架书籍" width="800px">
        <div class="book-form-container">
          <!-- 左侧图片上传区域：上传图书封面 -->
          <div class="book-image-section">
            <div class="upload-section-title">图书封面</div>
            <el-upload
              class="avatar-uploader-large"
              :http-request="handleUpload"
              :show-file-list="false"
              :before-upload="beforeUpload"
              accept="image/*">
              <img v-if="form.bookPicture" :src="getImageUrl(form.bookPicture)" class="avatar-large" />
              <div v-else class="upload-placeholder">
                <el-icon class="upload-icon"><Plus /></el-icon>  <!-- 加号图标 -->
                <div class="upload-text">点击上传封面</div>
              </div>
            </el-upload>
            <div class="upload-tip">
              支持jpg、png、gif格式<br/>大小不超过5MB
            </div>
          </div>
          <!-- 右侧表单区域：输入图书详细信息 -->
          <div class="book-form-section">
            <el-form :model="form" label-width="100px">
              <el-form-item label="图书编号">
                <el-input v-model="form.isbn" placeholder="请输入图书编号"></el-input>
              </el-form-item>
              <el-form-item label="图书名称">
                <el-input v-model="form.name" placeholder="请输入图书名称"></el-input>
              </el-form-item>
              <el-form-item label="价格">
                <el-input v-model="form.price" placeholder="请输入价格"></el-input>
              </el-form-item>
              <el-form-item label="作者">
                <el-input v-model="form.author" placeholder="请输入作者"></el-input>
              </el-form-item>
              <el-form-item label="出版社">
                <el-input v-model="form.publisher" placeholder="请输入出版社"></el-input>
              </el-form-item>
              <el-form-item label="出版时间">
                <el-date-picker
                  value-format="YYYY-MM-DD"
                  type="date"
                  style="width: 100%"
                  clearable
                  v-model="form.createTime"
                  placeholder="请选择出版时间">
                </el-date-picker>
              </el-form-item>
              <el-form-item label="馆藏数量">
                <el-input-number style="width: 100%" v-model="form.totalQuantity" :min="1" :max="9999" placeholder="请输入馆藏数量"></el-input-number>
              </el-form-item>
            </el-form>
          </div>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible = false">取 消</el-button>
            <el-button type="primary" @click="save">确 定</el-button>
          </span>
        </template>
      </el-dialog>

      <!-- 修改书籍信息对话框：编辑已有图书 -->
      <el-dialog v-model="dialogVisible2" title="修改书籍信息" width="800px">
        <div class="book-form-container">
          <!-- 左侧图片上传区域：更新图书封面 -->
          <div class="book-image-section">
            <div class="upload-section-title">图书封面</div>
            <el-upload
              class="avatar-uploader-large"
              :http-request="handleUpload"
              :show-file-list="false"
              :before-upload="beforeUpload"
              accept="image/*">
              <!-- 有图片时显示图片 -->
              <img v-if="form.bookPicture" :src="getImageUrl(form.bookPicture)" class="avatar-large" />
              <!-- 没有图片时显示上传按钮 -->
              <div v-else class="upload-placeholder">
                <el-icon class="upload-icon"><Plus /></el-icon>  <!-- 加号图标 -->
                <div class="upload-text">点击上传封面</div>
              </div>
            </el-upload>
            <div class="upload-tip">
              支持jpg、png、gif格式<br/>大小不超过5MB
            </div>
          </div>
          <!-- 右侧表单区域：编辑图书详细信息 -->
          <div class="book-form-section">
            <el-form :model="form" label-width="100px">
              <el-form-item label="图书编号">
                <el-input v-model="form.isbn" placeholder="请输入图书编号"></el-input>
              </el-form-item>
              <el-form-item label="图书名称">
                <el-input v-model="form.name" placeholder="请输入图书名称"></el-input>
              </el-form-item>
              <el-form-item label="价格">
                <el-input v-model="form.price" placeholder="请输入价格"></el-input>
              </el-form-item>
              <el-form-item label="作者">
                <el-input v-model="form.author" placeholder="请输入作者"></el-input>
              </el-form-item>
              <el-form-item label="出版社">
                <el-input v-model="form.publisher" placeholder="请输入出版社"></el-input>
              </el-form-item>
              <el-form-item label="出版时间">
              <!--Element Plus 的日期选择器-->
                <el-date-picker
                  value-format="YYYY-MM-DD"
                  type="date"
                  style="width: 100%"
                  clearable
                  v-model="form.createTime"
                  placeholder="请选择出版时间">
                </el-date-picker>
              </el-form-item>
              <el-form-item label="馆藏总数">
                <el-input-number style="width: 100%" v-model="form.totalQuantity" :min="1" :max="9999" placeholder="请输入馆藏总数"></el-input-number>
              </el-form-item>
            </el-form>
          </div>
        </div>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible2 = false">取 消</el-button>
            <el-button type="primary" @click="save">确 定</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script>
// 导入所需模块
// @ is an alias to /src
import request from "../utils/request";        // HTTP 请求工具
import { ElMessage } from "element-plus";     // Element Plus 消息提示组件
import moment from "moment";                 // 日期处理库
import { Plus, Picture } from "@element-plus/icons-vue";  // Element Plus 图标组件

export default {
  name: 'Book',
  components: {  // 注册自定义组件
    Plus,       // 加号图标（用于上传区域）
    Picture     // 图片图标（用于占位显示）
  },
  created() {
    // 组件创建时执行的初始化操作
    let userStr = sessionStorage.getItem("user") || "{}"  // 从 sessionStorage 获取用户信息
    this.user = JSON.parse(userStr)  // 解析用户信息
    // 初始化上传URL
    this.uploadUrl = 'http://localhost:9090/file/upload';
    this.load()  // 加载图书列表数据
  },
  methods: {
    /**
     * 获取年份
     * @param {String} dateStr - 日期字符串
     * @returns {String} 格式化后的年份（如：2023年）
     */
    getYear(dateStr) {
      if (!dateStr) return '-';
      const year = new Date(dateStr).getFullYear();//获取年份（4位数字）
      return isNaN(year) ? '-' : year + '年';//Not a Numbe  是无效数字返回 -，否则加 "年"
    },

    /**
     * 计算可借阅数量
     * @param {Object} book - 图书对象
     * @returns {Number} 可借阅数量 = 馆藏总数 - 已借出数量
     */
    getAvailableQuantity(book) {
      if (!book.totalQuantity || !book.borrowedQuantity) {
        return book.totalQuantity || 0;
      }
      return book.totalQuantity - book.borrowedQuantity;
    },

    /**
     * 显示图书详情对话框
     * @param {Object} row - 当前行图书数据
     */
    showDetail(row) {
      this.detailBook = JSON.parse(JSON.stringify(row))  // 深拷贝图书数据
      this.dialogVisibleDetail = true  // 显示详情对话框
    },

    /**
     * 表格复选框选择变化事件
     * @param {Array} val - 选中的行数据数组
     */
    handleSelectionChange(val) {
      this.ids = val.map(v => v.id)  // 提取选中行的 ID 数组
    },

    /**
     * 批量删除图书
     */
    deleteBatch() {
      if (!this.ids.length) {
        ElMessage.warning("请选择数据!")
        return
      }
      request.post("/book/deleteBatch", this.ids).then(res => {
        if (res.code === '0') {
          ElMessage.success("批量删除成功")
          this.load()  // 刷新列表
        }
        else {
          ElMessage.error(res.msg)
        }
      })
    },

    /**
     * 加载图书列表数据
     * 1. 获取图书分页列表
     * 2. 如果是普通用户，获取借阅记录和收藏状态
     * 3. 检查逾期图书
     */
    load() {
      this.numOfOutDataBook = 0;
      this.outDateBook = [];

      // 第一步：获取图书分页列表
      request.get("/book", {
        params: {
          pageNum: this.currentPage,
          pageSize: this.pageSize,
          search1: this.search1,
          search2: this.search2,
          search3: this.search3,
        }
      }).then(bookResponse => {
        // 图书列表请求成功后，执行这里的代码
        this.tableData = bookResponse.data.records;
        this.total = bookResponse.data.total;

        // 第二步：如果不是管理员，处理借阅和收藏状态
        if (this.user.role == 2) {
          // 2.1 处理用户借阅和逾期信息 (这部分逻辑不变)
          request.get("/bookwithuser", {
            params: {
              pageNum: "1",
              pageSize: this.total,
              search1: "",
              search2: "",
              search3: this.user.id,
            }
          }).then(res => {
            this.bookData = res.data.records;  // 存储借阅记录
            this.number = this.bookData.length;  // 当前借阅数量
            const nowDate = new Date();
            this.isbnArray = [];
            // 遍历借阅记录，检查逾期情况
            for (let i = 0; i < this.number; i++) {
              this.isbnArray[i] = this.bookData[i].isbn;
              let dDate = new Date(this.bookData[i].deadTime);
              if (dDate < nowDate) {
                // 发现逾期图书，添加到逾期列表
                this.outDateBook.push({
                  isbn: this.bookData[i].isbn,
                  bookName: this.bookData[i].bookName,
                  deadTime: this.bookData[i].deadTime,
                  lendTime: this.bookData[i].lendTime,
                });
                this.numOfOutDataBook++;  // 逾期图书数量加 1
              }
            }
          });

          // 2.2 获取用户的收藏列表，并更新 tableData
          request.get("/bookCollection", {
            params: {
              readerId: this.user.id,
              pageNum: 1,
              pageSize: 1000 // 获取全部收藏
            }
          }).then(collectionRes => {
            // 收藏列表请求成功后，执行这里的代码
            if (collectionRes.code === '0') {
              const collectedBookIds = new Set(collectionRes.data.records.map(c => c.bookId));
              // 遍历当前页的图书，设置 isCollected 状态
              // 这里的 this.tableData 是第一步请求成功后赋的值
              this.tableData.forEach(book => {
                book.isCollected = collectedBookIds.has(book.id);
              });
            }
          }).catch(error => {
            console.error("获取收藏列表失败", error);
          });
        }
      }).catch(error => {
        // 图书列表请求失败后，执行这里的代码
        console.error("加载图书列表失败", error);
        ElMessage.error("加载图书列表失败");
      });
    },

    /**
     * 清空搜索条件并重新加载数据
     */
    clear() {
      this.search1 = ""
      this.search2 = ""
      this.search3 = ""
      this.load()
    },

    /**
     * 删除单本图书
     * @param {Number} id - 图书 ID
     */
    handleDelete(id) {
      request.delete("book/" + id).then(res => {
        console.log(res)
        if (res.code == 0) {
          ElMessage.success("删除成功")
        }
        else
          ElMessage.error(res.msg)
        this.load()
      })
    },

    /**
     * 还书处理方法
     * 1. 更新图书信息：已借阅数量 -1
     * 2. 更新借阅历史为已归还
     * 3. 删除当前借阅记录
     * @param {Object} row - 当前行图书数据
     */
    handleReturn(row) {
      // 从借阅记录中找到对应的记录
      const borrowRecord = this.bookData.find(record => record.isbn === row.isbn);
      if (!borrowRecord) {
        ElMessage.error("未找到对应的借阅记录");
        return;
      }

      // 1) 更新图书信息：已借阅数量 -1
      const borrowedQuantity = Math.max((row.borrowedQuantity || 0) - 1, 0);
      const bookUpdate = {
        id: row.id,
        borrowedQuantity,
        status: '1'
      };

      request.put("/book", bookUpdate).then(updateRes => {
        if (updateRes.code !== 0 && updateRes.code !== '0') {
          ElMessage.error(updateRes.msg || '更新图书状态失败');
          return;
        }

        // 2) 更新借阅历史为已归还
        const lendPayload = {
          isbn: row.isbn,
          readerId: this.user.id,
          lendTime: borrowRecord.lendTime
        };

        request.put("/LendRecord1", lendPayload).then(() => {
          // 3) 删除当前借阅记录（bookwithuser）
          const payload = {
            bookId: borrowRecord.bookId,
            readerId: this.user.id,
            lendTime: borrowRecord.lendTime
          };

          request.post("bookwithuser/deleteRecord", payload).then(res => {
            if (res.code == 0 || res.code === '0') {
              ElMessage.success("归还成功");
            } else {
              ElMessage.error(res.msg || '归还失败');
            }
            this.load();
          }).catch(error => {
            console.error("删除借阅记录失败", error);
            ElMessage.error("删除借阅记录失败");
            this.load();
          });
        }).catch(error => {
          console.error("更新借阅历史失败", error);
          ElMessage.error("更新借阅历史失败");
        });
      }).catch(error => {
        console.error("更新图书信息失败", error);
        ElMessage.error("更新图书信息失败");
      });
    },

    /**
     * 借阅图书处理方法
     * 1. 验证借阅条件（最大借阅数、逾期状态、库存）
     * 2. 插入借阅记录
     * 3. 更新图书信息
     * 4. 添加到历史借阅记录
     * @param {Object} row - 当前行图书数据
     */
    handlelend(row) {
      // 检查借阅数量限制（最多5本）
      if (this.number == 5) {
        ElMessage.warning("您不能再借阅更多的书籍了")
        return;
      }
      // 检查是否有逾期图书
      if (this.numOfOutDataBook != 0) {
        ElMessage.warning("在您归还逾期书籍前不能再借阅书籍")
        return;
      }

      // 检查是否有可借阅的书
      if (this.getAvailableQuantity(row) <= 0) {
        ElMessage.warning("该图书暂无可借阅库存")
        return;
      }

      // 先准备借阅记录数据
      let startDate = moment(new Date()).format("yyyy-MM-DD HH:mm:ss");
      let form3 = {};
      form3.bookId = row.id;  // 添加图书ID
      form3.isbn = row.isbn;
      form3.bookName = row.name;
      form3.nickName = this.user.username;
      form3.readerId = this.user.id;
      form3.lendTime = startDate;
      // 计算应还日期（借阅日期 + 30天）
      let nowDate = new Date(startDate);
      nowDate.setDate(nowDate.getDate() + 30);
      form3.deadTime = moment(nowDate).format("yyyy-MM-DD HH:mm:ss");
      form3.prolong = 1;

      // 先检查是否已借阅该书
      console.log('准备插入借阅记录:', form3)
      request.post("/bookwithuser/insertNew", form3).then(res => {
        console.log('插入借阅记录返回:', res)
        if (res.code !== '0' && res.code !== 0) {
          // 借阅失败，显示错误信息
          ElMessage.error(res.msg || '借阅失败')
          return;
        }

        // 借阅成功，继续更新图书信息
        this.form.id = row.id
        this.form.borrowNum = (row.borrowNum || 0) + 1  // 累计借阅次数 +1
        this.form.borrowedQuantity = (row.borrowedQuantity || 0) + 1  // 已借出数量 +1

        request.put("/book", this.form).then(bookRes => {
          if (bookRes.code == 0) {
            ElMessage.success('借阅成功')

            // 添加到历史借阅记录
            this.form2.status = "0"  // 状态：0表示借阅中
            this.form2.bookId = row.id  // 添加图书ID - 这是必需的字段
            this.form2.isbn = row.isbn
            this.form2.bookname = row.name
            this.form2.readerId = this.user.id
            this.form2.borrowNum = (row.borrowNum || 0) + 1
            this.form2.lendTime = startDate

            request.post("/LendRecord", this.form2).then(() => {
              this.load()
            })
          } else {
            ElMessage.error(bookRes.msg)
            // 回滚：删除刚才插入的借阅记录
            request.post("/bookwithuser/deleteRecord", form3)
          }
        }).catch(() => {
          // 出错时回滚
          request.post("/bookwithuser/deleteRecord", form3)
          this.load()
        })
      })
    },

    /**
     * 打开上架书籍对话框
     */
    add() {
      this.dialogVisible = true
      this.form = {}  // 清空表单数据
    },

    /**
     * 保存图书信息（新增或修改）
     * - 如果有 id 则为修改操作
     * - 如果没有 id 则为新增操作
     */
    save() {
      if (this.form.id) {
        // 修改操作
        // 如果修改了馆藏总数，需要验证   检查 totalQuantity（馆藏总数）字段是否有值
        if (this.form.totalQuantity !== undefined && this.form.totalQuantity !== null) {
          // 查询bookwithuser表中该书的记录数（已借阅数量）
          request.get("/bookwithuser", {
            params: {
              pageNum: 1,
              pageSize: 1000,
              search1: this.form.isbn,
              search2: "",
              search3: ""
            }
          }).then(borrowRes => {
            const borrowedCount = (borrowRes.data && borrowRes.data.total) ? borrowRes.data.total : 0;

            if (this.form.totalQuantity < borrowedCount) {
              ElMessage.warning(`馆藏总数不能小于已借阅数量（${borrowedCount}本），请重新设置`);
              return;// 报错，停止保存
            }

            // 验证通过，执行更新
            this.updateBook();
          }).catch(error => {
            console.error("查询借阅记录失败", error);
            // 查询失败也允许更新，但给出警告
            ElMessage.warning("无法验证借阅数量，请确认馆藏总数设置正确");
            this.updateBook();
          });
        } else {
          // 没有修改馆藏总数，直接更新
          this.updateBook();
        }
      }
      else {
        // 新增图书时初始化字段
        this.form.borrowNum = 0          // 累计借阅次数初始化为 0
        this.form.borrowedQuantity = 0   // 已借出数量初始化为 0
        this.form.totalQuantity = this.form.totalQuantity || 1  // 馆藏总数，默认为 1
        this.form.status = 1             // 状态：1表示正常

        request.post("/book", this.form).then(res => {
          console.log(res)
          if (res.code == 0) {
            ElMessage.success('上架书籍成功')
          }
          else {
            ElMessage.error(res.msg)
          }
          this.load()
          this.dialogVisible = false
        })
      }
    },

    /**
     * 更新图书信息
     */
    updateBook() {
      request.put("/book", this.form).then(res => {
        console.log(res)
        if (res.code == 0) {
          ElMessage({
            message: '修改书籍信息成功',
            type: 'success',
          })
        }
        else {
          ElMessage.error(res.msg)
        }
        this.load()
        this.dialogVisible2 = false
      })
    },

    /**
     * 打开修改书籍信息对话框
     * @param {Object} row - 当前行图书数据
     */
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row))  // 深拷贝图书数据
      this.dialogVisible2 = true
    },

    /**
     * 分页大小变化事件
     * @param {Number} pageSize - 新的每页数量
     */
    handleSizeChange(pageSize) {
      this.pageSize = pageSize
      this.load()
    },

    /**
     * 当前页码变化事件
     * @param {Number} pageNum - 新的页码
     */
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
      this.load()
    },

    /**
     * 打开逾期详情对话框
     */
    toLook() {
      this.dialogVisible3 = true;
    },

    /**
     * 切换收藏状态（收藏/取消收藏）
     * @param {Object} row - 当前行图书数据
     */
    toggleCollection(row) {
      const params = {
        readerId: this.user.id,
        bookId: row.id,
      };

      if (row.isCollected) {
        // 当前是"已收藏"，调用"取消收藏"接口
        request.delete("/bookCollection/cancel", { params: params })
          .then(res => {
            // 请求成功后的回调
            if (res.code === '0') {
              ElMessage.success("已取消收藏");
              row.isCollected = false; // 更新UI
            } else {
              ElMessage.error(res.msg || "操作失败");
            }
          })
          .catch(error => {
            // 请求失败后的回调
            console.error("取消收藏失败", error);
            ElMessage.error("请求失败");
          });
      } else {
        // 当前是"未收藏"，调用"添加收藏"接口
        const data = {
          readerId: this.user.id,
          bookId: row.id
        };
        request.post("/bookCollection/add", data)
          .then(res => {
            // 请求成功后的回调
            if (res.code === '0') {
              ElMessage.success("收藏成功");
              row.isCollected = true; // 更新UI
            } else {
              ElMessage.error(res.msg || "操作失败");
            }
          })
          .catch(error => {
            // 请求失败后的回调
            console.error("收藏失败", error);
            ElMessage.error("请求失败");
          });
      }
    },

    // ==================== 图片上传相关方法 ====================

    /**
     * 获取图片完整 URL
     * @param {String} imagePath - 图片相对路径
     * @returns {String} 完整的图片 URL
     */
    getImageUrl(imagePath) {
      if (!imagePath) {
        return '';
      }
      // 如果已经是完整URL，直接返回
      if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
        return imagePath;
      }
      // 否则拼接基础URL（后端服务地址）
      return 'http://localhost:9090' + imagePath;
    },

    /**
     * 处理图片上传
     * @param {Object} options - 上传配置对象，包含 file 文件信息
     */
    handleUpload(options) {
      //创建表单数据对象
      const formData = new FormData();
      formData.append('file', options.file);
      //发送上传请求
      request.post('/file/upload', formData).then(response => {
        if (response.code === '0' || response.code === 0) {
          this.form.bookPicture = response.data;  // 保存图片路径
          ElMessage.success('图片上传成功');
        } else {
          ElMessage.error(response.msg || '图片上传失败');
        }
      }).catch(error => {
        console.error('上传失败', error);
        ElMessage.error('图片上传失败');
      });
    },

    /**
     * 上传前验证文件
     * @param {File} file - 待上传的文件对象
     * @returns {Boolean} 是否通过验证
     */
    beforeUpload(file) {
      const isImage = file.type.startsWith('image/');  //file.type：获取文件的 MIME 类型 例如 image/jpeg 检查这个字符串是不是以 image/ 开头
      const isLt5M = file.size / 1024 / 1024 < 5;      // 验证大小是否小于5MB

      if (!isImage) {
        ElMessage.error('只能上传图片文件!');
        return false;
      }
      if (!isLt5M) {
        ElMessage.error('图片大小不能超过 5MB!');
        return false;
      }
      return true;
    },
  },
  data() {
    return {
      form: {},                    // 图书表单数据（用于新增和修改）
      form2: {},                   // 历史借阅记录表单数据
      form3: {},                   // 借阅记录表单数据
      dialogVisible: false,        // 上架书籍对话框显示状态
      dialogVisible2: false,       // 修改书籍信息对话框显示状态
      dialogVisibleDetail: false,  // 图书详情对话框显示状态
      detailBook: {},              // 当前查看详情的图书数据
      search1: '',                 // 图书编号搜索条件
      search2: '',                 // 图书名称搜索条件
      search3: '',                 // 作者搜索条件
      total: 10,                   // 数据总条数
      currentPage: 1,              // 当前页码
      pageSize: 10,                // 每页数量
      tableData: [],               // 图书列表数据
      user: {},                    // 当前登录用户信息
      number: 0,                   // 当前用户借阅图书数量
      bookData: [],                // 用户借阅记录数据
      isbnArray: [],               // 用户已借阅图书的 ISBN 数组
      outDateBook: [],             // 逾期图书列表
      numOfOutDataBook: 0,         // 逾期图书数量
      dialogVisible3: true,        // 逾期详情对话框显示状态
      uploadUrl: '',               // 图片上传服务地址
    }
  },
}
</script>

<style scoped>
/* 详情页标签样式：加粗和背景色 */
.detail-label {
  font-weight: bold;
  background-color: #f5f7fa;
}

/* ==================== 图书详情页布局样式 ==================== */
/* 图书详情容器：左右分栏布局 */
.book-detail-container {
  display: flex;              /* 弹性布局 */
  gap: 30px;                  /* 左右间距 */
  padding: 10px 0;            /* 上下内边距 */
}

/* 图书封面区域：固定宽度 */
.book-image-section {
  flex: 0 0 280px;            /* 固定宽度280px，不伸缩 */
  display: flex;              /* 弹性布局 */
  flex-direction: column;     /* 垂直排列 */
  align-items: center;        /* 水平居中 */
}

/* 图书封面图片样式 */
.book-cover-image {
  width: 100%;                /* 宽度100% */
  max-width: 280px;           /* 最大宽度280px */
  height: 400px;              /* 固定高度400px */
  border-radius: 8px;         /* 圆角 */
  border: 1px solid #e4e7ed;  /* 边框 */
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);  /* 阴影效果 */
  object-fit: contain;        /* 图片等比缩放，完整显示 */
  background-color: #fafafa;  /* 背景色 */
}

/* 图书封面占位样式（无封面时） */
.book-cover-placeholder {
  width: 100%;                /* 宽度100% */
  max-width: 280px;           /* 最大宽度280px */
  height: 400px;              /* 固定高度400px */
  border: 2px dashed #d9d9d9; /* 虚线边框 */
  border-radius: 8px;         /* 圆角 */
  display: flex;              /* 弹性布局 */
  flex-direction: column;     /* 垂直排列 */
  align-items: center;        /* 水平居中 */
  justify-content: center;    /* 垂直居中 */
  background-color: #fafafa;  /* 背景色 */
  color: #909399;             /* 文字颜色 */
  gap: 10px;                  /* 元素间距 */
}

/* 图书信息区域：占据剩余空间 */
.book-info-section {
  flex: 1;                    /* 占据剩余空间 */
  min-width: 0;               /* 允许内容收缩 */
}

/* ==================== 表单页面布局样式 ==================== */
/* 图书表单容器：左右分栏布局 */
.book-form-container {
  display: flex;              /* 弹性布局 */
  gap: 30px;                  /* 左右间距 */
  padding: 10px 0;            /* 上下内边距 */
}

/* 图书表单区域：占据剩余空间 */
.book-form-section {
  flex: 1;                    /* 占据剩余空间 */
  min-width: 0;               /* 允许内容收缩 */
}

/* 上传区域标题 */
.upload-section-title {
  font-size: 14px;            /* 字体大小 */
  font-weight: 500;           /* 字体粗细 */
  color: #606266;             /* 文字颜色 */
  margin-bottom: 15px;        /* 下边距 */
  text-align: center;         /* 文字居中 */
}

/* 大尺寸上传组件容器 */
.avatar-uploader-large {
  border: 2px dashed #d9d9d9; /* 虚线边框 */
  border-radius: 8px;         /* 圆角 */
  cursor: pointer;            /* 鼠标悬停显示手型 */
  position: relative;         /* 相对定位 */
  overflow: hidden;           /* 隐藏溢出内容 */
  transition: all 0.3s;       /* 过渡动画 */
  width: 280px;               /* 固定宽度 */
  height: 400px;              /* 固定高度 */
  display: flex;              /* 弹性布局 */
  align-items: center;        /* 垂直居中 */
  justify-content: center;    /* 水平居中 */
  background-color: #fafafa;  /* 背景色 */
}

/* 上传组件悬停效果 */
.avatar-uploader-large:hover {
  border-color: #409eff;      /* 边框变蓝 */
  background-color: #f0f9ff;  /* 背景变浅蓝 */
}

/* 上传的图片样式 */
.avatar-large {
  width: 100%;                /* 宽度100% */
  height: 100%;               /* 高度100% */
  display: block;             /* 块级元素 */
  object-fit: contain;        /* 图片等比缩放，完整显示 */
}

/* 上传占位区域 */
.upload-placeholder {
  display: flex;              /* 弹性布局 */
  flex-direction: column;     /* 垂直排列 */
  align-items: center;        /* 水平居中 */
  justify-content: center;    /* 垂直居中 */
  width: 100%;                /* 宽度100% */
  height: 100%;               /* 高度100% */
  color: #8c939d;             /* 文字颜色 */
}

/* 上传图标样式 */
.upload-icon {
  font-size: 48px;            /* 图标大小 */
  margin-bottom: 10px;        /* 下边距 */
}

/* 上传提示文字 */
.upload-text {
  font-size: 14px;            /* 字体大小 */
  color: #8c939d;             /* 文字颜色 */
}

/* 上传提示信息 */
.upload-tip {
  margin-top: 15px;           /* 上边距 */
  color: #909399;             /* 文字颜色 */
  font-size: 12px;            /* 字体大小 */
  text-align: center;         /* 文字居中 */
  line-height: 1.6;           /* 行高 */
}

/* ==================== 小尺寸上传组件样式（保留用于其他地方） ==================== */
.avatar-uploader {
  border: 1px dashed #d9d9d9; /* 虚线边框 */
  border-radius: 6px;         /* 圆角 */
  cursor: pointer;            /* 鼠标悬停显示手型 */
  position: relative;         /* 相对定位 */
  overflow: hidden;           /* 隐藏溢出内容 */
  transition: all 0.3s;       /* 过渡动画 */
  width: 178px;               /* 固定宽度 */
  height: 178px;              /* 固定高度 */
  display: inline-block;      /* 行内块级元素 */
}

/* 小尺寸上传组件悬停效果 */
.avatar-uploader:hover {
  border-color: #409eff;      /* 边框变蓝 */
}

/* 上传图标容器 */
.avatar-uploader-icon {
  font-size: 28px;            /* 图标大小 */
  color: #8c939d;             /* 图标颜色 */
  width: 178px;               /* 宽度 */
  height: 178px;              /* 高度 */
  line-height: 178px;         /* 行高（垂直居中） */
  text-align: center;         /* 文字居中 */
}

/* 头像图片样式 */
.avatar {
  width: 178px;               /* 宽度 */
  height: 178px;              /* 高度 */
  display: block;             /* 块级元素 */
  object-fit: cover;          /* 图片填充覆盖 */
}

/* ==================== 响应式设计 ==================== */
/* 当屏幕宽度小于900px时，改为单栏布局 */
@media (max-width: 900px) {
  .book-detail-container,
  .book-form-container {
    flex-direction: column;    /* 改为垂直排列 */
  }

  .book-image-section {
    flex: none;                /* 取消弹性 */
    width: 100%;               /* 宽度100% */
  }

  .avatar-uploader-large {
    width: 100%;               /* 宽度100% */
    max-width: 280px;          /* 最大宽度280px */
    margin: 0 auto;            /* 水平居中 */
  }
}
</style>