import service from '@/utils/request';

export async function fetchZTFFJZPJYData() {
  try {
    const response = await service({
      url: 'http://10.11.0.20:8088/dev-api/gateway/ZTFFJZPJY',
      //url: 'http://localhost:8088/dev-api/gateway/ZTFFJZPJY',
      method: 'post',
    });
    console.log('Response data for ZTFFJZPJY:', response);
    return response;
  } catch (error) {
    console.error('Error fetching ZTFFJZPJY data:', error);
    throw error;
  }
}
