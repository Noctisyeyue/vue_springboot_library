<template>
  <div class="home" style ="padding: 10px">

    <!-- 搜索-->
    <div style="margin: 10px 0;">
      <el-form inline="true" size="small">
        <el-form-item label="图书编号" >
          <el-input v-model="search1" placeholder="请输入图书编号"  clearable>
            <template #prefix><el-icon class="el-input__icon"><search/></el-icon></template>
          </el-input>
        </el-form-item >
        <el-form-item label="图书名称" >
          <el-input v-model="search2" placeholder="请输入图书名称"  clearable>
            <template #prefix><el-icon class="el-input__icon"><search /></el-icon></template>
          </el-input>
        </el-form-item >
        <el-form-item label="作者" >
          <el-input v-model="search3" placeholder="请输入作者"  clearable>
            <template #prefix><el-icon class="el-input__icon"><search /></el-icon></template>
          </el-input>
        </el-form-item >
        <el-form-item>
          <el-button type="primary" style="margin-left: 1%" @click="load" size="mini" >
            <svg-icon iconClass="search"/>查询
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button size="mini"  type="danger" @click="clear">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right" v-if="numOfOutDataBook!=0">
          <el-popconfirm
              confirm-button-text="查看"
              cancel-button-text="取消"
              :icon="InfoFilled"
              icon-color="red"
              title="您有图书已逾期，请尽快归还"
              @confirm="toLook"
          >
            <template #reference>
              <el-button  type="warning">逾期通知</el-button>
            </template>
          </el-popconfirm>
        </el-form-item>
      </el-form>
    </div>
    <!-- 按钮-->
    <div style="margin: 10px 0;" >
      <el-button type="primary" @click = "add" v-if="user.role == 1">上架</el-button>
      <el-popconfirm title="确认删除?" @confirm="deleteBatch" v-if="user.role == 1">
        <template #reference>
          <el-button type="danger" size="mini" >批量删除</el-button>
        </template>
      </el-popconfirm>
    </div>
    <!-- 数据字段-->
    <el-table :data="tableData" stripe border="true" @selection-change="handleSelectionChange">
      <el-table-column v-if="user.role ==1"
                       type="selection"
                       width="55">
      </el-table-column>
      <el-table-column prop="isbn" label="图书编号" sortable />
      <el-table-column prop="name" label="图书名称">
        <template v-slot="scope">
          <el-button type="text" @click="showDetail(scope.row)" style="color: #409EFF; font-weight: 500;">
            {{ scope.row.name }}
          </el-button>
        </template>
      </el-table-column>
      <el-table-column prop="author" label="作者" />
      <el-table-column prop="publisher" label="出版社" />
      <el-table-column label="出版日期" width="100">
        <template v-slot="scope">
          {{ getYear(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="剩余可借" width="100">
        <template v-slot="scope">
          <el-tag :type="getAvailableQuantity(scope.row) > 0 ? 'success' : 'info'">
            {{ getAvailableQuantity(scope.row) }} 本
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template v-slot="scope">
          <el-tag v-if="getAvailableQuantity(scope.row) <= 0" type="warning">不可借阅</el-tag>
          <el-tag v-else type="success">可借阅</el-tag>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="240">
        <template v-slot="scope">
          <el-button  size="mini" @click ="handleEdit(scope.row)" v-if="user.role == 1">修改</el-button>
          <el-popconfirm title="确认删除?" @confirm="handleDelete(scope.row.id)" v-if="user.role == 1">
            <template #reference>
              <el-button type="danger" size="mini" >删除</el-button>
            </template>
          </el-popconfirm>
          <el-button  size="mini" @click ="handlelend(scope.row)" v-if="user.role == 2" :disabled="getAvailableQuantity(scope.row) <= 0">借阅</el-button>
          <el-popconfirm title="确认还书?" @confirm="handlereturn(scope.row)" v-if="user.role == 2">
            <template #reference>
              <el-button type="danger" size="mini" :disabled="(this.isbnArray.indexOf(scope.row.isbn)) == -1" >还书</el-button>
            </template>
          </el-popconfirm>
          <el-button 
            size="mini" 
            :type="scope.row.isCollected ? 'warning' : 'info'" 
            @click="toggleCollection(scope.row)" 
            v-if="user.role == 2"
            :icon="scope.row.isCollected ? 'StarFilled' : 'Star'">
            {{ scope.row.isCollected ? '已收藏' : '收藏' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 图书详情对话框 -->
    <el-dialog
        v-model="dialogVisibleDetail"
        title="图书详情"
        width="500px"
    >
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
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="dialogVisibleDetail = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

<!--测试,通知对话框-->
    <el-dialog
        v-model="dialogVisible3"
        v-if="numOfOutDataBook!=0"
        title="逾期详情"
        width="50%"
        :before-close="handleClose"
    >
        <el-table :data="outDateBook" style="width: 100%">
          <el-table-column prop="isbn" label="图书编号" />
          <el-table-column prop="bookName" label="书名" />
          <el-table-column prop="lendTime" label="借阅日期" />  
          <el-table-column prop="deadTime" label="截至日期" />  
        </el-table>

      <template #footer>
      <span class="dialog-footer">
        <el-button type="primary" @click="dialogVisible3 = false"
        >确认</el-button>
      </span>
      </template>
    </el-dialog>
    <!--    分页-->
    <div style="margin: 10px 0">
      <el-pagination
          v-model:currentPage="currentPage"
          :page-sizes="[5, 10, 20]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      >
      </el-pagination>

      <el-dialog v-model="dialogVisible" title="上架书籍" width="30%">
        <el-form :model="form" label-width="120px">
          <el-form-item label="图书编号">
            <el-input style="width: 80%" v-model="form.isbn"></el-input>
          </el-form-item>
          <el-form-item label="图书名称">
            <el-input style="width: 80%" v-model="form.name"></el-input>
          </el-form-item>
          <el-form-item label="价格">
            <el-input style="width: 80%" v-model="form.price"></el-input>
          </el-form-item>
          <el-form-item label="作者">
            <el-input style="width: 80%" v-model="form.author"></el-input>
          </el-form-item>
          <el-form-item label="出版社">
            <el-input style="width: 80%" v-model="form.publisher"></el-input>
          </el-form-item>
          <el-form-item label="出版时间">
            <div>
              <el-date-picker value-format="YYYY-MM-DD" type="date" style="width: 80%" clearable v-model="form.createTime" ></el-date-picker>
            </div>
          </el-form-item>
          <el-form-item label="馆藏数量">
            <el-input-number style="width: 80%" v-model="form.totalQuantity" :min="1" :max="9999"></el-input-number>
          </el-form-item>
        </el-form>
        <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">确 定</el-button>
      </span>
        </template>
      </el-dialog>

      <el-dialog v-model="dialogVisible2" title="修改书籍信息" width="30%">
        <el-form :model="form" label-width="120px">
          <el-form-item label="图书编号">
            <el-input style="width: 80%" v-model="form.isbn"></el-input>
          </el-form-item>
          <el-form-item label="图书名称">
            <el-input style="width: 80%" v-model="form.name"></el-input>
          </el-form-item>
          <el-form-item label="价格">
            <el-input style="width: 80%" v-model="form.price"></el-input>
          </el-form-item>
          <el-form-item label="作者">
            <el-input style="width: 80%" v-model="form.author"></el-input>
          </el-form-item>
          <el-form-item label="出版社">
            <el-input style="width: 80%" v-model="form.publisher"></el-input>
          </el-form-item>
          <el-form-item label="出版时间">
            <div>
              <el-date-picker value-format="YYYY-MM-DD" type="date" style="width: 80%" clearable v-model="form.createTime" ></el-date-picker>
            </div>
          </el-form-item>
          <el-form-item label="馆藏总数">
            <el-input-number style="width: 80%" v-model="form.totalQuantity" :min="1" :max="9999"></el-input-number>
          </el-form-item>
        </el-form>
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
// @ is an alias to /src
import request from "../utils/request";
import {ElMessage} from "element-plus";
import moment from "moment";
export default {
  created(){
    let userStr = sessionStorage.getItem("user") ||"{}"
    this.user = JSON.parse(userStr)
    this.load()
  },
  name: 'Book',
  methods: {
    // 获取年份
    getYear(dateStr) {
      if (!dateStr) return '-';
      const year = new Date(dateStr).getFullYear();
      return isNaN(year) ? '-' : year + '年';
    },
    
    // 计算可借阅数量
    getAvailableQuantity(book) {
      if (!book.totalQuantity || !book.borrowedQuantity) {
        return book.totalQuantity || 0;
      }
      return book.totalQuantity - book.borrowedQuantity;
    },
    
    // 显示图书详情
    showDetail(row) {
      this.detailBook = JSON.parse(JSON.stringify(row))
      this.dialogVisibleDetail = true
    },
    
    handleSelectionChange(val){
      this.ids = val.map(v =>v.id)
    },
    deleteBatch(){
      if (!this.ids.length) {
        ElMessage.warning("请选择数据!")
        return
      }
      request.post("/book/deleteBatch",this.ids).then(res =>{
        if(res.code === '0'){
          ElMessage.success("批量删除成功")
          this.load()
        }
        else {
          ElMessage.error(res.msg)
        }
      })
    },
    // 修改 load 方法 (使用 .then() 链)
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
            this.bookData = res.data.records;
            this.number = this.bookData.length;
            const nowDate = new Date();
            this.isbnArray = [];
            for (let i = 0; i < this.number; i++) {
              this.isbnArray[i] = this.bookData[i].isbn;
              let dDate = new Date(this.bookData[i].deadTime);  
              if (dDate < nowDate) {
                this.outDateBook.push({
                  isbn: this.bookData[i].isbn,
                  bookName: this.bookData[i].bookName,
                  deadTime: this.bookData[i].deadTime,  
                  lendTime: this.bookData[i].lendTime,  
                });
                this.numOfOutDataBook++;
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

    clear(){
      this.search1 = ""
      this.search2 = ""
      this.search3 = ""
      this.load()
    },

    handleDelete(id){
      request.delete("book/" + id ).then(res =>{
        console.log(res)
        if(res.code == 0 ){
          ElMessage.success("删除成功")
        }
        else
          ElMessage.error(res.msg)
        this.load()
      })
    },
    handlereturn(row){
      // 还书：已借阅数量 -1
      this.form.id = row.id
      this.form.borrowedQuantity = (row.borrowedQuantity || 0) - 1
      
      request.put("/book",this.form).then(res =>{
        console.log(res)
        if(res.code == 0){
          ElMessage({
            message: '还书成功',
            type: 'success',
          })
        }
        else {
          ElMessage.error(res.msg)
        }
      
        this.form3.isbn = row.isbn
        this.form3.readerId = this.user.id
        let endDate = moment(new Date()).format("yyyy-MM-DD HH:mm:ss")
        this.form3.returnTime = endDate
        this.form3.status = "1"
        this.form3.borrowNum = row.borrowNum 
        request.put("/LendRecord1/",this.form3).then(res =>{
          console.log(res)
          let form3 ={};
          form3.isbn = row.isbn;
          form3.bookName = row.name;
          form3.nickName = this.user.username;
          form3.readerId = this.user.id;
          form3.lendTime = endDate;  
          form3.deadTime = endDate;  
          form3.prolong  = 1;
          request.post("/bookwithuser/deleteRecord",form3).then(res =>{
            console.log(res)
            this.load()
          })
        })
      })
    },
    handlelend(row){
      if(this.number ==5){
        ElMessage.warning("您不能再借阅更多的书籍了")
        return;
      }
      if(this.numOfOutDataBook !=0){
        ElMessage.warning("在您归还逾期书籍前不能再借阅书籍")
        return;
      }
      
      // 检查是否有可借阅的书
      if(this.getAvailableQuantity(row) <= 0){
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
      let nowDate = new Date(startDate);
      nowDate.setDate(nowDate.getDate()+30);
      form3.deadTime = moment(nowDate).format("yyyy-MM-DD HH:mm:ss");
      form3.prolong  = 1;
      
      // 先检查是否已借阅该书
      console.log('准备插入借阅记录:', form3)
      request.post("/bookwithuser/insertNew", form3).then(res =>{
        console.log('插入借阅记录返回:', res)
        if(res.code !== '0' && res.code !== 0){
          // 借阅失败，显示错误信息
          ElMessage.error(res.msg || '借阅失败')
          return;
        }
        
        // 借阅成功，继续更新图书信息
        this.form.id = row.id
        this.form.borrowNum = (row.borrowNum || 0) + 1 
        this.form.borrowedQuantity = (row.borrowedQuantity || 0) + 1
        
        request.put("/book", this.form).then(bookRes =>{
          if(bookRes.code == 0){
            ElMessage.success('借阅成功')
            
            // 添加到历史借阅记录
            this.form2.status = "0"
            this.form2.bookId = row.id  // 添加图书ID - 这是必需的字段
            this.form2.isbn = row.isbn
            this.form2.bookname = row.name
            this.form2.readerId = this.user.id
            this.form2.borrowNum = (row.borrowNum || 0) + 1
            this.form2.lendTime = startDate
            
            request.post("/LendRecord", this.form2).then(() =>{
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
    add(){
      this.dialogVisible= true
      this.form ={}
    },
    save(){
      if(this.form.id){
        request.put("/book",this.form).then(res =>{
          console.log(res)
          if(res.code == 0){
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
      }
      else {
        // 新增图书时初始化字段
        this.form.borrowNum = 0 
        this.form.borrowedQuantity = 0
        this.form.totalQuantity = this.form.totalQuantity || 1
        this.form.status = 1
        
        request.post("/book",this.form).then(res =>{
          console.log(res)
          if(res.code == 0){
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

    handleEdit(row){
      this.form = JSON.parse(JSON.stringify(row))
      this.dialogVisible2 = true
    },
    handleSizeChange(pageSize){
      this.pageSize = pageSize
      this.load()
    },
    handleCurrentChange(pageNum){
      this.pageNum = pageNum
      this.load()
    },
    toLook(){
      this.dialogVisible3 =true;
    },
    // 点击收藏按钮时触发
    toggleCollection(row) {
      const params = {
        readerId: this.user.id,
        bookId: row.id,
      };

      if (row.isCollected) {
        // 当前是“已收藏”，调用“取消收藏”接口
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
        // 当前是“未收藏”，调用“添加收藏”接口
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
  },
  data() {
    return {
      form: {},
      form2:{},
      form3:{},
      dialogVisible: false,
      dialogVisible2: false,
      dialogVisibleDetail: false,
      detailBook: {},
      search1:'',
      search2:'',
      search3:'',
      total:10,
      currentPage:1,
      pageSize: 10,
      tableData: [],
      user:{},
      number:0,
      bookData:[],
      isbnArray:[],
      outDateBook:[],
      numOfOutDataBook: 0,
      dialogVisible3 : true,
    }
  },
}
</script>

<style scoped>
.detail-label {
  font-weight: bold;
  background-color: #f5f7fa;
}
</style>