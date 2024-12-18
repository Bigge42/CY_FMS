import service from '@/utils/request';

export async function fetchXZFZPYSData() {
  try {
    const response = await service({
      url: 'http://10.11.0.20:8088/dev-api/gateway/XZFZPYS',
      //url: 'http://localhost:8088/dev-api/gateway/XZFZPYS',
      method: 'post',
    });
    console.log('Response data for XZFZPYS:', response);
    return response;
  } catch (error) {
    console.error('Error fetching XZFZPYS data:', error);
    throw error;
  }
}
