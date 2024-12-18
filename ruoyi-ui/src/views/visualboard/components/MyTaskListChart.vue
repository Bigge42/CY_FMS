<template>
  <div class="task-list-wrapper">
    <div class="title-container">
      <div class="title-texts">
        <div class="title-small"><img src="../../../assets/visualboard/stats.png" alt="任务图标" class="title-icon" />当日任务清单</div>
        <div class="title-large">当日任务: {{ localTotalTasks !== 0 ? localTotalTasks : totalTasks }}</div>
        <div class="title-large">累计总任务: {{ localLJZRW }}</div>
      </div>
    </div>
    <div class="task-charts">
      <div id="chart-waiting" class="chart"></div>
      <div id="chart-abnormal" class="chart"></div>
      <div id="chart-inserted" class="chart"></div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import { loadVisualData } from '@/utils/loadVisualData';

export default {
  name: 'MyTaskListChart',
  props: {
    type: {
      type: String,
      
    },
    totalTasks: {
      type: Number,
      default: 100, // 默认总任务数
    },
    waitingTasks: {
      type: Number,
      default: 30, // 待装任务数
    },
    abnormalTasks: {
      type: Number,
      default: 10, // 异常任务数
    },
    insertedTasks: {
      type: Number,
      default: 15, // 插单任务数
    },
  },
  data() {
    return {
      localTotalTasks: this.totalTasks,
      localWaitingTasks: this.waitingTasks,
      localAbnormalTasks: this.abnormalTasks,
      localInsertedTasks: this.insertedTasks,
      localLJZRW: 0, // 累计总任务数
    };
  },
  mounted() {
    this.loadData();
  },
  methods: {
    async loadData() {
      try {
        const type = this.type;
        const data = await loadVisualData(type);
        if (data && Array.isArray(data.DRRW) && data.DRRW.length > 0) {
          const drrwData = data.DRRW[0];
          this.localTotalTasks = drrwData.ZRW;
          this.localWaitingTasks = drrwData.DZ;
          this.localAbnormalTasks = drrwData.YC;
          this.localInsertedTasks = drrwData.CD;
          this.localLJZRW = drrwData.LJZRW; // 设置累计总任务数
          this.initCharts();
        } else {
          console.error('Invalid DRRW data:', data.DRRW);
          this.initCharts();
        }
      } catch (error) {
        console.error('Error loading DRRW data:', error);
        this.initCharts();
      }
    },
    initCharts() {
      const otherColor = '#32a487'; // 其他部分的绿色
      this.renderChart(
        'chart-waiting',
        this.localWaitingTasks !== 0 ? this.localWaitingTasks : this.waitingTasks,
        '#5aa5e8',
        '待装',
        otherColor
      ); // 待装为蓝色
      this.renderChart(
        'chart-abnormal',
        this.localAbnormalTasks !== 0 ? this.localAbnormalTasks : this.abnormalTasks,
        '#ad3a3a',
        '异常',
        otherColor
      ); // 异常为红色
      this.renderChart(
        'chart-inserted',
        this.localInsertedTasks !== 0 ? this.localInsertedTasks : this.insertedTasks,
        '#FFD700',
        'VIP',
        otherColor
      ); // 插单为金色
    },
    renderChart(elementId, value, color, label, otherColor) {
      const chartDom = document.getElementById(elementId);

      if (!chartDom) {
        console.error('Chart DOM element not found:', elementId);
        return;
      }

      // 检查是否已有图表实例并销毁它
      if (echarts.getInstanceByDom(chartDom)) {
        echarts.dispose(chartDom);
      }

      const myChart = echarts.init(chartDom);

      const otherValue = this.localLJZRW !== 0 ? this.localLJZRW - value : this.totalTasks - value; // 使用 LJZRW 减去当前值

      const option = {
        tooltip: {
          trigger: 'item',
        },
        series: [
          {
            name: label,
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['50%', '70%'],
            startAngle: 180,
            endAngle: 360,
            label: {
              position: 'center', // 在中心显示
              formatter: `${label}\n${value}`, // 显示标签和数值
              color: '#ffffff',
              fontSize: 12, // 缩小字体
              fontWeight: 'bold',
            },
            labelLine: {
              show: false, // 隐藏标签引线
            },
            data: [
              { value: value, name: label, itemStyle: { color: color } },
              { value: otherValue, name: '其他', itemStyle: { color: otherColor } } // 其他部分为绿色
            ],
          },
        ],
      };

      myChart.setOption(option);
    },
  },
};
</script>

<style scoped>
.task-list-wrapper {
  background-color: rgba(10, 31, 68, 0.5); /* 半透明背景 */
  backdrop-filter: blur(10px); /* 毛玻璃效果 */
  padding: 10px;
  width: 100%;
  height: 90px;
  margin: 20px auto;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2); /* 轻微阴影 */
  display: flex;
}

.title-container {
  display: flex;
  align-items: center;
}

.title-icon {
  margin-right: 10px;
  width: 14px; /* 缩小图标大小 */
  height: 14px;
}

.title-texts {
  display: flex;
  flex-direction: column;
}

.title-small {
  font-size: 14px; /* 小文本字体大小 */
  color: white;
}

.title-large {
  font-size: 16px; /* 大文本字体大小 */
  margin: 10px;
  color: #e6c545;
  font-weight: bold;
}

.task-charts {
  display: flex;
  justify-content: space-around;
  justify-content: flex-end; /* 让图表靠右对齐 */
  flex-grow: 1; /* 让图表区域占满剩余空间 */
}

.chart {
  width: 100px;
  height: 100px;
}
</style>
