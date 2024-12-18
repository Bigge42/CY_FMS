<template>
  <div class="equipment-status-wrapper">
    <div class="title-container">
      <img :src="databaseIcon" alt="图标" class="title-icon" />
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
            <img :src="getStatusIcon(status.isError)" alt="状态图标" class="status-icon" />
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
import { loadVisualData } from '@/utils/loadVisualData';
import pulseR from '@/assets/visualboard/pulse_r.png';
import pulseG from '@/assets/visualboard/pulse_g.png';
import databaseIcon from '@/assets/visualboard/database.png';

export default {
  name: 'MyEquipmentStatus',
  props: {
    type: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      equipmentStatus: [],
      currentPage: 0,
      pageSize: 4, // 每页显示的设备数量
      databaseIcon: databaseIcon,
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
  async mounted() {
    await this.loadEquipmentStatusData();
    this.autoPageChange();
  },
  methods: {
    // 加载设备状态数据
    async loadEquipmentStatusData() {
      try {
        const responseData = await loadVisualData(this.type);
        if (responseData && Array.isArray(responseData.SBZT)) {
          const sbztData = responseData.SBZT;
          this.equipmentStatus = sbztData.map(item => ({
            device: `${item.SBMC} (${item.SBDM})`,
            isError: item.SBZT === '异常',
          }));
        } else {
          console.error('Invalid equipment status data:', responseData);
        }
      } catch (error) {
        console.error('Error loading equipment status data:', error);
      }
    },
    // 自动翻页功能
    autoPageChange() {
      setInterval(() => {
        this.currentPage = (this.currentPage + 1) % Math.ceil(this.equipmentStatus.length / this.pageSize);
      }, 5000); // 每5秒自动翻页
    },
    // 获取设备状态的图标路径
    getStatusIcon(isError) {
      return isError ? pulseR : pulseG;
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
