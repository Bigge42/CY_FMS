<template>
  <div class="attendance-wrapper">
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
import { loadVisualData } from '@/utils/loadVisualData';

export default {
  name: 'MyAttendanceInfo',
  props: {
    type: {
      type: String,
      required: true,
      default: 'ZZCJCJDP', // 默认值为 'ZZCJCJDP'
    },
  },
  data() {
    return {
      expectedAttendance: 0,
      actualAttendance: 0,
      attendanceRate: 0,
    };
  },
  mounted() {
  this.loadAttendanceData();
  // 每隔 30 分钟刷新数据
  setInterval(() => this.loadAttendanceData(), 30 * 60 * 1000);
  },
  methods: {
    async loadAttendanceData() {
      try {
        // 使用父组件传递的 type 字段，而不是硬编码
        const type = this.type;
        const data = await loadVisualData(type);
        if (data && Array.isArray(data.CQ) && data.CQ.length > 0) {
          const cqData = data.CQ[0];
          this.expectedAttendance = cqData.YCQ;
          this.actualAttendance = cqData.SCQ;
          this.attendanceRate = ((this.actualAttendance / this.expectedAttendance) * 100).toFixed(2);
        } else {
          console.error('Invalid CQ data:', data.CQ);
        }
      } catch (error) {
        console.error('Error loading attendance data:', error);
      }
    },
  },
};
</script>

<style scoped>
.attendance-wrapper {
  align-items: center;
  background-color: rgba(10, 31, 68, 0.5);
  backdrop-filter: blur(20px);
  padding: 20px;
  border-radius: 0px;
  width: 90%;
  margin: 20px auto;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
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
  background: linear-gradient(145deg, #1e3a5f, #22496b);
  border: 2px solid #58B3D6;
  border-radius: 10px;
  padding: 10px;
  margin: 10px;
  width: 33%;
  height: 80%;
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
  width: 14px;
  height: 14px;
}
</style>
