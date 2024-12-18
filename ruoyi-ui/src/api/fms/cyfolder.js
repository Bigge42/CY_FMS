import request from '@/utils/request'

export function listCyfolderExcludeChild() {
}


// 查询文件夹列表
export function listCyfolder(query) {
  return request({
    url: '/fms/cyfolder/list',
    method: 'get',
    params: query
  })
}

// 查询文件夹详细
export function getCyfolder(folderId) {
  return request({
    url: '/fms/cyfolder/' + folderId,
    method: 'get'
  })
}

// 新增文件夹
export function addCyfolder(data) {
  return request({
    url: '/fms/cyfolder',
    method: 'post',
    data: data
  })
}

// 修改文件夹
export function updateCyfolder(data) {
  return request({
    url: '/fms/cyfolder',
    method: 'put',
    data: data
  })
}

// 删除文件夹
export function delCyfolder(folderId) {
  return request({
    url: '/fms/cyfolder/' + folderId,
    method: 'delete'
  })
}
