<template>
  <div class="crawl-source-page">
    <div class="page-header">
      <h2 class="page-title">抓取源管理</h2>
      <div class="header-actions">
        <a-button @click="handleTriggerCrawl" :loading="crawling">
          <ThunderboltOutlined /> 手动触发抓取
        </a-button>
        <a-button type="primary" @click="showAddModal">
          <PlusOutlined /> 新增抓取源
        </a-button>
      </div>
    </div>

    <a-table :dataSource="sources" :columns="columns" :loading="loading" rowKey="id" :pagination="false">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'enabled'">
          <a-switch :checked="record.enabled === 1" @change="(val: boolean) => handleToggle(record, val)" />
        </template>
        <template v-if="column.key === 'action'">
          <a-space>
            <a @click="showEditModal(record)">编辑</a>
            <a-popconfirm title="确定删除？" @confirm="handleDelete(record.id)">
              <a style="color: #ff4d4f">删除</a>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-modal v-model:open="modalVisible" :title="editingId ? '编辑抓取源' : '新增抓取源'" @ok="handleSubmit" :confirmLoading="submitting">
      <a-form :model="form" layout="vertical">
        <a-form-item label="名称" required>
          <a-input v-model:value="form.name" placeholder="如 Hacker News" />
        </a-form-item>
        <a-form-item label="URL" required>
          <a-input v-model:value="form.url" placeholder="https://..." />
        </a-form-item>
        <a-form-item label="条目选择器" required>
          <a-input v-model:value="form.itemSelector" placeholder="CSS 选择器，如 .athing" />
        </a-form-item>
        <a-form-item label="标题选择器" required>
          <a-input v-model:value="form.titleSelector" placeholder="相对于条目，如 .titleline > a" />
        </a-form-item>
        <a-form-item label="链接选择器" required>
          <a-input v-model:value="form.linkSelector" placeholder="如 .titleline > a" />
        </a-form-item>
        <a-form-item label="摘要选择器">
          <a-input v-model:value="form.descSelector" placeholder="可选" />
        </a-form-item>
        <a-form-item label="Cron 表达式">
          <a-input v-model:value="form.cronExpression" placeholder="0 */30 * * * ?" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, ThunderboltOutlined } from '@ant-design/icons-vue'
import {
  listCrawlSources,
  addCrawlSource,
  updateCrawlSource,
  deleteCrawlSource,
} from '@/api/crawlSourceController'
import { triggerCrawl } from '@/api/hotController'

const loading = ref(false)
const crawling = ref(false)
const submitting = ref(false)
const modalVisible = ref(false)
const editingId = ref<number | null>(null)
const sources = ref<any[]>([])

const form = reactive({
  name: '',
  url: '',
  itemSelector: '',
  titleSelector: '',
  linkSelector: '',
  descSelector: '',
  cronExpression: '0 */30 * * * ?',
})

const columns = [
  { title: '名称', dataIndex: 'name', key: 'name', width: 120 },
  { title: 'URL', dataIndex: 'url', key: 'url', ellipsis: true },
  { title: '启用', key: 'enabled', width: 80 },
  { title: '操作', key: 'action', width: 120 },
]

const loadData = async () => {
  loading.value = true
  try {
    const res = await listCrawlSources()
    if (res.data.code === 0) {
      sources.value = res.data.data || []
    }
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.name = ''
  form.url = ''
  form.itemSelector = ''
  form.titleSelector = ''
  form.linkSelector = ''
  form.descSelector = ''
  form.cronExpression = '0 */30 * * * ?'
}

const showAddModal = () => {
  editingId.value = null
  resetForm()
  modalVisible.value = true
}

const showEditModal = (record: any) => {
  editingId.value = record.id
  Object.assign(form, {
    name: record.name,
    url: record.url,
    itemSelector: record.itemSelector,
    titleSelector: record.titleSelector,
    linkSelector: record.linkSelector,
    descSelector: record.descSelector || '',
    cronExpression: record.cronExpression || '0 */30 * * * ?',
  })
  modalVisible.value = true
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    if (editingId.value) {
      await updateCrawlSource({ id: editingId.value, ...form })
      message.success('更新成功')
    } else {
      await addCrawlSource(form)
      message.success('新增成功')
    }
    modalVisible.value = false
    loadData()
  } catch (e: any) {
    message.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (id: number) => {
  await deleteCrawlSource({ id })
  message.success('已删除')
  loadData()
}

const handleToggle = async (record: any, val: boolean) => {
  await updateCrawlSource({ id: record.id, enabled: val ? 1 : 0 })
  loadData()
}

const handleTriggerCrawl = async () => {
  crawling.value = true
  try {
    const res = await triggerCrawl()
    if (res.data.code === 0) {
      message.success('抓取任务已触发')
    } else {
      message.error(res.data.message || '触发失败')
    }
  } finally {
    crawling.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.crawl-source-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 32px 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}
</style>