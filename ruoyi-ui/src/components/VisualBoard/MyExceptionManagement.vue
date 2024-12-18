<template>
    <div class="exception-management">
      <div class="title">异常管理</div>
      <table>
        <thead>
          <tr>
            <th>计划号</th>
            <th>型号</th>
            <th>异常现象</th>
            <th>处理时间</th>
            <th>超期天数</th>
            <th>负责人</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in exceptions" :key="item.planNumber">
            <td>{{ item.planNumber }}</td>
            <td>{{ item.model }}</td>
            <td>{{ item.exception }}</td>
            <td>{{ item.handlingDate }}</td>
            <td>{{ item.overdueDays }}</td>
            <td>{{ item.responsiblePerson }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </template>
  
  <script>
  export default {
    name: 'MyExceptionManagement',
    data() {
      return {
        exceptions: [
          {
            planNumber: '23-03-M0999V-266',
            model: 'HCP-10000',
            exception: '阀体异常',
            handlingDate: '8月30',
            overdueDays: 5,
            responsiblePerson: '崔伟'
          },
          {
            planNumber: '23-03-M0999V-285',
            model: 'HCP-10000',
            exception: '手轮异常',
            handlingDate: '9月3',
            overdueDays: 2,
            responsiblePerson: '蒋先友'
          }
          /*,
          {
            planNumber: '23-03-M0999V-311',
            model: 'HCP-10000',
            exception: '附件异常',
            handlingDate: '9月1',
            overdueDays: 3,
            responsiblePerson: '罗南雄'
          }*/
        ]
      };
    }
  };
  </script>
  
  <style scoped>
  .exception-management {
    background-color: rgba(10, 31, 68, 0.5); /* 半透明背景 */
    backdrop-filter: blur(10px); /* 毛玻璃效果 */
    padding: 10px;
    width: 90%;
    margin: 15px auto;
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
  