<template>
  <div class="attendance-wrapper"> <!-- 外层大框，带毛玻璃效果 -->
    <div class="attendance-container">
      <div class="attendance-item">
        <img src="@/assets/visualboard/users-alt.png" alt="应到人数">
        <div class="text-container">
          <span class="small-text">应出勤</span>
          <span class="large-text">{{ expectedAttendance }} 人</span>
        </div>
      </div>
      <div class="attendance-item">
        <img src="@/assets/visualboard/users.png" alt="实到人数">
        <div class="text-container">
          <span class="small-text">实出勤</span>
          <span class="large-text">{{ actualAttendance }} 人</span>
        </div>
      </div>
      <div class="attendance-item">
        <img src="@/assets/visualboard/file-user.png" alt="出勤率">
        <div class="text-container">
          <span class="small-text">出勤率</span>
          <span class="large-text">{{ attendanceRate }}%</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MyAttendanceInfo',
  data() {
    return {
      expectedAttendance: 15,
      actualAttendance: 14,
      attendanceRate: 93,
    };
  },
};
</script>

<style scoped>
.attendance-wrapper {
  align-items: center;
  background-color: rgba(10, 31, 68, 0.5); /* 半透明背景 */
  backdrop-filter: blur(20px); /* 毛玻璃效果 */
  padding: 20px;
  border-radius: 0px;
  width: 80%; /* 根据需要调整宽度 */
  margin: 20px auto;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2); /* 轻微阴影 */
  height: 15%;
}

.attendance-container {
  display: flex;
  justify-content: space-around;
  height: 100%;
  align-items: center;
}

.attendance-item {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  color: #fff;
  background: linear-gradient(145deg, #1e3a5f, #22496b); /* 渐变背景 */
  border: 2px solid #58B3D6; /* 边框线条 */
  border-radius: 10px;
  padding: 10px;
  margin: 10px;
  width: 33%; /* 确保每个框占相同的宽度 */
  height: 60%; /* 固定高度，确保所有框高度一致 */
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.5);
}

.text-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  margin-left: 10px;
}

.small-text {
  color: white;
  font-size: 10px;
}

.large-text {
  color: #f1c40f;
  font-size: 12px;
  font-weight: bold;
}

.attendance-item img {
  width: 14px; /* 调整图片大小 */
  height: 14px;
}

</style>
