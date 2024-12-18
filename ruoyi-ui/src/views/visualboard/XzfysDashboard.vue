<template>
  <!-- 显示加载动画，直到数据加载完成 -->
  <div v-if="loading" class="loading-container">
    <div class="loading-chart" ref="loadingChart"></div> <!-- 加载动画图表 -->
  </div>

  <!-- 页面内容，当加载完成后显示 -->
  <div v-else id="visualboard">
    <!-- 第一行：标题 -->
    <div class="row title-row">
      <MyTitleBar :type="type"/>
    </div>

    <!-- 第二行：三列布局，最后一个元素底部对齐 -->
    <div class="row content-row">
      <!-- 第一列：考勤和排班表 -->
      <div class="column first-column">
        <MyAttendanceInfo :type="type" />
        <MyBarChart :type="type" />
        <MyPassRateChart :type="type" class="align-bottom" />
      </div>

      <!-- 第二列：任务清单、任务进度、合格率 -->
      <div class="column second-column">
        <MyTaskListChart :type="type" />
        <MyScheduleTable :type="type" class="align-bottom" />
      </div>

      <!-- 第三列：班组信息、设备状态、异常管理 -->
      <div class="column third-column">
        <MyTeamInfo :type="type" class="compact-spacing" />
        <MyEquipmentStatus :type="type" class="compact-spacing" />
        <MyExceptionManagement :type="type" class="compact-spacing align-bottom" />
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import { loadVisualData } from '@/utils/loadVisualData'; // 根据您提供的路径导入 loadVisualData 函数
import MyTitleBar from '@/views/visualboard/components/MyTitleBar.vue';
import MyAttendanceInfo from '@/views/visualboard/components/MyAttendanceInfo.vue';
import MyTeamInfo from '@/views/visualboard/components/MyTeamInfo.vue';
import MyTaskListChart from '@/views/visualboard/components/MyTaskListChart.vue';
import MyBarChart from '@/views/visualboard/components/MyBarChart.vue';
import MyPassRateChart from '@/views/visualboard/components/MyPassRateChart.vue';
import MyEquipmentStatus from '@/views/visualboard/components/MyEquipmentStatusChart.vue';
import MyScheduleTable from '@/views/visualboard/components/MyScheduleTable.vue';
import MyExceptionManagement from '@/views/visualboard/components/MyExceptionManagement.vue';

export default {
  name: 'XzfysDashboard',
  components: {
    MyTitleBar,
    MyAttendanceInfo,
    MyTeamInfo,
    MyTaskListChart,
    MyBarChart,
    MyPassRateChart,
    MyEquipmentStatus,
    MyScheduleTable,
    MyExceptionManagement,
  },
  data() {
    return {
      type: 'XZFZPYS', // 设定数据类型
      loading: true, // 控制加载动画的标志位
      visualData: null, // 用于存储请求的数据
    };
  },
  mounted() {
    this.initLoadingChart(); // 初始化加载动画
    this.fetchData(); // 请求数据
  },
  methods: {
    async fetchData() {
      try {
        // 调用 loadVisualData 函数，根据类型获取数据
        const data = await loadVisualData(this.type);
        console.log('Loaded data:', data);

        this.visualData = data; // 存储数据
        this.loading = false; // 数据加载完成，隐藏加载动画
      } catch (error) {
        console.error('Error while fetching visual board data:', error);
      }
    },
    initLoadingChart() {
      const chartDom = this.$refs.loadingChart;
      if (!chartDom) {
        console.warn("Loading chart DOM element not found");
        return;
      }

      const loadingChart = echarts.init(chartDom);

      // 加载动画的配置
      const option = {
        graphic: {
          elements: [
            {
              type: 'group',
              left: 'center',
              top: 'center',
              children: new Array(7).fill(0).map((val, i) => ({
                type: 'rect',
                x: i * 20,
                shape: {x: 0, y: -40, width: 10, height: 80},
                style: {fill: '#bb3b22'},
                keyframeAnimation: {
                  duration: 1000,
                  delay: i * 200,
                  loop: true,
                  keyframes: [
                    {percent: 0.5, scaleY: 0.3, easing: 'cubicIn'},
                    {percent: 1, scaleY: 1, easing: 'cubicOut'},
                  ],
                },
              })),
            },
          ],
        },
      };

      loadingChart.setOption(option);
    },
  },
};
</script>

<style scoped>
#visualboard {
  background-image: url('@/assets/visualboard/bg.jpg'); /* 添加背景图片 */
  background-position: center; /* 背景居中 */
  background-repeat: no-repeat; /* 防止图片重复 */
  max-width: 100%; /* 避免元素超出屏幕宽度 */
  max-height: 100%;
  z-index: 0;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 1080px;
  width: 1920px;
  margin-top: -5px;
}

.loading-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
}

.loading-chart {
  width: 200px;
  height: 200px;
}

.title-row {
  height: 150px;
  justify-content: center;
}

.row {
  display: flex;
  width: 100%;
  margin-bottom: 20px;
}

.content-row {
  display: flex;
  margin-top: -100px;
  width: 100%;
  height: 80%; /* 调整第二行的总高度 */
}

.column {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 30%; /* 三列等宽 */
}

.second-column {
  width: 40%;
  padding-right: 3%;
}

.align-bottom {
  margin-top: auto; /* 确保列中最后的元素对齐底部 */
}

.first-column,
.second-column,
.third-column {
  height: 100%; /* 确保列充满第二行的高度 */
  justify-content: space-between;
}
</style>
