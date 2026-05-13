// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 新增抓取源 POST /crawl-source/add */
export async function addCrawlSource(
  body: {
    name: string
    url: string
    itemSelector: string
    titleSelector: string
    linkSelector: string
    descSelector?: string
    cronExpression?: string
  },
  options?: { [key: string]: any },
) {
  return request('/crawl-source/add', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    data: body,
    ...(options || {}),
  })
}

/** 更新抓取源 POST /crawl-source/update */
export async function updateCrawlSource(
  body: {
    id: number
    name?: string
    url?: string
    itemSelector?: string
    titleSelector?: string
    linkSelector?: string
    descSelector?: string
    cronExpression?: string
    enabled?: number
  },
  options?: { [key: string]: any },
) {
  return request('/crawl-source/update', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    data: body,
    ...(options || {}),
  })
}

/** 删除抓取源 POST /crawl-source/delete */
export async function deleteCrawlSource(
  body: { id: number },
  options?: { [key: string]: any },
) {
  return request('/crawl-source/delete', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    data: body,
    ...(options || {}),
  })
}

/** 查询所有抓取源 GET /crawl-source/list */
export async function listCrawlSources(options?: { [key: string]: any }) {
  return request('/crawl-source/list', {
    method: 'GET',
    ...(options || {}),
  })
}
