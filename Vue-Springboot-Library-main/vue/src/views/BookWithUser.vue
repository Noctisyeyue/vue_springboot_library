<template>
  <div class="home" style="padding: 10px">
    <!-- 搜索-->
    <div style="margin: 10px 0;">

      <el-form inline="true" size="small">
        <el-form-item label="图书编号">
          <el-input v-model="search1" placeholder="请输入图书编号" clearable>
            <template #prefix><el-icon class="el-input__icon">
                <search />
              </el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="图书名称">
          <el-input v-model="search2" placeholder="请输入图书名称" clearable>
            <template #prefix><el-icon class="el-input__icon">
                <search />
              </el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item label="借阅者" v-if="user.role == 1">
          <el-input v-model="search3" placeholder="请输入借阅者昵称" clearable>
            <template #prefix><el-icon class="el-input__icon">
                <search />
              </el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="margin-left: 1%" @click="load" size="mini">查询</el-button>
        </el-form-item>
        <el-form-item>
          <el-button size="mini" type="danger" @click="clear">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <!-- 按钮-->
    <div style="margin: 10px 0;">
      <el-popconfirm title="确认删除?" @confirm="deleteBatch" v-if="user.role == 1">
        <template #reference>
          <el-button type="danger" size="mini">批量删除</el-button>
        </template>
      </el-popconfirm>
    </div>
    <!-- 数据字段-->
    <el-table :data="tableData" stripe border="true" @selection-change="handleSelectionChange" style="width: 100%;">
        <el-table-column v-if="user.role == 1" type="selection" width="55">
        </el-table-column>
        <el-table-column prop="isbn" label="图书编号" sortable />
        <el-table-column prop="bookName" label="图书名称" />
        <el-table-column v-if="user.role == 1" prop="readerId" label="读者编号" />
        <el-table-column v-if="user.role == 1" prop="nickName" label="借阅者" />
        <el-table-column prop="lendTime" label="借阅时间" />
        <el-table-column prop="deadTime" label="最迟归还日期" />
        <el-table-column prop="prolong" label="可续借次数" />
        <el-table-column fixed="right" label="操作" width="200">
          <template v-slot="scope">
            <el-button size="mini" @click="handleEdit(scope.row)" v-if="user.role == 1">修改</el-button>
            <el-popconfirm title="确认删除?" @confirm="handleDelete(scope.row)" v-if="user.role == 1">
              <template #reference>
                <el-button type="danger" size="mini">删除</el-button>
              </template>
            </el-popconfirm>
            <el-popconfirm title="确认续借(续借一次延长30天)?" @confirm="handleProlong(scope.row)" v-if="user.role == 2"
              :disabled="scope.row.prolong == 0">
              <template #reference>
                <el-button type="warning" size="mini" :disabled="scope.row.prolong == 0">续借</el-button>
              </template>
            </el-popconfirm>
            <el-popconfirm title="确认还书?" @confirm="handleReturn(scope.row)" v-if="user.role == 2">
              <template #reference>
                <el-button type="primary" size="mini">还书</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    <!--    分页-->
    <div style="margin: 10px 0">
      <el-pagination v-model:currentPage="currentPage" :page-sizes="[5, 10, 20]" :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper" :total="total" @size-change="handleSizeChange"
        @current-change="handleCurrentChange">
      </el-pagination>

      <el-dialog v-model="dialogVisible2" title="修改借阅信息" width="30%">
        <el-form :model="form" label-width="120px">
          <el-form-item label="最迟归还日期">
            <el-date-picker v-model="form.deadTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 80%">
            </el-date-picker>
          </el-form-item>
          <el-form-item label="可续借次数">
            <el-input-number style="width: 80%" v-model="form.prolong" :min="0" :max="10"></el-input-number>
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
import { ElMessage } from "element-plus";
import moment from "moment";
export default {
  created() {
    let userStr = sessionStorage.getItem("user") || "{}"
    this.user = JSON.parse(userStr)
    this.load()
  },
  name: 'bookwithuser',
  methods: {

    handleSelectionChange(val) {
      this.forms = val
    },
    async deleteBatch() {
      if (!this.forms.length) {
        ElMessage.warning("请选择数据！")
        return
      }
      // 对每个选中项分别执行删除逻辑
      let successCount = 0;
      let failCount = 0;
      const total = this.forms.length;
      // 记录每本书需要减少的借阅数量
      const bookUpdateMap = {};
      
      for (const record of this.forms) {
        try {
          // 先删除记录，不更新book表
          await this.deleteSingleRecordWithoutBookUpdate(record);
          // 记录需要更新的书籍
          if (!bookUpdateMap[record.bookId]) {
            bookUpdateMap[record.bookId] = 0;
          }
          bookUpdateMap[record.bookId]++;
          successCount++;
        } catch (error) {
          failCount++;
          console.error("删除记录失败", error);
        }
      }
      
      // 统一更新每本书的借阅数量
      const updatePromises = Object.keys(bookUpdateMap).map(async (bookId) => {
        try {
          const decreaseCount = bookUpdateMap[bookId];
          const bookRes = await request.get(`/book/${bookId}`);
          if (bookRes.code === 0 || bookRes.code === '0') {
            const book = bookRes.data || {};
            const borrowedQuantity = Math.max((book.borrowedQuantity || 0) - decreaseCount, 0);
            const bookUpdate = {
              id: bookId,
              borrowedQuantity,
              status: '1'
            };
            await request.put("/book", bookUpdate);
          }
        } catch (error) {
          console.error(`更新图书 ${bookId} 信息失败`, error);
        }
      });
      
      await Promise.all(updatePromises);
      
      if (failCount === 0) {
        ElMessage.success(`批量删除成功，共删除 ${successCount} 条记录`);
      } else {
        ElMessage.warning(`批量删除完成，成功 ${successCount} 条，失败 ${failCount} 条`);
      }
      this.load();
    },
    load() {
      if (this.user.role == 1) {
        request.get("/bookwithuser", {
          params: {
            pageNum: this.currentPage,
            pageSize: this.pageSize,
            search1: this.search1,
            search2: this.search2,
            search3: this.search3,
          }
        }).then(res => {
          console.log(res)
          this.tableData = res.data.records
          this.total = res.data.total
        })
      }
      else {
        request.get("/bookwithuser", {
          params: {
            pageNum: this.currentPage,
            pageSize: this.pageSize,
            search1: this.search1,
            search2: this.search2,
            search3: this.user.id,
          }
        }).then(res => {
          console.log(res)
          this.tableData = res.data.records
          this.total = res.data.total
        })
      }
    },
    clear() {
      this.search1 = ""
      this.search2 = ""
      this.search3 = ""
      this.load()
    },
    handleDelete(row) {
      this.deleteSingleRecord(row).then(() => {
        ElMessage.success("删除成功");
        this.load();
      }).catch(error => {
        ElMessage.error("删除失败");
        console.error("删除失败", error);
      });
    },
    deleteSingleRecord(row) {
      return new Promise((resolve, reject) => {
        // 1) 删除LendRecord中的对应记录
        const lendRecordPayload = {
          readerId: row.readerId,
          bookId: row.bookId,
          lendTime: row.lendTime
        };
        
        request.delete("/LendRecord", { data: lendRecordPayload }).then(() => {
          // 2) 删除bookwithuser记录
          const payload = {
            bookId: row.bookId,
            readerId: row.readerId,
            lendTime: row.lendTime
          };
          
          request.post("bookwithuser/deleteRecord", payload).then(res => {
            if (res.code == 0 || res.code === '0') {
              // 3) 更新book表：可借阅数量加一
              request.get(`/book/${row.bookId}`).then(bookRes => {
                if (bookRes.code === 0 || bookRes.code === '0') {
                  const book = bookRes.data || {};
                  const borrowedQuantity = Math.max((book.borrowedQuantity || 0) - 1, 0);
                  const bookUpdate = {
                    id: row.bookId,
                    borrowedQuantity,
                    status: '1'
                  };
                  
                  request.put("/book", bookUpdate).then(() => {
                    resolve();
                  }).catch(error => {
                    console.error("更新图书信息失败", error);
                    resolve(); // 即使更新失败也认为删除成功
                  });
                } else {
                  resolve();
                }
              }).catch(error => {
                console.error("获取图书信息失败", error);
                resolve(); // 即使获取失败也认为删除成功
              });
            } else {
              reject(new Error(res.msg || '删除失败'));
            }
          }).catch(error => {
            console.error("删除借阅记录失败", error);
            reject(error);
          });
        }).catch(error => {
          console.error("删除借阅历史失败", error);
          reject(error);
        });
      });
    },
    deleteSingleRecordWithoutBookUpdate(row) {
      return new Promise((resolve, reject) => {
        // 1) 删除LendRecord中的对应记录
        const lendRecordPayload = {
          readerId: row.readerId,
          bookId: row.bookId,
          lendTime: row.lendTime
        };
        
        request.delete("/LendRecord", { data: lendRecordPayload }).then(() => {
          // 2) 删除bookwithuser记录
          const payload = {
            bookId: row.bookId,
            readerId: row.readerId,
            lendTime: row.lendTime
          };
          
          request.post("bookwithuser/deleteRecord", payload).then(res => {
            if (res.code == 0 || res.code === '0') {
              resolve();
            } else {
              reject(new Error(res.msg || '删除失败'));
            }
          }).catch(error => {
            console.error("删除借阅记录失败", error);
            reject(error);
          });
        }).catch(error => {
          console.error("删除借阅历史失败", error);
          reject(error);
        });
      });
    },
    handleProlong(row) {
      const dead = new Date(row.deadTime);
      dead.setDate(dead.getDate() + 30);
      const updated = {
        ...row,
        deadTime: moment(dead).format("yyyy-MM-DD HH:mm:ss"),
        prolong: row.prolong - 1,
      }
      request.post("/bookwithuser", updated).then(res => {
        console.log(res)
        if (res.code == 0) {
          ElMessage({
            message: '续借成功',
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
    handleReturn(row) {
      // 1) 查询图书详情，计算借出数量-1并更新状态
      request.get(`/book/${row.bookId}`).then(bookRes => {
        if (bookRes.code !== 0 && bookRes.code !== '0') {
          ElMessage.error(bookRes.msg || '获取图书信息失败')
          return
        }
        const book = bookRes.data || {}
        const borrowedQuantity = Math.max((book.borrowedQuantity || 0) - 1, 0)
        const bookUpdate = {
          id: row.bookId,
          borrowedQuantity,
          status: '1'
        }
        request.put("/book", bookUpdate).then(updateRes => {
          if (updateRes.code !== 0 && updateRes.code !== '0') {
            ElMessage.error(updateRes.msg || '更新图书状态失败')
            return
          }
          // 2) 更新借阅历史为已归还
          const lendPayload = {
            isbn: row.isbn,
            readerId: row.readerId,
            lendTime: row.lendTime
          }
          request.put("/LendRecord1", lendPayload).then(() => {
            // 3) 删除当前借阅记录（bookwithuser）
            const payload = {
              bookId: row.bookId,
              readerId: row.readerId,
              lendTime: row.lendTime
            }
            request.post("bookwithuser/deleteRecord", payload).then(res => {
              if (res.code == 0 || res.code === '0') {
                ElMessage.success("归还成功")
              } else {
                ElMessage.error(res.msg || '归还失败')
              }
              this.load()
            })
          })
        })
      })
    },
    save() {
      if (this.form.prolong === undefined || this.form.prolong === null) {
        ElMessage.error("请填写可续借次数");
        return;
      }
      if (!this.form.deadTime) {
        ElMessage.error("请填写最迟归还日期");
        return;
      }
      
      // 直接保存表单数据，不自动调整归还日期
      request.post("/bookwithuser", this.form).then(res => {
        console.log(res)
        if (res.code == 0) {
          ElMessage({
            message: '修改信息成功',
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

    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row))
      this.dialogVisible2 = true
    },
    handleSizeChange(pageSize) {
      this.pageSize = pageSize
      this.load()
    },
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
      this.load()
    },

  },
  data() {
    return {
      form: {},
      form2: {},
      form3: {},
      dialogVisible: false,
      dialogVisible2: false,
      search1: '',
      search2: '',
      search3: '',
      total: 10,
      currentPage: 1,
      pageSize: 10,
      tableData: [],
      user: {},
      forms: [],
    }
  },
}
</script>
