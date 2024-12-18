import service from '@/utils/request';

export async function fetchVisualBoardData() {
  try {
    const response = await service({
      url: 'http://10.11.0.20:8088/dev-api/gateway/ZZCJCJDP',
      //url: 'http://localhost:8088/dev-api/gateway/ZZCJCJDP',
      method: 'post',
    });
    console.log('Response data:', response); // 打印返回的数据
    return response;
  } catch (error) {
    console.error('Error fetching visual board data:', error);
    throw error;
  }
}
