<template>
  <div class="bar-chart-wrapper"> <!-- 外层容器，应用统一样式 -->
    <div class="bar-chart">
      <div class="title-container">
        <img src="@/assets/visualboard/list.png" alt="列表图标" class="title-icon" />
        <span class="chart-title">任务执行进度</span>
      </div>
      <div ref="chart" class="chart-container"></div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';

export default {
  name: 'MyBarChart',
  mounted() {
    this.initChart();
    window.addEventListener('resize', this.resizeChart); // 添加窗口变化监听器
  },
  beforeUnmount() { // 这里使用 beforeUnmount 替换 beforeDestroy
    window.removeEventListener('resize', this.resizeChart); // 组件销毁时移除监听器
  },
  methods: {
    initChart() {
      this.chart = echarts.init(this.$refs.chart, 'walden'); // 使用全局注册的 walden 主题

      let option = {
        title: {
          text: '',
          textStyle: {
            color: '#ccc'  // 标题文本颜色为 #ccc
          }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow' // 默认为直线，可选为：'line' | 'shadow'
          }
        },
        grid: {
          left: '3%',
          right: '5%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'value',
          axisLabel: {
            color: '#ccc',  // X 轴文本颜色为 #ccc
            formatter: '{value}%'   // 格式化为百分比
          }
        },
        yAxis: {
          type: 'category',
          axisLabel: {
            color: '#ccc'  // Y 轴文本颜色为 #ccc
          },
          data: ['配套进度', '完成进度', '计划需求']
        },
        series: [
          {
            name: '进度',
            type: 'bar',
            data: [85, 60, 100],  // 根据实际数据调整
            itemStyle: {
              color: function(params) {
                // 每根柱子分配不同的颜色
                const colors = ['#626c91', '#6be6c1', '#3fb1e3'];
                return colors[params.dataIndex % colors.length];
              }
            }
          }
        ]
      };

      this.chart.setOption(option);
    },
    resizeChart() {
      if (this.chart) {
        this.chart.resize(); // 让图表响应窗口大小变化
      }
    }
  }
}
</script>

<style scoped>
.bar-chart-wrapper {
  background-color: rgba(10, 31, 68, 0.5); /* 半透明背景 */
  backdrop-filter: blur(10px); /* 毛玻璃效果 */
  padding: 20px;
  width: 80%;
  height: 140px;
  margin: 20px auto;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2); /* 轻微阴影 */
}

.bar-chart {
  width: 100%;
  color: #ccc; /* 全局文本颜色设置为 #ccc */

}

.chart-container {
  width: 100%;
  height: 160px; /* 确保高度适中 */
  margin-top: -30px; /* 确保没有额外的上间距 */
}

.title-container {
  display: flex;
  align-items: center;
}

.title-icon {
  margin-right: 10px;
  width: 14px;
  height: 14px; /* 控制图标大小 */
}

.chart-title {
  font-size: 12px;
  font-weight: bold;
  color: #ccc;
}

@media (max-width: 768px) {
  .chart-container {
    height: 150px; /* 在小屏幕上缩小图表高度 */
  }

  .chart-title {
    font-size: 12px; /* 小屏幕上缩小标题字体 */
  }

  .title-icon {
    width: 15px;
    height: 15px; /* 缩小图标 */
  }
}
</style>
