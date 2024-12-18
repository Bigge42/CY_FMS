import request from '@/utils/request'

// 查询文件信息列表
export function listCyfile(query) {
  return request({
    url: '/fms/cyfile/list',
    method: 'get',
    params: query
  })
}

// 查询文件信息详细
export function getCyfile(fileId) {
  return request({
    url: '/fms/cyfile/' + fileId,
    method: 'get'
  })
}

// 新增文件信息
export function addCyfile(data) {
  return request({
    url: '/fms/cyfile',
    method: 'post',
    data: data
  })
}

// 修改文件信息
export function updateCyfile(data) {
  return request({
    url: '/fms/cyfile',
    method: 'put',
    data: data
  })
}

// 删除文件信息
export function delCyfile(fileId) {
  return request({
    url: '/fms/cyfile/' + fileId,
    method: 'delete'
  })
}
