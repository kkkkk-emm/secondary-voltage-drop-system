import request from './request'

/**
 * 上传图片并执行 OCR + 键值对抽取
 */
export function extractOcr(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/ocr/extract',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    timeout: 60000,
  })
}

