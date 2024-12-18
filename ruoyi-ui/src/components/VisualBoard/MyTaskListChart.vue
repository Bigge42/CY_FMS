<template>
  <div class="task-list-wrapper">
    <div class="title-container">
      <div class="title-texts">
        <div class="title-small"><img src="@/assets/visualboard/stats.png" alt="任务图标" class="title-icon" />当日任务清单</div>
        <div class="title-large">当日任务: {{ totalTasks }}</div>
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

export default {
  name: 'MyTaskListChart',
  props: {
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
  mounted() {
    this.initCharts();
  },
  methods: {
    initCharts() {
      const otherColor = '#32a487'; // 其他部分的绿色
      this.renderChart('chart-waiting', this.waitingTasks, '#5aa5e8', '待装', otherColor); // 待装为蓝色
      this.renderChart('chart-abnormal', this.abnormalTasks, '#ad3a3a', '异常', otherColor); // 异常为红色
      this.renderChart('chart-inserted', this.insertedTasks, '#FFD700', 'VIP', otherColor); // 插单为金色
    },
    renderChart(elementId, value, color, label, otherColor) {
      const chartDom = document.getElementById(elementId);
      const myChart = echarts.init(chartDom);

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
              position: 'center',  // 在中心显示
              formatter: `${label}\n${value}`, // 显示标签和数值
              color: '#ffffff',
              fontSize: 12,  // 缩小字体
              fontWeight: 'bold',
            },
            labelLine: {
              show: false, // 隐藏标签引线
            },
            data: [
              { value: value, name: label, itemStyle: { color: color } },
              { value: this.totalTasks - value, name: '其他', itemStyle: { color: otherColor } }, // 其他部分为绿色
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
  font-size: 26px; /* 大文本字体大小 */
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
