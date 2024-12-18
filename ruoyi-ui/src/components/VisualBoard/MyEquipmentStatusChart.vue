<template>
  <div class="equipment-status-wrapper">
    <div class="title-container">
      <img src="@/assets/visualboard/database.png" alt="图标" class="title-icon" />
      <span class="chart-title">设备状态</span>
    </div>
    <table class="status-table">
      <thead>
        <tr>
          <th>设备</th>
          <th>状态</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(status, index) in paginatedEquipmentStatus" :key="index">
          <td>{{ status.device }}</td>
          <td>
            <img :src="status.isError ? require('@/assets/visualboard/pulse_r.png') : require('@/assets/visualboard/pulse_g.png')" alt="状态图标" class="status-icon" />
            <span>{{ status.isError ? '异常' : '正常' }}</span>
          </td>
        </tr>
      </tbody>
    </table>
    <div class="progress-bar-container">
      <div class="progress-bar" :style="{ width: progressWidth + '%' }"></div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MyEquipmentStatus',
  data() {
    return {
      equipmentStatus: [
        { device: "设备1", isError: false },
        { device: "设备2", isError: false },
        { device: "设备3", isError: false },
        { device: "设备4", isError: false },
        { device: "设备5", isError: false },
        { device: "设备6", isError: false },
        { device: "设备7", isError: true },
        { device: "设备8", isError: false },
        { device: "设备9", isError: true },
        { device: "设备10", isError: false },
        { device: "设备11", isError: false },
        { device: "设备12", isError: false },
        { device: "设备13", isError: false },
        { device: "设备14", isError: false },
        { device: "设备15", isError: false },
        { device: "设备16", isError: false },
        { device: "设备17", isError: false },
        { device: "设备18", isError: true },
        { device: "设备19", isError: false },
        { device: "设备20", isError: false },
        { device: "设备21", isError: false },
        { device: "设备22", isError: false },
        { device: "设备23", isError: false },
        { device: "设备24", isError: false },
        { device: "设备25", isError: false },
        { device: "设备26", isError: true },
        { device: "设备27", isError: false },
        { device: "设备28", isError: false },
        { device: "设备29", isError: false },
        { device: "设备30", isError: false }
      ],
      currentPage: 0,
      pageSize: 8, // 每页显示的设备数量
    };
  },
  computed: {
    // 分页后当前页显示的数据
    paginatedEquipmentStatus() {
      const start = this.currentPage * this.pageSize;
      const end = start + this.pageSize;
      return this.equipmentStatus.slice(start, end);
    },
    // 计算当前进度条的宽度
    progressWidth() {
      return ((this.currentPage + 1) / Math.ceil(this.equipmentStatus.length / this.pageSize)) * 100;
    }
  },
  mounted() {
    this.autoPageChange();
  },
  methods: {
    // 自动翻页功能
    autoPageChange() {
      setInterval(() => {
        this.currentPage = (this.currentPage + 1) % Math.ceil(this.equipmentStatus.length / this.pageSize);
      }, 5000); // 每5秒自动翻页
    }
  }
};
</script>

<style scoped>
.equipment-status-wrapper {
  background-color: rgba(10, 31, 68, 0.5); /* 半透明背景 */
  backdrop-filter: blur(10px); /* 毛玻璃效果 */
  padding: 10px; /* 调整 padding 为缩小比例 */
  width: 90%; /* 宽度缩小 */
  margin: 10px auto; /* 调整 margin */
  height: 30%;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2); /* 轻微阴影 */
}

.title-container {
  display: flex;
  align-items: center;
  margin-bottom: 5px; /* 缩小 */
}

.title-icon {
  width: 14px; /* 缩小图标 */
  height: 14px;
  margin-right: 5px;
}

.chart-title {
  font-size: 12px; /* 缩小字体 */
  color: #ccc;
}

.status-table {
  width: 100%;
  border-collapse: collapse;
  color: #ccc;
  font-size: 10px;
}

.status-table th,
.status-table td {
  padding: 5px; /* 调整 padding */
  border: 1px solid #3a3a3a;
  text-align: left;
}

.status-table th {
  background-color: #2a2a2a;
}

.status-icon {
  width: 14px; /* 缩小状态图标 */
  height: 14px;
  margin-right: 5px;
}

.progress-bar-container {
  width: 100%;
  background-color: #2a2a2a;
  height: 5px; /* 缩小进度条 */
  border-radius: 3px;
  margin-top: 10px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background-color: #43eec6;
  width: 0;
  transition: width 0.5s ease; /* 平滑过渡效果 */
}
</style>
