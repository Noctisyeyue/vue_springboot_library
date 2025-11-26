<template>
  <div
      class="ValidCode disabled-select"
      :style="`width:${width}; height:${height}`"
      @click="refreshCode"
  >
    <span
        v-for="(item, index) in codeList"
        :key="index"
        :style="getStyle(item)"
    >{{ item.code }}</span>
  </div>
</template>

<script>
export default {
  name: 'ValidCode',
  model: {
    prop: 'value',
    event: 'input'
  },
  props: { //外部可传参数
    width: {
      type: String,
      default: '100px'
    },
    height: {
      type: String,
      default: '40px'
    },
    length: {
      type: Number,
      default: 4   // 要生成几位验证码
    },
    refresh: {   // 外部触发刷新用
      type: Number
    }
  },
  data () {
    return {
      codeList: []
    }
  },
  watch: {
    refresh () {
      this.createdCode() // 当父组件的 refresh 变化时也重新生成
    }
  },
  mounted () {  //组件第一次挂载时生成验证码
    this.createdCode()
  },
  methods: {
    refreshCode () {
      this.createdCode()
    },
    createdCode () {
      const len = this.length        // 要几位验证码，默认 4
      const codeList = []            // 用来装 4 个“带样式的字符对象”
      const chars = 'ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz0123456789'
      const charsLen = chars.length  // 62 个可选字符
      // 生成
      for (let i = 0; i < len; i++) {
        /* 1. 随机颜色（偏淡，防止太白看不清） */
        const rgb = [
          Math.round(Math.random() * 220), 
        Math.round(Math.random() * 240), 
        Math.round(Math.random() * 200)]
         /* 2. 拼一个“字符+样式”对象 */
        codeList.push({
          code: chars.charAt(Math.floor(Math.random() * charsLen)), // 随机字符
          color: `rgb(${rgb})`,                                     // 随机颜色
          fontSize: `${16 + Math.floor(Math.random() * 10)}px`,     // 16~25 px
          padding: `${[Math.floor(Math.random() * 10)]}px`,               // 0~9 px 内边距
          transform: `rotate(${Math.floor(Math.random() * 90) - Math.floor(Math.random() * 90)}deg)`   // -90~90° 旋转
        })
      }
      // 指向
      this.codeList = codeList   // 本地保存
      // 将当前数据派发出去
      // console.log(codeList.map(item => item.code).join(''))
      this.$emit('input',  //发射一个名为 input 的事件
      codeList.map(item => item.code).join(''))   // 把纯字符串发出去给父组件   join('')把数组无缝拼成字符串 
    },
    getStyle (data) {
      return `color: ${data.color}; font-size: ${data.fontSize}; padding: ${data.padding}; transform: ${data.transform}`
    }
  }
}
</script>

<style scoped>
.ValidCode{
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
}
.ValidCode span{
  display: inline-block;
}
</style>
