<template>
<!-- 借阅记录管理页面容器 -->
<div style="padding: 10px">
    <!-- 搜索表单区域：支持按图书编号、图书名称、借阅者搜索 -->
    <div style="margin: 10px 0;">
      <!-- Element Plus 表单组件：inline属性实现表单项横向排列 -->
      <el-form inline="true" size="small">
        <!-- 图书编号搜索框 -->
        <el-form-item label="图书编号">
          <el-input v-model="search1" placeholder="请输入图书编号" clearable>
            <template #prefix><el-icon class="el-input__icon">
                <search />  <!-- 搜索图标 -->
              </el-icon></template>
          </el-input>
        </el-form-item>
        <!-- 图书名称搜索框 -->
        <el-form-item label="图书名称">
          <el-input v-model="search2" placeholder="请输入图书名称" clearable>
            <template #prefix><el-icon class="el-input__icon">
                <search />
              </el-icon></template>
          </el-input>
        </el-form-item>
        <!-- 借阅者搜索框：仅管理员可见 -->
        <el-form-item label="借阅者" v-if="user.role == 1">
          <el-input v-model="search3" placeholder="请输入借阅者昵称" clearable>
            <template #prefix><el-icon class="el-input__icon">
                <search />
              </el-icon></template>
          </el-input>
        </el-form-item>
        <!-- 查询按钮 -->
        <el-form-item>
          <el-button type="primary" style="margin-left: 1%" @click="load" size="mini">查询</el-button>
        </el-form-item>
        <!-- 重置按钮：清空搜索条件 -->
        <el-form-item>
          <el-button size="mini" type="danger" @click="clear">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <!-- 操作按钮区域：批量删除等功能 -->
    <div style="margin: 10px 0;">
      <!-- 批量删除按钮：仅管理员可见，带二次确认 -->
      <el-popconfirm title="确认删除?" @confirm="deleteBatch" v-if="user.role == 1">
        <template #reference>
          <el-button type="danger" size="mini">批量删除</el-button>
        </template>
      </el-popconfirm>
    </div>
    <!-- 数据表格区域：展示借阅记录列表 -->
    <el-table :data="tableData" stripe border="true" @selection-change="handleSelectionChange" style="width: 100%;">
        <!-- 复选框列：仅管理员可见，用于批量选择 -->
        <el-table-column v-if="user.role == 1" type="selection" width="55">
        </el-table-column>
        <el-table-column prop="isbn" label="图书编号" sortable />  <!-- sortable属性支持排序 -->
        <el-table-column prop="bookName" label="图书名称" />
        <el-table-column v-if="user.role == 1" prop="readerId" label="读者编号" sortable/>  <!-- 仅管理员可见 -->
        <el-table-column v-if="user.role == 1" prop="nickName" label="借阅者" />  <!-- 仅管理员可见 -->
        <el-table-column prop="lendTime" label="借阅时间" sortable/>
        <el-table-column prop="deadTime" label="最迟归还日期" sortable/>
        <el-table-column prop="prolong" label="可续借次数" />
        <!-- 操作列：固定在右侧 -->
        <el-table-column fixed="right" label="操作" width="200">
          <template v-slot="scope">
            <!-- 修改按钮：仅管理员可见 -->
            <el-button size="mini" @click="handleEdit(scope.row)" v-if="user.role == 1">修改</el-button>
            <!-- 删除按钮：仅管理员可见，带二次确认 -->
            <el-popconfirm title="确认删除?" @confirm="handleDelete(scope.row)" v-if="user.role == 1">
              <template #reference>
                <el-button type="danger" size="mini">删除</el-button>
              </template>
            </el-popconfirm>
            <!-- 续借按钮：仅普通用户可见，当prolong为0时禁用，带二次确认 -->
            <el-popconfirm title="确认续借(续借一次延长30天)?" @confirm="handleProlong(scope.row)" v-if="user.role == 2"
              :disabled="scope.row.prolong == 0">
              <template #reference>
                <el-button type="warning" size="mini" :disabled="scope.row.prolong == 0">续借</el-button>
              </template>
            </el-popconfirm>
            <!-- 还书按钮：仅普通用户可见，带二次确认 -->
            <el-popconfirm title="确认还书?" @confirm="handleReturn(scope.row)" v-if="user.role == 2">
              <template #reference>
                <el-button type="primary" size="mini">还书</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    <!-- 分页组件：支持每页显示数量切换和页码跳转 -->
    <div style="margin: 10px 0">
      <el-pagination v-model:currentPage="currentPage" :page-sizes="[5, 10, 20]" :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper" :total="total" @size-change="handleSizeChange"
        @current-change="handleCurrentChange">
      </el-pagination>

      <!-- 修改借阅信息对话框 -->
      <el-dialog v-model="dialogVisible2" title="修改借阅信息" width="30%">
        <el-form :model="form" label-width="120px">
          <!-- 最迟归还日期选择器 -->
          <el-form-item label="最迟归还日期">
            <el-date-picker v-model="form.deadTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 80%">
            </el-date-picker>
          </el-form-item>
          <!-- 可续借次数输入框：数字选择器，范围0-10 -->
          <el-form-item label="可续借次数">
            <el-input-number style="width: 80%" v-model="form.prolong" :min="0" :max="10"></el-input-number>
          </el-form-item>
        </el-form>
        <!-- 对话框底部按钮 -->
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
import request from "../utils/request";        // HTTP 请求工具
import { ElMessage } from "element-plus";        // Element Plus 消息提示组件
import moment from "moment";                    // 日期时间处理库

export default {
  name: 'bookwithuser',
  created() {
    // 从 sessionStorage 获取用户信息并解析
    let userStr = sessionStorage.getItem("user") || "{}"
    this.user = JSON.parse(userStr)
    this.load()  // 页面创建时加载数据
  },
  methods: {
    /**
     * 表格复选框选中变化事件处理
     * @param val - 选中的行数据数组
     */
    handleSelectionChange(val) {
      this.forms = val  // 存储选中的行数据
    },
    /**
     * 批量删除借阅记录
     * 1. 验证是否选中数据
     * 2. 逐条删除借阅记录
     * 3. 统一更新图书的借阅数量
     */
    //声明异步函数
    async deleteBatch() {
      if (!this.forms.length) {
        ElMessage.warning("请选择数据！")
        return
      }
      // 对每个选中项分别执行删除逻辑
      let successCount = 0;             // 成功删除数量
      let failCount = 0;                // 失败数量
      const total = this.forms.length;  // 总数量
      const bookUpdateMap = {};         // 记录每本书需要减少的借阅数量

      for (const record of this.forms) {
        try {
          // 先删除记录，不更新book表
          await this.deleteSingleRecordWithoutBookUpdate(record);
          // 统计这本书需要减少多少借阅数量
          if (!bookUpdateMap[record.bookId]) {
            bookUpdateMap[record.bookId] = 0;// 初始化为0
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
          const decreaseCount = bookUpdateMap[bookId]; // 获取需要减少的数量
          const bookRes = await request.get(`/book/${bookId}`);//先查出这本书现在的库存
          if (bookRes.code === 0 || bookRes.code === '0') {
            const book = bookRes.data || {};
            //计算新库存：当前已借出 - 这次归还的总数
            const borrowedQuantity = Math.max((book.borrowedQuantity || 0) - decreaseCount, 0);
            //准备更新数据
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
      // 只有当所有Promise都成功，才会继续往下执行
      await Promise.all(updatePromises);

      if (failCount === 0) {
        ElMessage.success(`批量删除成功，共删除 ${successCount} 条记录`);
      } else {
        ElMessage.warning(`批量删除完成，成功 ${successCount} 条，失败 ${failCount} 条`);
      }
      this.load();
    },
    /**
     * 加载借阅记录数据
     * 管理员：可查看所有借阅记录
     * 普通用户：只能查看自己的借阅记录
     */
    load() {
      if (this.user.role == 1) {
        // 管理员查询所有借阅记录
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
        // 普通用户查询自己的借阅记录（search3设为用户名称）
        request.get("/bookwithuser", {
          params: {
            pageNum: this.currentPage,
            pageSize: this.pageSize,
            search1: this.search1,
            search2: this.search2,
            search3: this.user.nickName,
          }
        }).then(res => {
          console.log(res)
          this.tableData = res.data.records
          this.total = res.data.total
        })
      }
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
     * 删除单条借阅记录
     * @param row - 要删除的行数据
     */
    handleDelete(row) {
      this.deleteSingleRecord(row).then(() => {
        ElMessage.success("删除成功");
        this.load();
      }).catch(error => {
        ElMessage.error("删除失败");
        console.error("删除失败", error);
      });
    },
    /**
     * 删除单条借阅记录（包含更新图书信息）
     * 1. 删除LendRecord中的对应记录
     * 2. 删除bookwithuser记录
     * 3. 更新book表：借阅数量减1
     * @param row - 要删除的行数据
     * @returns Promise
     */
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
    /**
     * 删除单条借阅记录（不更新图书信息）
     * 用于批量删除时先删除记录，最后统一更新图书信息
     * 1. 删除LendRecord中的对应记录
     * 2. 删除bookwithuser记录
     * @param row - 要删除的行数据
     * @returns Promise
     */
    deleteSingleRecordWithoutBookUpdate(row) {
      return new Promise((resolve, reject) => {
          // resolve：成功时调用
          // reject：失败时调用
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
    /**
     * 续借图书处理
     * 1. 将最迟归还日期延长30天
     * 2. 可续借次数减1
     * @param row - 借阅记录行数据
     */
    handleProlong(row) {
      // 计算延期后的日期（当前归还日期 + 30天）
      const dead = new Date(row.deadTime);
      dead.setDate(dead.getDate() + 30);
      // ...row 复制 row 对象的所有属性
      const updated = {
        ...row,
        deadTime: moment(dead).format("yyyy-MM-DD HH:mm:ss"),
        prolong: row.prolong - 1,  // 续借次数减1
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
    /**
     * 还书处理
     * 1. 更新图书表：借阅数量减1，状态改为可借
     * 2. 更新借阅历史为已归还
     * 3. 删除当前借阅记录
     * @param row - 借阅记录行数据
     */
    handleReturn(row) {
      // 1) 查询图书详情，计算借出数量-1并更新状态
      request.get(`/book/${row.bookId}`).then(bookRes => {
        if (bookRes.code !== 0 && bookRes.code !== '0') {
          ElMessage.error(bookRes.msg || '获取图书信息失败')
          return
        }
        // 拿到书的详细信息对象
        const book = bookRes.data || {}
        // 已借出数量减1，Math.max确保不小于0
        const borrowedQuantity = Math.max((book.borrowedQuantity || 0) - 1, 0)
        // 更新后的已借出量
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
          // 2) 更新借阅历史为已归还（lendrecord）
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
    /**
     * 保存借阅信息修改
     * 验证表单数据后提交更新
     */
    save() {
      // 1. 检查“可续借次数”是否为空
      if (this.form.prolong === undefined || this.form.prolong === null) {
        ElMessage.error("请填写可续借次数");
        return;// 如果没填，报错并打断，不往下执行
      }
      // 2. 检查“最迟归还日期”是否为空
      if (!this.form.deadTime) {
        ElMessage.error("请填写最迟归还日期");
        return;// 没填就打断
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

    /**
     * 打开修改借阅信息对话框
     * @param row - 要修改的行数据
     */
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row))  // 深拷贝避免直接修改表格数据
      this.dialogVisible2 = true
    },
    /**
     * 分页大小变化事件处理
     * @param pageSize - 新的每页显示数量
     */
    handleSizeChange(pageSize) {
      this.pageSize = pageSize
      this.load()
    },
    /**
     * 当前页码变化事件处理
     * @param pageNum - 新的页码
     */
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
      this.load()
    },

  },
  data() {
    return {
      form: {},         // 表单数据：用于修改借阅信息
      form2: {},        // 预留表单数据2
      form3: {},        // 预留表单数据3
      dialogVisible: false,   // 对话框显示状态（预留）
      dialogVisible2: false,  // 修改借阅信息对话框显示状态
      search1: '',      // 搜索条件1：图书编号
      search2: '',      // 搜索条件2：图书名称
      search3: '',      // 搜索条件3：借阅者（管理员用）或用户名称（普通用户用）
      total: 10,        // 数据总条数
      currentPage: 1,   // 当前页码
      pageSize: 10,     // 每页显示数量
      tableData: [],    // 表格数据列表
      user: {},         // 当前登录用户信息
      forms: [],        // 批量选中的行数据
    }
  },
}
</script>
