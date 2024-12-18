<template>
  <div class="exception-management">
    <div class="title">异常管理</div>
    <table>
      <thead>
        <tr>
          <th>计划号</th>
          <th>型号</th>
          <th>异常现象</th>
          <th>提交时间</th>
          <th>负责人</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in visibleExceptions" :key="item.planNumber">
          <td>{{ item.planNumber }}</td>
          <td>{{ item.model }}</td>
          <td>{{ item.exception }}</td>
          <td>{{ item.handlingDate }}</td>
          <td>{{ item.responsiblePerson }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import { loadVisualData } from '@/utils/loadVisualData';

export default {
  name: 'MyExceptionManagement',
  props: {
    type: {
      type: String,
      
    },
  },
  data() {
    return {
      exceptions: [],
      currentIndex: 0
    };
  },
  computed: {
    visibleExceptions() {
      return this.exceptions.slice(this.currentIndex, this.currentIndex + 2);
    }
  },
  mounted() {
    this.loadExceptionData();
    this.startAutoSwitch();
  },
  beforeDestroy() {
    if (this.switchInterval) {
      clearInterval(this.switchInterval);
    }
  },
  methods: {
    async loadExceptionData() {
      try {
        const type = this.type;
        const responseData = await loadVisualData(type);
        console.log('Full response data:', responseData); // 打印完整数据以验证结构

        // 直接检查 ZPYCLB 是否存在且是数组
        if (responseData && Array.isArray(responseData.ZPYCLB)) {
          const zpyclbData = responseData.ZPYCLB;
          this.exceptions = zpyclbData.map(item => ({
            planNumber: item.FMTONO,
            model: item.FTXH,
            exception: item.YCXX,
            handlingDate: item.TJSJ,
            responsiblePerson: item.SBR
          }));
        } else {
          console.error('Invalid exception data structure:', responseData);
        }
      } catch (error) {
        console.error('Error loading exception data:', error);
      }
    },
    startAutoSwitch() {
      this.switchInterval = setInterval(() => {
        if (this.currentIndex + 2 < this.exceptions.length) {
          this.currentIndex += 2;
        } else {
          this.currentIndex = 0;
        }
      }, 5000); // 每5秒自动切换一次
    }
  }
};
</script>

<style scoped>
.exception-management {
  background-color: rgba(10, 31, 68, 0.5); /* 半透明背景 */
  backdrop-filter: blur(10px); /* 毛玻璃效果 */
  padding: 10px ;
  width: 90%;
  margin: 15px auto;
  margin-top: 25%;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2); /* 轻微阴影 */
  color: #ccc;  /* 使用浅灰色文本 */
}

.title {
  text-align: left;
  font-size: 14px;
  color: #ccc;  /* 使用淡蓝色作为标题颜色 */
  margin-bottom: 10px;
}

table {
  width: 100%;
  border-collapse: collapse;  /* 合并单元格边框 */
  background-color: rgba(255, 255, 255, 0.1);  /* 半透明背景 */
  border: 1px solid #a0c4ff;  /* 边框颜色 */
}

th, td {
  font-size: 10px;
  padding: 7px;
  text-align: left;
  border: 1px solid #a0c4ff;  /* 单元格边框颜色 */
}

th {
  font-size: 8px;
  background-color: rgba(160, 196, 255, 0.2);  /* 淡蓝色背景 */
}

td {
  font-size: 10px;
  background-color: rgba(160, 196, 255, 0.1);  /* 更淡的背景颜色 */
}
</style>
