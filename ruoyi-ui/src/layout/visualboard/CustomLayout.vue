<template>
  <div id="layout-wrapper">
    <header class="header" v-if="showHeader">
      <!-- 头部区域，可以放置 Logo 和导航 -->
      <div class="logo">数字大屏系统</div>
      <nav class="nav">导航菜单</nav>
    </header>

    <main class="main-content">
      <slot></slot>
    </main>

    <footer class="footer" v-if="showFooter">
      <!-- 底部区域 -->
      <p>© 2024 数字大屏系统. 保留所有权利。</p>
    </footer>
  </div>
</template>

<script>
export default {
  name: 'CustomLayout',
  props: {
    showHeader: {
      type: Boolean,
      default: true
    },
    showFooter: {
      type: Boolean,
      default: true
    }
  }
};
</script>
