<template>
  <div class="pass-rate-chart-wrapper">
    <div class="title-container">
      <img src="@/assets/visualboard/chat-arrow-grow.png" alt="图标" class="title-icon" />
      <span class="chart-title">合格率</span>
    </div>
    <div ref="chart" class="pass-rate-chart"></div>
  </div>
</template>

<script>
import * as echarts from 'echarts';

export default {
  name: 'MyPassRateChart',
  mounted() {
    this.initChart();
  },
  methods: {
    initChart() {
      let category = [];
      let dottedBase = +new Date();
      let lineData = []; // 合格率数据
      let barData = []; // 合格数量数据

      // 生成两周的合格数量和合格率
      for (let i = 0; i < 14; i++) {
        let date = new Date((dottedBase += 3600 * 24 * 1000));
        category.push(
          [date.getFullYear(), date.getMonth() + 1, date.getDate()].join('-')
        );
        let qualifiedCount = Math.random() * 40 + 240; // 合格数量在 240 到 280 之间浮动
        let qualifiedRate = (Math.random() * 10 + 85).toFixed(2); // 合格率在 85% 到 95% 之间浮动
        barData.push(qualifiedCount);
        lineData.push(qualifiedRate); // 合格率百分比
      }

      let chart = echarts.init(this.$refs.chart);

      let option = {
        backgroundColor: '#0f375f',
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: function (params) {
            let result = params[0].name + '<br/>';
            params.forEach(function (item) {
              result += item.seriesName + ': ' + item.data + (item.seriesName === '合格率' ? '%' : '') + '<br/>';
            });
            return result;
          }
        },
        legend: {
          data: ['合格数量', '合格率'],
          textStyle: {
            color: '#ccc'
          }
        },
        xAxis: {
          data: category,
          axisLine: {
            lineStyle: {
              color: '#ccc'
            }
          }
        },
        grid: {
          left: '3%',     // 左边距
          right: '3%',    // 增加右边距，确保折线图不会被裁剪
          bottom: '3%',
          containLabel: true
        },
        yAxis: [
          {
            type: 'value',
            name: '数量',
            axisLine: {
              lineStyle: {
                color: '#ccc'
              }
            }
          },
          {
            type: 'value',
            name: '合格率',
            position: 'right',
            min: 80,
            max: 100,
            axisLabel: {
              formatter: '{value} %'
            },
            axisLine: {
              lineStyle: {
                color: '#ccc'
              }
            }
          }
        ],
        series: [
          {
            name: '合格数量',
            type: 'bar',
            barWidth: 10, // 缩小柱状图宽度
            itemStyle: {
              borderRadius: 5,
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#14c8d4' },
                { offset: 1, color: '#43eec6' }
              ])
            },
            data: barData
          },
          {
            name: '合格率',
            type: 'line',
            smooth: true,
            yAxisIndex: 1, // 使用第二个Y轴（合格率）
            showAllSymbol: true,
            symbol: 'emptyCircle',
            symbolSize: 6, // 缩小点的大小
            itemStyle: {
              color: '#FFD700' // 合格率线的颜色
            },
            data: lineData
          }
        ]
      };

      chart.setOption(option);
    }
  }
};
</script>

<style scoped>
.pass-rate-chart-wrapper {
  background-color: rgba(10, 31, 68, 0.5); /* 半透明背景 */
  backdrop-filter: blur(10px); /* 毛玻璃效果 */
  padding: 10px; /* 缩小 padding */
  width: 85%; /* 宽度 */
  height: 252px; /* 外层容器高度设定为200px */
  margin: 10px auto;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2); /* 轻微阴影 */
}

.title-container {
  display: flex;
  align-items: center;
  margin-bottom: 5px; /* 缩小 margin */
}

.title-icon {
  width: 14px; /* 缩小图标 */
  height: 14px;
  margin-right: 10px;
}

.chart-title {
  font-size: 12px; /* 缩小字体 */
  color: #ccc;
  font-weight: bold;
}

.pass-rate-chart {
  width: 100%;
  height: 230px; /* 图表高度调整为容器的 140px，留出空间给标题 */
}
</style>
