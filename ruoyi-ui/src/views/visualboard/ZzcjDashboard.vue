<template>
    <div id="visualboard">
      <!-- 第一行：标题 -->
      <div class="row title-row">
        <MyTitleBar />
      </div>

      <!-- 第二行：三列布局，最后一个元素底部对齐 -->
      <div class="row content-row">
        <!-- 第一列：考勤和排班表 -->
        <div class="column first-column">
          <MyAttendanceInfo />
          <MyBarChart />
          <MyPassRateChart class="align-bottom" />
        </div>

        <!-- 第二列：任务清单、任务进度、合格率 -->
        <div class="column second-column">
          <MyTaskListChart />
          <MyScheduleTable class="align-bottom" />
        </div>

        <!-- 第三列：班组信息、设备状态、异常管理 -->
        <div class="column third-column">
          <MyTeamInfo class="compact-spacing"/>
          <MyEquipmentStatus class="compact-spacing"/>
          <MyExceptionManagement class="compact-spacing align-bottom" />
        </div>
      </div>
    </div>
</template>

<script>
import MyTitleBar from '@/components/VisualBoard/MyTitleBar.vue';
import MyAttendanceInfo from '@/components/VisualBoard/MyAttendanceInfo.vue';
import MyTeamInfo from '@/components/VisualBoard/MyTeamInfo.vue';
import MyTaskListChart from '@/components/VisualBoard/MyTaskListChart.vue';
import MyBarChart from '@/components/VisualBoard/MyBarChart.vue';
import MyPassRateChart from '@/components/VisualBoard/MyPassRateChart.vue';
import MyEquipmentStatus from '@/components/VisualBoard/MyEquipmentStatusChart.vue';
import MyScheduleTable from '@/components/VisualBoard/MyScheduleTable.vue';
import MyExceptionManagement from '@/components/VisualBoard/MyExceptionManagement.vue';

export default {
  name: 'ZzcjDashboard',
  components: {
    MyTitleBar,
    MyAttendanceInfo,
    MyTeamInfo,
    MyTaskListChart,
    MyBarChart,
    MyPassRateChart,
    MyEquipmentStatus,
    MyScheduleTable,
    MyExceptionManagement
  }
};
</script>

<style>
#visualboard {
  background-image: url('~@/assets/visualboard/bg.jpg'); /*添加背景图片 */
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

.first-column, .second-column, .third-column {
  height: 100%; /* 确保列充满第二行的高度 */
  justify-content: space-between;
}
</style>
