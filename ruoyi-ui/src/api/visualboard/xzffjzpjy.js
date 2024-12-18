import service from '@/utils/request';

export async function fetchXZFFJZPJYData() {
  try {
    const response = await service({
      url: 'http://10.11.0.20:8088/dev-api/gateway/XZFFJZPJY',
      //url: 'http://localhost:8088/dev-api/gateway/XZFFJZPJY',
      method: 'post',
    });
    console.log('Response data for XZFFJZPJY:', response);
    return response;
  } catch (error) {
    console.error('Error fetching XZFFJZPJY data:', error);
    throw error;
  }
}
