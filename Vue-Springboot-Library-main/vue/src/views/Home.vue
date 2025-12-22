<template>
  <div class="home-container">
    <!-- 个人信息模块 -->
    <div class="welcome-section">
      <div class="welcome-card">
        <div class="section-title">
          <h3>基本信息</h3>
        </div>
        <div class="welcome-info">
          <div class="info-row">
            <div class="info-item">
              <span class="info-label">用户名：</span>
              <span class="info-value">{{ user.username }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">性别：</span>
              <span class="info-value">{{ user.sex || '未设置' }}</span>
            </div>
          </div>
          <div class="info-row">
            <div class="info-item">
              <span class="info-label">电话：</span>
              <span class="info-value">{{ user.phone || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">地址：</span>
              <span class="info-value">{{ user.address || '未设置' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 阅读贴士区域 -->
    <div class="tips-section">
      <div class="tips-card">
        <div class="section-title">
          <h3>阅读贴士</h3>
          <p>让阅读成为生活的一部分</p>
        </div>
        <div class="tips-container">
          <div class="tips-column">
            <div class="tip-item" v-for="(tip, index) in readingTips.slice(0, 2)" :key="index">
              <div class="tip-bullet" :style="{ backgroundColor: tip.color }"></div>
              <div class="tip-text-content">
                <strong>{{ tip.title }}</strong>
                <span>{{ tip.text }}</span>
              </div>
            </div>
          </div>
          <div class="tips-column">
            <div class="tip-item" v-for="(tip, index) in readingTips.slice(2)" :key="index + 2">
              <div class="tip-bullet" :style="{ backgroundColor: tip.color }"></div>
              <div class="tip-text-content">
                <strong>{{ tip.title }}</strong>
                <span>{{ tip.text }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 热门推荐区域 -->
    <div class="books-section">
      <div class="books-card">
        <div class="section-title">
          <h3>热门推荐</h3>
          <p>借阅量最高的图书</p>
        </div>
        <div class="books-grid" v-loading="loading">
          <div 
            v-for="book in topBooks" 
            :key="book.id" 
            class="book-card"
            @click="showBookDetail(book)">
            <div class="book-image-wrapper">
              <el-image 
                v-if="book.bookPicture && book.bookPicture.trim()"
                :src="getImageUrl(book.bookPicture)" 
                class="book-image"
                fit="cover"
                :preview-src-list="[]"
                :hide-on-click-modal="true">
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div v-else class="book-image-placeholder">
                <el-icon class="placeholder-icon"><Picture /></el-icon>
                <span class="placeholder-text">暂无封面</span>
              </div>
              <!-- 借阅量标签 -->
              <div class="borrow-badge">
                <el-icon><TrendCharts /></el-icon>
                <span>{{ book.borrowNum || 0 }}次借阅</span>
              </div>
            </div>
            <div class="book-info">
              <h3 class="book-title">{{ book.name }}</h3>
              <p class="book-author">{{ book.author }}</p>
              <div class="book-meta">
                <el-tag :type="getAvailableQuantity(book) > 0 ? 'success' : 'warning'" size="small">
                  {{ getAvailableQuantity(book) > 0 ? '可借阅' : '已借完' }}
                </el-tag>
                <span class="book-price">¥{{ book.price }}</span>
              </div>
            </div>
          </div>
          <!-- 如果没有图书 -->
          <el-empty v-if="!loading && topBooks.length === 0" description="暂无热门图书" />
        </div>
      </div>
    </div>

    <!-- 图书详情对话框 -->
    <el-dialog v-model="dialogVisibleDetail" title="图书详情" width="800px">
      <div class="book-detail-container">
        <!-- 左侧图片区域 -->
        <div class="book-image-section">
          <el-image 
            v-if="detailBook.bookPicture && detailBook.bookPicture.trim()"
            :src="getImageUrl(detailBook.bookPicture)" 
            class="book-cover-image"
            fit="contain"
            :preview-src-list="[getImageUrl(detailBook.bookPicture)]"
            preview-teleported
            :initial-index="0">
          </el-image>
          <div v-else class="book-cover-placeholder">
            <el-icon style="font-size: 48px; color: #c0c4cc;"><Picture /></el-icon>
            <span>暂无封面图片</span>
          </div>
        </div>
        <!-- 右侧信息区域 -->
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
  </div>
</template>

<script>
import request from "../utils/request";
import { ElMessage } from "element-plus";
import { Picture, TrendCharts, Reading, Collection, Clock, InfoFilled, User, UserFilled, Phone, Location, Male, Female } from "@element-plus/icons-vue";

export default {
  name: 'Home',
  components: {
    Picture,
    TrendCharts,
    Reading,
    Collection,
    Clock,
    InfoFilled,
    User,
    UserFilled,
    Phone,
    Location,
    Male,
    Female
  },
  data() {
    return {
      user: {},
      topBooks: [],
      loading: false,
      dialogVisibleDetail: false,
      detailBook: {},
      readingTips: [
        {
          icon: 'Reading',
          title: '阅读改变人生',
          text: '书籍是人类进步的阶梯，每天阅读30分钟，一年可以阅读12本书',
          color: '#409eff'
        },
        {
          icon: 'Clock',
          title: '借阅期限提醒',
          text: '图书借阅期限为30天，可续借1次，请及时归还避免逾期',
          color: '#67c23a'
        },
        {
          icon: 'Collection',
          title: '收藏功能',
          text: '遇到喜欢的图书可以点击收藏，方便下次快速查找',
          color: '#e6a23c'
        },
        {
          icon: 'InfoFilled',
          title: '热门推荐',
          text: '首页展示借阅量最高的图书，帮助您发现更多优质读物',
          color: '#f56c6c'
        }
      ]
    }
  },
  created() {
    // 获取用户信息
    let userStr = sessionStorage.getItem("user") || "{}";
    this.user = JSON.parse(userStr);
    this.loadTopBooks();
  },
  methods: {
    // 加载热门图书
    loadTopBooks() {
      this.loading = true;
      request.get("/book/top", {
        params: {
          limit: 5
        }
      }).then(res => {
        if (res.code === '0' || res.code === 0) {
          this.topBooks = res.data || [];
        } else {
          ElMessage.error(res.msg || '加载热门图书失败');
        }
      }).catch(error => {
        console.error("加载热门图书失败", error);
        ElMessage.error("加载热门图书失败");
      }).finally(() => {
        this.loading = false;
      });
    },
    // 显示图书详情
    showBookDetail(book) {
      this.detailBook = JSON.parse(JSON.stringify(book));
      this.dialogVisibleDetail = true;
    },
    // 获取图片URL
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
    // 计算可借阅数量
    getAvailableQuantity(book) {
      if (!book.totalQuantity || !book.borrowedQuantity) {
        return book.totalQuantity || 0;
      }
      return book.totalQuantity - book.borrowedQuantity;
    }
  }
}
</script>

<style scoped>
.home-container {
  padding: 30px;
  min-height: calc(100vh - 60px);
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

/* 个人信息模块 */
.welcome-section {
  margin-bottom: 30px;
}

.welcome-card {
  background: #ffffff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid #e4e7ed;
}

.welcome-card .section-title {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.welcome-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 6px;
  border-left: 3px solid #409eff;
  transition: all 0.2s ease;
}

.info-item:hover {
  background: #ecf5ff;
  border-left-color: #66b1ff;
}

.info-label {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
  white-space: nowrap;
}

.info-value {
  font-size: 14px;
  color: #303133;
  font-weight: 600;
}

/* 区域标题 */
.section-title {
  text-align: left;
  margin-bottom: 20px;
}

.section-title h3 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 6px 0;
}

.section-title p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

/* 图书展示区域 */
.books-section {
  width: 100%;
  margin-bottom: 40px;
  clear: both;
}

/* 图书展示区域容器 */
.books-card {
  background: #ffffff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid #e4e7ed;
}

.books-card .section-title {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

/* 图书网格布局 */
.books-grid {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  width: 100%;
  flex-wrap: wrap;
}

/* 图书卡片 */
.book-card {
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  width: calc(20% - 13px);
  min-width: 200px;
  flex: 0 0 auto;
}

.book-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

/* 图片区域 */
.book-image-wrapper {
  position: relative;
  width: 100%;
  height: 180px;
  overflow: hidden;
  background: #f5f7fa;
}

.book-image {
  width: 100%;
  height: 100%;
  transition: transform 0.3s ease;
  pointer-events: none; /* 禁用图片的点击事件，防止触发预览 */
}

.book-card:hover .book-image {
  transform: scale(1.05);
}

.book-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e9ecef 100%);
  color: #909399;
}

.placeholder-icon {
  font-size: 64px;
  margin-bottom: 10px;
  opacity: 0.5;
}

.placeholder-text {
  font-size: 14px;
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #c0c4cc;
}

.image-error .el-icon {
  font-size: 48px;
}

/* 借阅量标签 */
.borrow-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 6px 12px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
  color: #409eff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.borrow-badge .el-icon {
  font-size: 14px;
}

/* 图书信息 */
.book-info {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.book-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
}

.book-author {
  font-size: 14px;
  color: #909399;
  margin: 0 0 12px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.book-price {
  font-size: 18px;
  font-weight: 700;
  color: #f56c6c;
}

/* 图书详情对话框样式（复用Book.vue的样式） */
.book-detail-container {
  display: flex;
  gap: 30px;
  padding: 10px 0;
}

.book-image-section {
  flex: 0 0 280px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.book-cover-image {
  width: 100%;
  max-width: 280px;
  height: 400px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  object-fit: contain;
  background-color: #fafafa;
}

.book-cover-placeholder {
  width: 100%;
  max-width: 280px;
  height: 400px;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #fafafa;
  color: #909399;
  gap: 10px;
}

.book-info-section {
  flex: 1;
  min-width: 0;
}

.detail-label {
  font-weight: bold;
  background-color: #f5f7fa;
}

/* 阅读贴士区域 */
.tips-section {
  margin-bottom: 30px;
}

.tips-card {
  background: #ffffff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid #e4e7ed;
}

.tips-card .section-title {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.tips-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.tips-column {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.tip-item:hover {
  background: #f5f7fa;
}

.tip-bullet {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 6px;
}

.tip-text-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.tip-text-content strong {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  display: block;
}

.tip-text-content span {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .welcome-info {
    grid-template-columns: 1fr;
  }

  .welcome-card {
    padding: 15px;
  }

  .book-card {
    width: calc(50% - 12px);
    min-width: 180px;
  }

  .tips-container {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .book-image-wrapper {
    height: 160px;
  }
}

@media (max-width: 1400px) {
  .book-card {
    width: calc(25% - 12px);
  }
}

@media (max-width: 1200px) {
  .book-card {
    width: calc(33.333% - 11px);
  }
}

@media (max-width: 480px) {
  .book-card {
    width: 100%;
  }

  .home-container {
    padding: 20px;
  }
}
</style>

