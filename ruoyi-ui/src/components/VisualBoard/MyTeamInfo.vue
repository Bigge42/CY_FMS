<template>
  <div class="team-info-wrapper"> <!-- 外层大框 -->
    <div class="first-row">
      <div class="team-info-item">
        <img src="@/assets/visualboard/globe-user.png" alt="班组" class="info-icon">
        <span class="info-label">班组：</span>
        <span class="info-text">{{ teamName }}</span>
      </div>
    </div>
    <div class="second-row">
      <div class="team-info-item">
        <img src="@/assets/visualboard/employee-man-alt.png" alt="班组长" class="info-icon">
        <span class="info-label">班组长：</span>
        <span class="info-text">{{ teamLeader }}</span>
      </div>
      <div class="team-info-item">
        <img src="@/assets/visualboard/phone-call.png" alt="电话" class="info-icon">
        <span class="info-label">电话：</span>
        <span class="info-text">{{ teamPhone }}</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MyTeamInfo',
  data() {
    return {
      teamName: '调节阀装配班组',
      teamLeader: '万军',
      teamPhone: '15025341025',
    };
  },
};
</script>

<style scoped>
.team-info-wrapper {
  background-color: rgba(10, 31, 68, 0.5); /* 半透明背景 */
  backdrop-filter: blur(10px); /* 毛玻璃效果 */
  padding: 20px;
  width: 90%; /* 根据需要调整宽度 */
  margin: 20px auto;
  height: 100px; /* 适当调整高度以适应两排 */
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2); /* 轻微阴影 */
}

.first-row, .second-row {
  display: flex;
  justify-content: space-between; /* 让第一排的两个框均匀分布 */
}

.first-row .team-info-item {
  width: 100%; /* 班组信息占满整个第一排 */
}

.team-info-item {
  display: flex;
  align-items: center;
  color: #fff;
  background: linear-gradient(145deg, #1e3a5f, #22496b); /* 渐变背景 */
  border: 2px solid #58B3D6; /* 边框线条 */
  border-radius: 10px;
  padding: 10px;
  margin: 3px;
  width: 48%; /* 第二排的两个信息框宽度各占50% */
  justify-content: flex-start;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.5);
}

.info-icon {
  width: 14px; /* 缩小图标 */
  height: 14px;
  margin-right: 10px;
}

.info-label {
  font-size: 10px; /* 小文本 */
  color: white;
}

.info-text {
  font-size: 12px; /* 大文本 */
  font-weight: bold;
  color: #f1c40f;
  margin-left: 5px;
}
</style>
