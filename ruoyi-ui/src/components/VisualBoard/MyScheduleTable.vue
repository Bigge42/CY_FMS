<template>
  <div class="schedule-wrapper">
    <!-- 表格标题和第一个表格 -->
    <div class="table-container">
      <div class="table-title">
        <img src="@/assets/visualboard/list-check.png" alt="图标" class="title-icon" />
        <h2>8:00-15:30排班</h2>
      </div>
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
          <tr v-for="(row, index) in firstShiftData" :key="index">
            <td>{{ row.planNumber }}</td>
            <td>{{ row.model }}</td>
            <td>{{ row.diameter }}</td>
            <td>{{ row.pressure }}</td>
            <td>{{ row.planCount }}</td>
            <td :class="{'status-error': row.status === '异常'}">{{ row.status }}</td>
            <td>{{ row.process }}</td>
            <td>{{ row.specialRequest }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 表格标题和第二个表格 -->
    <div class="table-container">
      <div class="table-title">
        <img src="@/assets/visualboard/list-check.png" alt="图标" class="title-icon" />
        <h2>15:00-22:30排班</h2>
      </div>
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
          <tr v-for="(row, index) in secondShiftData" :key="index">
            <td>{{ row.planNumber }}</td>
            <td>{{ row.model }}</td>
            <td>{{ row.diameter }}</td>
            <td>{{ row.pressure }}</td>
            <td>{{ row.planCount }}</td>
            <td :class="{'status-error': row.status === '异常'}">{{ row.status }}</td>
            <td>{{ row.process }}</td>
            <td>{{ row.specialRequest }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MyScheduleTable',
  data() {
    return {
      firstShiftData: [
        { planNumber: "22-10-M056-40", model: "HCP-10000", diameter: "DN200", pressure: "Class300", planCount: 1, status: "正常", process: "装配", specialRequest: "" },
        { planNumber: "23-02-M067V-1", model: "VFR-111", diameter: "DN250", pressure: "Class600", planCount: 1, status: "正常", process: "装配", specialRequest: "验货" },
        { planNumber: "23-02-M067V-2", model: "VFR-111", diameter: "DN250", pressure: "Class600", planCount: 1, status: "异常", process: "压水", specialRequest: "验货" },
        { planNumber: "23-03-M529FV-307", model: "HCP-10001", diameter: "DN50", pressure: "PN25", planCount: 1, status: "正常", process: "装配", specialRequest: "" },
        { planNumber: "23-03-M688-131", model: "HTS-10010", diameter: "DN25", pressure: "PN20", planCount: 1, status: "正常", process: "压水", specialRequest: "" },
        { planNumber: "23-03-M0981V-114", model: "VFR-111", diameter: "DN100", pressure: "PN25", planCount: 1, status: "正常", process: "装配", specialRequest: "" },
      ],
      secondShiftData: [
        { planNumber: "23-04-M056-41", model: "HCP-20000", diameter: "DN300", pressure: "Class400", planCount: 1, status: "正常", process: "装配", specialRequest: "" },
        { planNumber: "23-05-M067V-3", model: "VFR-211", diameter: "DN350", pressure: "Class700", planCount: 1, status: "正常", process: "装配", specialRequest: "验货" },
        { planNumber: "23-06-M067V-4", model: "VFR-111", diameter: "DN250", pressure: "Class600", planCount: 1, status: "异常", process: "压水", specialRequest: "验货" },
        { planNumber: "23-07-M529FV-308", model: "HCP-20001", diameter: "DN60", pressure: "PN50", planCount: 1, status: "正常", process: "装配", specialRequest: "" },
        { planNumber: "23-08-M688-132", model: "HTS-20010", diameter: "DN35", pressure: "PN30", planCount: 1, status: "正常", process: "压水", specialRequest: "" },
        { planNumber: "23-09-M0981V-115", model: "VFR-211", diameter: "DN120", pressure: "PN30", planCount: 1, status: "正常", process: "装配", specialRequest: "" },
      ]
    };
  }
};
</script>

<style scoped>
.schedule-wrapper {
  background-color: rgba(10, 31, 68, 0.5); /* 半透明背景 */
  backdrop-filter: blur(10px); /* 毛玻璃效果 */
  padding: 10px;
  width: 100%;
  height: 450px;
  margin: 20px auto;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2); /* 轻微阴影 */
}

.table-container {
  margin-top: -10px ;
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

.schedule-table {
  width: 100%;
  border-collapse: collapse;
  color: #ccc;
}

.schedule-table th,
.schedule-table td {
  padding: 7px;
  text-align: left;
  font-size: 8px;
  border: 1px solid #a0c4ff;  /* 边框颜色 */
}

.schedule-table th {
  background-color: rgba(160, 196, 255, 0.2);  /* 淡蓝色背景 */
}

.schedule-table td {
  background-color: rgba(160, 196, 255, 0.1);  /* 更淡的背景颜色 */
}
</style>
