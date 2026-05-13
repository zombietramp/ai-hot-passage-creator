// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 分页查询热榜 GET /hot/list */
export async function listHotArticles(
  params: {
    pageNum?: number
    pageSize?: number
    sourceId?: number
    sortField?: string
    sortOrder?: string
  },
  options?: { [key: string]: any },
) {
  return request('/hot/list', {
    method: 'GET',
    params: { ...params },
    ...(options || {}),
  })
}

/** 获取所有抓取源（用于前端筛选） GET /hot/sources */
export async function listHotSources(options?: { [key: string]: any }) {
  return request('/hot/sources', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 手动触发抓取 POST /hot/trigger-crawl */
export async function triggerCrawl(options?: { [key: string]: any }) {
  return request('/hot/trigger-crawl', {
    method: 'POST',
    ...(options || {}),
  })
}

/** 全部 AI 动态 GET /hot/all */
export async function fetchAllItems(
  params?: { take?: number; cursor?: string; q?: string; category?: string; mode?: string },
  options?: { [key: string]: any },
) {
  return request('/hot/all', {
    method: 'GET',
    params: { ...params },
    ...(options || {}),
  })
}

/** 最新 AI 日报 GET /hot/daily */
export async function fetchDaily(options?: { [key: string]: any }) {
  return request('/hot/daily', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 指定日期日报 GET /hot/daily/{date} */
export async function fetchDailyByDate(date: string, options?: { [key: string]: any }) {
  return request(`/hot/daily/${date}`, {
    method: 'GET',
    ...(options || {}),
  })
}

/** 日报列表 GET /hot/dailies */
export async function fetchDailies(
  params?: { take?: number },
  options?: { [key: string]: any },
) {
  return request('/hot/dailies', {
    method: 'GET',
    params: { ...params },
    ...(options || {}),
  })
}
