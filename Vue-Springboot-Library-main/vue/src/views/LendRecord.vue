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
        <el-form-item label="读者编号" v-if="user.role == 1">
          <el-input v-model="search3" placeholder="请输入读者编号" clearable>
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

    <!--按钮-->
    <div style="margin: 10px 0;" v-if="user.role == 1">
      <el-popconfirm title="确认删除?" @confirm="deleteBatch">
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
        <el-table-column v-if="user.role == 1" prop="readerId" label="读者编号" sortable />
        <el-table-column v-if="user.role == 1" prop="nickName" label="借阅者" />
        <el-table-column prop="lendTime" label="借阅时间" sortable />
        <el-table-column prop="returnTime" label="归还时间" sortable />
        <el-table-column prop="status" label="状态">
          <template v-slot="scope">
            <el-tag v-if="scope.row.status == 0" type="warning">未归还</el-tag>
            <el-tag v-else type="success">已归还</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="user.role === 1" label="操作" width="230px">
          <template v-slot="scope">
            <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
            <el-popconfirm title="确认删除?" @confirm="handleDelete(scope.row)">
              <template #reference>
                <el-button type="danger" size="mini">删除</el-button>
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


      <el-dialog v-model="dialogVisible" title="修改借阅记录" width="30%">
        <el-form :model="form" label-width="120px">
          <el-form-item label="借阅时间">
            <el-input v-model="form.lendTime" disabled></el-input>
          </el-form-item>
          <el-form-item label="归还时间">
            <el-date-picker v-model="form.returnTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss">
            </el-date-picker>
          </el-form-item>
          <el-form-item label="是否归还" prop="status">
            <el-radio v-model="form.status" label="0" :disabled="form.originalStatus === '1'">未归还</el-radio>
            <el-radio v-model="form.status" label="1" :disabled="form.originalStatus === '1'">已归还</el-radio>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible = false">取 消</el-button>
            <el-button type="primary" @click="update">确 定</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </div>

</template>

<script>

import request from "../utils/request";
import { ElMessage } from "element-plus";
import { defineComponent, reactive, toRefs } from 'vue'

export default defineComponent({

  created() {
    const userStr = sessionStorage.getItem("user") || "{}"
    this.user = JSON.parse(userStr)
    // 普通用户默认只看自己的借阅记录
    if (this.user && this.user.role === 2) {
      this.search3 = this.user.id
    }
    this.load()
  },
  name: 'LendRecord',
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
      
      for (const record of this.forms) {
        try {
          await this.deleteSingleRecord(record);
          successCount++;
        } catch (error) {
          failCount++;
          console.error("删除记录失败", error);
        }
      }
      
      if (failCount === 0) {
        ElMessage.success(`批量删除成功，共删除 ${successCount} 条记录`);
      } else {
        ElMessage.warning(`批量删除完成，成功 ${successCount} 条，失败 ${failCount} 条`);
      }
      this.load();
    },
    load() {
      const params = {
        pageNum: this.currentPage,
        pageSize: this.pageSize,
        search1: this.search1,
        search2: this.search2,
        search3: this.search3
      }
      // 后端按读者编号过滤，普通用户强制使用自己的ID
      if (this.user.role !== 1) {
        params.search3 = this.user.id
      }
      request.get("/LendRecord", { params }).then(res => {
        console.log(res)
        this.tableData = res.data.records
        this.total = res.data.total
      })
    },
    update() {
      if (!this.form.readerId || !this.form.bookId) {
        ElMessage.error("缺少必要的字段信息");
        return;
      }

      const originalStatus = this.form.originalStatus || '0';
      const newStatus = this.form.status || '0';
      const hasReturnTime = this.form.returnTime && this.form.returnTime.trim() !== '';

      // 判断逻辑1：如果编辑之前状态为未归还
      if (originalStatus === '0') {
        // 如果状态没有改为已归还，但填写了归还日期
        if (newStatus === '0' && hasReturnTime) {
          ElMessage.warning("状态为未归还时不能填写归还日期，请重新填写编辑信息");
          return;
        }
        // 如果状态改为已归还，必须填写归还日期
        if (newStatus === '1' && !hasReturnTime) {
          ElMessage.warning("状态改为已归还时，必须填写归还日期");
          return;
        }
      }

      // 判断逻辑2：如果编辑之前状态是已归还，不能修改状态
      if (originalStatus === '1' && newStatus !== '1') {
        ElMessage.warning("已归还的记录不能修改状态，只能修改归还日期");
        return;
      }

      // 使用原始的 lendTime 作为定位条件
      const updateData = {
        readerId: this.form.readerId,
        bookId: this.form.bookId,
        lendTime: this.form.originalLendTime || this.form.lendTime,
        returnTime: this.form.returnTime,
        status: this.form.status
      };

      // 如果从未归还改为已归还，需要执行还书逻辑
      if (originalStatus === '0' && newStatus === '1' && hasReturnTime) {
        this.updateWithReturnLogic(updateData);
      } else {
        // 普通更新
        this.updateRecord(updateData);
      }
    },
    updateRecord(updateData) {
      request.put("/LendRecord", updateData).then(res => {
        console.log(res)
        if (res.code == 0 || res.code === '0') {
          ElMessage({
            message: '更新成功',
            type: 'success',
          })
          this.load()
          this.dialogVisible = false
        } else {
          ElMessage.error(res.msg || '更新失败')
        }
      }).catch(error => {
        console.error("更新失败", error);
        ElMessage.error("更新失败");
      })
    },
    updateWithReturnLogic(updateData) {
      // 1) 查询图书详情，计算借出数量-1并更新状态
      request.get(`/book/${updateData.bookId}`).then(bookRes => {
        if (bookRes.code !== 0 && bookRes.code !== '0') {
          ElMessage.error(bookRes.msg || '获取图书信息失败')
          return
        }
        const book = bookRes.data || {}
        const borrowedQuantity = Math.max((book.borrowedQuantity || 0) - 1, 0)
        const bookUpdate = {
          id: updateData.bookId,
          borrowedQuantity,
          status: '1'
        }
        
        request.put("/book", bookUpdate).then(updateRes => {
          if (updateRes.code !== 0 && updateRes.code !== '0') {
            ElMessage.error(updateRes.msg || '更新图书状态失败')
            return
          }
          
          // 2) 更新借阅历史为已归还
          request.put("/LendRecord", updateData).then(() => {
            // 3) 删除当前借阅记录（bookwithuser）
            const payload = {
              bookId: updateData.bookId,
              readerId: updateData.readerId,
              lendTime: updateData.lendTime
            }
            
            request.post("bookwithuser/deleteRecord", payload).then(res => {
              if (res.code == 0 || res.code === '0') {
                ElMessage.success("更新成功")
                this.load()
                this.dialogVisible = false
              } else {
                ElMessage.error(res.msg || '删除借阅记录失败')
              }
            }).catch(error => {
              console.error("删除借阅记录失败", error);
              ElMessage.error("删除借阅记录失败");
            })
          }).catch(error => {
            console.error("更新借阅历史失败", error);
            ElMessage.error("更新借阅历史失败");
          })
        }).catch(error => {
          console.error("更新图书信息失败", error);
          ElMessage.error("更新图书信息失败");
        })
      }).catch(error => {
        console.error("获取图书信息失败", error);
        ElMessage.error("获取图书信息失败");
      })
    },
    clear() {
      this.search1 = ""
      this.search2 = ""
      this.search3 = ""
      this.load()
    },
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row))
      // 保存原始的 lendTime 和 status 用于定位记录和判断逻辑
      this.form.originalLendTime = row.lendTime
      this.form.originalStatus = row.status || '0'
      this.dialogVisible = true
    },
    handleSizeChange(pageSize) {
      this.pageSize = pageSize
      this.load()
    },
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
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
        const status = row.status || '0';
        
        // 如果状态是未归还，需要删除bookwithuser记录并更新book表
        if (status === '0' || status === 0) {
          // 1) 删除bookwithuser中的记录
          const bookWithUserPayload = {
            bookId: row.bookId,
            readerId: row.readerId,
            lendTime: row.lendTime
          };
          
          request.post("bookwithuser/deleteRecord", bookWithUserPayload).then(() => {
            // 2) 更新book表：可借阅数量加一
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
                  // 3) 删除LendRecord记录
                  request.delete("/LendRecord", { data: row }).then(res => {
                    if (res.code == 0 || res.code === '0') {
                      resolve();
                    } else {
                      reject(new Error(res.msg || '删除失败'));
                    }
                  }).catch(error => {
                    reject(error);
                  });
                }).catch(error => {
                  console.error("更新图书信息失败", error);
                  // 即使更新失败，也继续删除LendRecord
                  request.delete("/LendRecord", { data: row }).then(res => {
                    if (res.code == 0 || res.code === '0') {
                      resolve();
                    } else {
                      reject(new Error(res.msg || '删除失败'));
                    }
                  }).catch(err => {
                    reject(err);
                  });
                });
              } else {
                // 获取图书信息失败，直接删除LendRecord
                request.delete("/LendRecord", { data: row }).then(res => {
                  if (res.code == 0 || res.code === '0') {
                    resolve();
                  } else {
                    reject(new Error(res.msg || '删除失败'));
                  }
                }).catch(err => {
                  reject(err);
                });
              }
            }).catch(error => {
              console.error("获取图书信息失败", error);
              // 获取失败，直接删除LendRecord
              request.delete("/LendRecord", { data: row }).then(res => {
                if (res.code == 0 || res.code === '0') {
                  resolve();
                } else {
                  reject(new Error(res.msg || '删除失败'));
                }
              }).catch(err => {
                reject(err);
              });
            });
          }).catch(error => {
            console.error("删除借阅记录失败", error);
            // 删除bookwithuser失败，也尝试删除LendRecord
            request.delete("/LendRecord", { data: row }).then(res => {
              if (res.code == 0 || res.code === '0') {
                resolve();
              } else {
                reject(new Error(res.msg || '删除失败'));
              }
            }).catch(err => {
              reject(err);
            });
          });
        } else {
          // 如果状态是已归还，直接删除LendRecord记录
          request.delete("/LendRecord", { data: row }).then(res => {
            if (res.code == 0 || res.code === '0') {
              resolve();
            } else {
              reject(new Error(res.msg || '删除失败'));
            }
          }).catch(error => {
            reject(error);
          });
        }
      });
    },
    add() {
      this.dialogVisible2 = true
      this.form = {}
    }
  },

  setup() {
  const state = reactive({
    shortcuts: [
      {
        text: 'Today',
        value: new Date(),
      },
      {
        text: 'Yesterday',
        value: () => {
          const date = new Date()
          date.setTime(date.getTime() - 3600 * 1000 * 24)
          return date
        },
      },
      {
        text: 'A week ago',
        value: () => {
          const date = new Date()
          date.setTime(date.getTime() - 3600 * 1000 * 24 * 7)
          return date
        },
      },
    ],
    value1: '',
    value2: '',
    value3: '',
    defaultTime: new Date(2000, 1, 1, 12, 0, 0), // '12:00:00'
  })

    return toRefs(state)
},
  data() {
  return {
    form: {},
    search1: '',
    search2: '',
    search3: '',
    total: 10,
    currentPage: 1,
    pageSize: 10,
    tableData: [],
    user: {},
    dialogVisible: false,
    dialogVisible2: false

  }
},

})
</script>
