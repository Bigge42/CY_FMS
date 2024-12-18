<template>
  <div class="pass-rate-chart-wrapper">
    <div class="title-container">
      <img :src="chartIcon" alt="图标" class="title-icon" />
      <span class="chart-title">合格数</span>
    </div>
    <div ref="passRateChart" class="chart-container"></div>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import chartIcon from '@/assets/visualboard/chat-arrow-grow.png';
import { loadVisualData } from '@/utils/loadVisualData';


export default {
  name: 'MyPassRateChart',
  props: {
    type: {
      type: String,
      
    },
  },
  data() {
    return {
      chartInstance: null,
      hgsData: [],
      chartIcon: chartIcon,
    };
  },
  async mounted() {
    await this.loadPassRateData();
    this.initChart();
  },
  methods: {
    async loadPassRateData() {
      try {
        const type = this.type;
        const responseData = await loadVisualData(type);
        if (responseData && Array.isArray(responseData.HGS)) {
          this.hgsData = responseData.HGS;
        } else {
          console.error('Invalid HGS data:', responseData);
        }
      } catch (error) {
        console.error('Error loading HGS data:', error);
      }
    },
    initChart() {
      this.chartInstance = echarts.init(this.$refs.passRateChart);
      const categories = this.hgsData.map(item => `${item.RQ}:00`);
      const qualifiedData = this.hgsData.map(item => item.SL);
      
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            label: {
              backgroundColor: '#283b56'
            }
          }
        },
        legend: {
          data: ['合格数'],
          show: false // 隐藏图例
        },
        toolbox: {
          show: false, // 隐藏工具栏按钮
        },
        xAxis: {
          type: 'category',
          boundaryGap: true,
          axisLabel: {
            color: '#ccc' // Y 轴文本颜色为 #ccc
          },
          data: categories
        },
        yAxis: [
          {
            type: 'value',
            name: '',
            min: 0,
            axisLabel: {
            color: '#ccc' // Y 轴文本颜色为 #ccc
          },
            max: Math.max(...qualifiedData) + 5,
            boundaryGap: [0.2, 0.2]
          }
        ],
        series: [
          {
            name: '合格数',
            type: 'bar',
            data: qualifiedData
          }
        ]
      };

      this.chartInstance.setOption(option);
    }
  }
};
</script>

<style scoped>
.pass-rate-chart-wrapper {
  background-color: rgba(10, 31, 68, 0.5); /* 半透明背景 */
  backdrop-filter: blur(10px); /* 毛玻璃效果 */
  padding: 10px; /* 调整 padding 为缩小比例 */
  width: 90%; /* 宽度缩小 */
  margin: 10px auto; /* 调整 margin */
  height: 240px; /* 调整高度为 50% 以拉长图表 */
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

.chart-container {
  width: 100%;
  height: 240px; /* 调整高度以适应容器并拉长图表部分 */
}
</style>
