<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in cards" :key="item.title">
        <el-card class="box-card">
          <div slot="header" class="clearfix">{{ item.title }}</div>
          <div class="text item">
            <svg class="icon" aria-hidden="true">
              <use :xlink:href="item.icon" style="width: 100px"></use>
            </svg>
            <span class="text">{{ item.data }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <!-- 使用Vue的ref属性来引用DOM元素，避免直接使用id进行DOM操作 -->
    <div ref="myTimer" style="margin-left: 15px;font-weight: 550;"></div>
    <!-- 为 ECharts 准备一个具备大小（宽高）的 DOM -->
    <div id="main" style="margin-left: 5px"></div>
  </div>
</template>
 
<script>
import * as echarts from 'echarts'
import {ElMessage} from "element-plus";
import request from "../utils/request";

export default {
  data() {
    return {
      cards: [
        { title: '已借阅', data: 100, icon: '#iconlend-record-pro' },
        { title: '总访问', data: 100, icon: '#iconvisit'   },
        { title: '图书数', data: 100, icon: '#iconbook-pro' },
        { title: '用户数', data: 1000, icon: '#iconpopulation' }
      ],
      data:{}
    }
  },
  created() {

  },
  mounted() {
    this.circleTimer()

    request.get("/dashboard").then(res=>{
      if(res.code == 0)
      {

        this.cards[0].data = res.data.lendRecordCount
        this.cards[1].data = res.data.visitCount
        this.cards[2].data = res.data.bookCount
        this.cards[3].data = res.data.userCount

      }
      else
      {
        ElMessage.error(res.msg)
      }


      // 基于准备好的dom，初始化echarts实例
      var myChart = echarts.init(document.getElementById('main'))
    console.log(this.cards[0].data)
      // 绘制图表
      myChart.setOption({
        title: {
          text: '统计'
        },
        tooltip: {
          trigger: 'axis'
          // axisPointer: {
          //   type: 'shadow' // 默认为直线，可选为：'line' | 'shadow'
          // }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: this.cards.map(item => item.title),
          axisTick: {
            alignWithLabel: true
          }
        },
        yAxis: {
          type: 'value'
        },
        series: [
          {
            type: 'bar',
            label: { show: true },
            barWidth: '25%',
            data: [
              {
                value: this.cards[0].data,
                itemStyle: { color: '#5470c6' }
              },
              {
                value: this.cards[1].data,
                itemStyle: { color: '#91cc75' }
              },
              {
                value: this.cards[2].data,
                itemStyle: { color: '#fac858' }
              },
              {
                value: this.cards[3].data,
                itemStyle: { color: '#ee6666' }
              }
            ]
          }
        ]
      })
      window.addEventListener('resize', () => {
        myChart.resize()
      })
    })
  },
  methods: {
    circleTimer() {
      this.getTimer()
      setInterval(() => {
        this.getTimer()
      }, 1000)
    },
    getTimer() {
      //创建新的Date对象获取当前时间
      var d = new Date()
      //将时间转换为本地时间字符串格式
      var t = d.toLocaleString()

      //添加空值检查，防止DOM元素未渲染完成时访问导致的错误
      if (this.$refs.myTimer) {
        //设置元素的innerHTML来显示时间
        this.$refs.myTimer.innerHTML = t
      }
      // 如果元素不存在（比如组件还未完全挂载），则跳过更新，避免报错
    }
  }
}
</script>

<style scoped>
.box-card {
   width: 80%;
  margin-bottom: 25px;
  margin-left: 10px;
}

.clearfix {
  text-align: center;
  font-size: 15px;
}

.text {
  text-align: center;
  font-size: 24px;
  font-weight: 700;
  vertical-align: super;
}

#main {
  width: 100%;
  height: 450px;
  margin-top: 20px;
}

.icon {
  width: 50px;
  height: 50px;
  padding-top: 5px;
  padding-right: 10px;
}
</style>