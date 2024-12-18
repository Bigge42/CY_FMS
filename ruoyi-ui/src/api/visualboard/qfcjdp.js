import service from '@/utils/request';

export async function fetchQFCJDPData() {
    try {
        const response = await service({
            url: 'http://10.11.0.20:8088/dev-api/gateway/QFCJDP',
            method: 'post',
        });
        console.log('Response data for QFCJDP:', response);
        return response;
    } catch (error) {
        console.error('Error fetching QFCJDP data:', error);
        throw error;
    }
}
