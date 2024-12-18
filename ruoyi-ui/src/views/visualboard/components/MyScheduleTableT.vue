<template>
  <div class="schedule-wrapper">
    <!-- 表格标题和第一个表格 -->
    <div class="table-container">
      <div class="table-title">
        <img src="../../../assets/visualboard/list-check.png" alt="图标" class="title-icon" />
        <h2>8:00-15:30排班</h2>
      </div>
      <div class="table-scroll" ref="firstTableWrapper">
        <table class="schedule-table">
          <thead>
            <tr>
              <th>计划号</th>
              <th>型号</th>
              <th>口径</th>
              <th>压力</th>
              <th>计划数</th>
              <th>状态</th>
              <th>工序</th>
              <th>特殊要求</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in firstHalf" :key="item.ERPPlanOrderId">
              <td>{{ item.ERPPlanOrderId }}</td>
              <td>{{ item.CPXH }}</td>
              <td>{{ item.gctj }}</td>
              <td>{{ item.gcyl }}</td>
              <td>{{ item.Quantity }}</td>
              <td>{{ item.abnormalState }}</td>
              <td>{{ item.OperationName }}</td>
              <td>{{ item.TSYQ }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 表格标题和第二个表格 -->
    <div class="table-container">
      <div class="table-title">
        <img src="../../../assets/visualboard/list-check.png" alt="图标" class="title-icon" />
        <h2>15:00-22:30排班</h2>
      </div>
      <div class="table-scroll" ref="secondTableWrapper">
        <table class="schedule-table">
          <thead>
            <tr>
              <th>计划号</th>
              <th>型号</th>
              <th>口径</th>
              <th>压力</th>
              <th>计划数</th>
              <th>状态</th>
              <th>工序</th>
              <th>特殊要求</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in secondHalf" :key="item.ERPPlanOrderId">
              <td>{{ item.ERPPlanOrderId }}</td>
              <td>{{ item.CPXH }}</td>
              <td>{{ item.gctj }}</td>
              <td>{{ item.gcyl }}</td>
              <td>{{ item.Quantity }}</td>
              <td>{{ item.abnormalState }}</td>
              <td>{{ item.OperationName }}</td>
              <td>{{ item.TSYQ }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
import { loadVisualData } from '@/utils/loadVisualData';

export default {
  name: 'MyScheduleTable',
  props: {
    type: {
      type: String,
      default: 'ZZCJCJDP', // 默认值为 'ZZCJCJDP'
    },
  },
  data() {
    return {
      GXMX: [],
      firstScrollInterval: null,
      secondScrollInterval: null,
    };
  },
  computed: {
    firstHalf() {
      return this.GXMX.slice(0, Math.ceil(this.GXMX.length / 2));
    },
    secondHalf() {
      return this.GXMX.slice(Math.ceil(this.GXMX.length / 2));
    },
  },
  async mounted() {
    await this.loadData();
    this.startAutoScroll();
  },
  methods: {
    async loadData() {
      try {
        const type = this.type;
        const data = await loadVisualData(type);
        if (data && Array.isArray(data.GXMX)) {
          this.GXMX = data.GXMX;
        } else {
          console.error('Invalid GXMX data:', data.GXMX);
        }
      } catch (error) {
        console.error('Error loading GXMX data:', error);
      }
    },
    startAutoScroll() {
      // 自动滚动第一个表格
      const firstTableWrapper = this.$refs.firstTableWrapper;
      if (firstTableWrapper) {
        this.firstScrollInterval = setInterval(() => {
          firstTableWrapper.scrollTop += 1;
          if (firstTableWrapper.scrollTop >= firstTableWrapper.scrollHeight - firstTableWrapper.clientHeight) {
            firstTableWrapper.scrollTop = 0;
          }
        }, 50); // 每50毫秒滚动1像素，实现平滑滚动效果
      }

      // 自动滚动第二个表格
      const secondTableWrapper = this.$refs.secondTableWrapper;
      if (secondTableWrapper) {
        this.secondScrollInterval = setInterval(() => {
          secondTableWrapper.scrollTop += 1;
          if (secondTableWrapper.scrollTop >= secondTableWrapper.scrollHeight - secondTableWrapper.clientHeight) {
            secondTableWrapper.scrollTop = 0;
          }
        }, 50); // 每50毫秒滚动1像素，实现平滑滚动效果
      }
    },
  },
  beforeDestroy() {
    if (this.firstScrollInterval) {
      clearInterval(this.firstScrollInterval);
    }
    if (this.secondScrollInterval) {
      clearInterval(this.secondScrollInterval);
    }
  },
};
</script>

<style scoped>
.schedule-wrapper {
  background-color: rgba(10, 31, 68, 0.5); /* 半透明背景 */
  backdrop-filter: blur(10px); /* 毛玻璃效果 */
  padding: 10px;
  width: 100%;
  height: 100%;
  margin: -20px auto;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2); /* 轻微阴影 */
  display: block;
  position: relative;
}

.table-container {

  font-size: 12px;
}

.table-title {
  display: flex;
  align-items: center;
  color: #ccc;
}

.title-icon {
  width: 14px; /* 图标大小缩小 */
  height: 14px;
  margin-right: 10px;
}

.table-scroll {
  max-height: 170px; /* 限制表格的高度 */
  overflow: hidden;
}

.schedule-table {
  width: 100%; /* 适应组件宽度 */
  border-collapse: collapse;
  color: #ccc;
  position: relative;
}

.schedule-table thead th {
  position: sticky;
  top: 0;
  background-color: rgba(10, 31, 68,1); /* 让表头固定，背景色加深 */
  z-index: 1;
}

.schedule-table th,
.schedule-table td {
  padding: 7px;
  text-align: center;
  font-size: 8px;
  border: none; /* 隐藏除表头以外的边框 */
}
</style>
