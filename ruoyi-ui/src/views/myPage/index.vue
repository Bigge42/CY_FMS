<template>
  <div class="iframe-container">
    <iframe :src="src" frameborder="0" class="iframe-content"></iframe>
  </div>
</template>

<script>
export default {
  name: 'MyPage',
  data() {
    return {
      src: '/myPage/index.html'  // 设置 iframe 指向您在 public 中的 index.html 文件路径
    }
  }
}
</script>

<style scoped>
.iframe-container {
  width: 100%;
  height: calc(100vh - 50px);  /* 设置高度，确保页面能够完全显示 */
}
.iframe-content {
  width: 100%;
  height: 100%;
}
</style>
