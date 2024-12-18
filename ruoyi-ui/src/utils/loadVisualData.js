import cache from '@/plugins/cache';
import { fetchVisualBoardData } from '@/api/visualboard/zzcjkb';
import { fetchXZFFJZPJYData } from '@/api/visualboard/xzffjzpjy';
import { fetchZTFFJZPJYData } from '@/api/visualboard/ztffjzpjy';
import { fetchXZFZPYSData } from '@/api/visualboard/xzfzpys';
import {fetchQFCJDPData} from "@/api/visualboard/qfcjdp.js";

const cacheDuration = 30 * 60 * 1000; // 缓存时间 30 分钟

/**
 * 根据 type 加载对应的接口数据，支持缓存。
 * @param {string} type 数据类型，可选值：'ZZCJCJDP', 'XZFFJZPJY', 'ZTFFJZPJY', 'XZFZPYS','QFCJDP'
 * @returns {Promise<any>} 返回接口数据
 */
export async function loadVisualData(type) {
  const cacheKey = `${type}Data`;
  const currentTime = new Date().getTime();
  const cachedData = cache.session.getJSON(cacheKey);
  const cachedTime = cache.session.getJSON(`${cacheKey}_time`);

  // 检查缓存是否有效
  if (cachedData && cachedTime && (currentTime - cachedTime < cacheDuration)) {
    return cachedData;
  }

  // 根据 type 调用不同的接口
  let fetchFunction;
  switch (type) {
    case 'ZZCJCJDP':
      fetchFunction = fetchVisualBoardData;
      break;
    case 'XZFFJZPJY':
      fetchFunction = fetchXZFFJZPJYData;
      break;
    case 'ZTFFJZPJY':
      fetchFunction = fetchZTFFJZPJYData;
      break;
    case 'XZFZPYS':
      fetchFunction = fetchXZFZPYSData;
      break;
    case 'QFCJDP':
      fetchFunction = fetchQFCJDPData;
      break;
    default:
      throw new Error(`Unsupported data type: ${type}`);
  }

  try {
    // 调用对应的接口函数
    const response = await fetchFunction();
    const data = response.data;

    // 存储新数据到缓存
    cache.session.setJSON(cacheKey, data);
    cache.session.setJSON(`${cacheKey}_time`, currentTime);

    return data;
  } catch (error) {
    console.error(`Error fetching data for type ${type}:`, error);
    throw error;
  }
}
