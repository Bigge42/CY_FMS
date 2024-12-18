<template>
  <div class="bar-chart-wrapper">
    <div class="bar-chart">
      <div class="title-container">
        <img src="../../../assets/visualboard/list.png" alt="列表图标" class="title-icon" />
        <span class="chart-title">任务执行进度</span>
      </div>
      <div ref="chart" class="chart-container"></div>
    </div>
  </div>
</template>

<script>
import { nextTick } from 'vue';
import * as echarts from 'echarts';
import { loadVisualData } from '@/utils/loadVisualData';

export default {
  name: 'MyBarChart',
  props: {
    type: {
      type: String,
    },
  },
  data() {
    return {
      kgsl: 0,
      wgsl: 0,
      jhsl: 0,
    };
  },
  mounted() {
    this.loadData();
    nextTick(() => {
      this.initChart();
      window.addEventListener('resize', this.resizeChart); // 添加窗口变化监听器
    });
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.resizeChart); // 组件销毁时移除监听器
    if (this.chart) {
      this.chart.dispose(); // 组件销毁时清除图表实例
    }
  },
  methods: {
    async loadData() {
      try {
        const type = this.type;
        const data = await loadVisualData(type); // 使用传递的 type
        if (data && Array.isArray(data.JD) && data.JD.length > 0) {
          const jdData = data.JD[0];
          this.kgsl = jdData.KGSL;
          this.wgsl = jdData.WGSL;
          this.jhsl = jdData.JHSL;
          this.updateChart();
        } else {
          console.error('Invalid JD data:', data.JD);
        }
      } catch (error) {
        console.error(`Error loading data for type ${type}:`, error);
      }
    },
    initChart() {
      const chartDom = this.$refs.chart;
      if (!chartDom) {
        console.warn("Chart DOM element not found");
        return;
      }

      this.chart = echarts.init(chartDom, 'walden'); // 使用全局注册的 walden 主题
      this.updateChart();
    },
    updateChart() {
      if (!this.chart) {
        return;
      }

      const option = {
        title: {
          text: '',
          textStyle: {
            color: '#ccc',
          },
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow',
          },
        },
        grid: {
          left: '3%',
          right: '5%',
          bottom: '3%',
          containLabel: true,
        },
        xAxis: {
          type: 'value',
          axisLabel: {
            color: '#ccc',
          },
        },
        yAxis: {
          type: 'category',
          axisLabel: {
            color: '#ccc',
          },
          data: ['开工数量', '完成数量', '计划数量'],
        },
        series: [
          {
            name: '进度',
            type: 'bar',
            data: [this.kgsl, this.wgsl, this.jhsl],
            itemStyle: {
              color: function (params) {
                const colors = ['#626c91', '#6be6c1', '#3fb1e3'];
                return colors[params.dataIndex % colors.length];
              },
            },
          },
        ],
      };

      this.chart.setOption(option);
    },
    resizeChart() {
      if (this.chart) {
        this.chart.resize(); // 让图表响应窗口大小变化
      }
    },
  },
};
</script>

<style scoped>
.bar-chart-wrapper {
  background-color: rgba(10, 31, 68, 0.5);
  backdrop-filter: blur(10px);
  padding: 20px;
  width: 90%;
  height: 45%;
  margin: auto;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
}

.bar-chart {
  width: 100%;
  color: #ccc;
}

.chart-container {
  width: 100%;
  /*height: 140px;*/
  height: 240px;
  margin-top: -30px;
}

.title-container {
  display: flex;
  align-items: center;
}

.title-icon {
  margin-right: 10px;
  width: 14px;
  height: 14px;
}

.chart-title {
  font-size: 12px;
  font-weight: bold;
  color: #ccc;
}

@media (max-width: 768px) {
  .chart-container {
    height: 150px;
  }

  .chart-title {
    font-size: 12px;
  }

  .title-icon {
    width: 15px;
    height: 15px;
  }
}
</style>
