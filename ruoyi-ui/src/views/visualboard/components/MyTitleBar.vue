<template>
  <div class="title-bar">
    <div class="title-text"><img src="../../../assets/logo/logo.png" alt="列表图标" class="title-icon" />{{ titleText }}</div>
    <div class="title-time">{{ currentTime }}</div>
  </div>
</template>

<script>
export default {
  name: 'MyTitleBar',
  props: {
    type: {
      type: String,
      default: 'ZZCJCJDP', // 默认值为 'ZZCJCJDP'
    },
  },
  data() {
    return {
      currentTime: '',
    };
  },
  computed: {
    // 根据 type 动态设置标题文本
    titleText() {
      switch (this.type) {
        case 'XZFFJZPJY':
          return '旋转阀部件装配及检验看板';
        case 'ZTFFJZPJY':
          return '直通阀附件装配及检验看板';
        case 'XZFZPYS':
          return '旋转阀装配压水车间看板';
        case 'QFCJDP':
          return '球阀车间生成看板';
        default:
          return '直通阀车间生产看板'; // 默认标题
      }
    },
  },
  mounted() {
    this.updateTime();
    setInterval(this.updateTime, 1000); // 每秒更新时间
  },
  methods: {
    updateTime() {
      const now = new Date();
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, '0');
      const day = String(now.getDate()).padStart(2, '0');
      const hours = String(now.getHours()).padStart(2, '0');
      const minutes = String(now.getMinutes()).padStart(2, '0');
      const seconds = String(now.getSeconds()).padStart(2, '0');
      this.currentTime = `${year}年${month}月${day}日 ${hours}时${minutes}分${seconds}秒`;
    },
  },
};
</script>

<style scoped>
.title-bar {
  background-image: url('../../../assets/visualboard/head_bg.png'); /* 修正背景图片路径 */
  background-size: 100% 50%;
  background-repeat: no-repeat; /* 防止背景图片重复 */
  z-index: 1;
  display: flex;
  justify-content: space-between; /* 确保标题和时间分开 */
  align-items: center;
  padding: 20px;
  font-size: 18px;
  color: white;
  width: 1920px; /* 确保占据整个宽度 */
  height: 100%;
}
.title-icon{
  height: 43px;
  width: 70px;
}
.title-text {
  font-weight: bold;
  text-align: center; /* 确保文本居中 */
  font-size: 48px;
  flex-grow: 1; /* 让标题文本扩展以填充可用空间 */
  margin-top: -90px;
  margin-left: 200px;
}

.title-time {
  font-size: 16px;
  text-align: right; /* 确保时间在右侧对齐 */
  margin-top: -28px;
}
</style>
