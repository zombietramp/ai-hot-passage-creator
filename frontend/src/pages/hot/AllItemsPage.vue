<template>
  <div class="feed-page">
    <div class="page-header">
      <h1 class="page-title">全部 AI 动态</h1>
      <div class="page-subtitle">AI 领域全量资讯流</div>
    </div>

    <div class="feed-toolbar-row">
      <div class="segmented">
        <button
          v-for="cat in categories"
          :key="cat.value"
          :class="['segmented-btn', { active: activeCategory === cat.value }]"
          @click="setCategory(cat.value)"
        >{{ cat.label }}</button>
      </div>
      <form class="filter-form" @submit.prevent="handleSearch">
        <input v-model="searchQuery" class="filter-input" placeholder="搜索标题/摘要…" />
        <button type="submit" class="filter-submit">搜索</button>
      </form>
    </div>

    <a-spin :spinning="loading">
      <section class="timeline">
        <div v-for="group in groupedItems" :key="group.date" class="timeline-day">
          <div class="timeline-day-head">
            <div class="timeline-date">{{ group.dateLabel }}</div>
          </div>
          <div class="timeline-day-items">
            <div v-for="item in group.items" :key="item.id" class="timeline-item">
              <div class="timeline-time">{{ item.timeLabel }}</div>
              <div class="timeline-rail"><div class="timeline-dot"></div></div>
              <div class="timeline-card">
                <div class="timeline-card-head">
                  <div class="timeline-head-left">
                    <span class="timeline-source">{{ item.source }}</span>
                  </div>
                  <div class="timeline-head-right">
                    <span v-if="item.category" class="timeline-cat">{{ categoryLabel(item.category) }}</span>
                  </div>
                </div>
                <h3 class="timeline-title">
                  <a :href="item.url" target="_blank" rel="noopener">{{ item.title }}</a>
                </h3>
                <p class="timeline-summary" v-if="item.summary">{{ item.summary }}</p>
                <div class="timeline-footer-row">
                  <span></span>
                  <a-button type="link" size="small" class="create-btn" @click="goCreate(item)">
                    <EditOutlined /> 创作
                  </a-button>
                </div>
              </div>
            </div>
          </div>
        </div>
        <a-empty v-if="!loading && items.length === 0" description="暂无数据" />
      </section>
    </a-spin>

    <div class="feed-pagination" v-if="hasNext || cursor">
      <a-button @click="loadMore" :loading="loadingMore" v-if="hasNext">加载更多</a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { EditOutlined } from '@ant-design/icons-vue'
import { fetchAllItems } from '@/api/hotController'
import dayjs from 'dayjs'

const router = useRouter()
const loading = ref(false)
const loadingMore = ref(false)
const items = ref<any[]>([])
const hasNext = ref(false)
const cursor = ref<string | undefined>(undefined)
const searchQuery = ref('')
const activeCategory = ref('')

const categories = [
  { label: '全部', value: '' },
  { label: '模型', value: 'ai-models' },
  { label: '产品', value: 'ai-products' },
  { label: '研究', value: 'ai-research' },
  { label: '论文', value: 'paper' },
  { label: '技巧', value: 'tip' },
]

// PLACEHOLDER_ALL_SCRIPT

const groupedItems = computed(() => {
  const groups: Record<string, { date: string; dateLabel: string; items: any[] }> = {}
  for (const item of items.value) {
    const d = dayjs(item.publishedAt)
    const key = d.format('YYYY-MM-DD')
    if (!groups[key]) {
      groups[key] = { date: key, dateLabel: d.format('M月D日'), items: [] }
    }
    item.timeLabel = d.format('HH:mm')
    groups[key].items.push(item)
  }
  return Object.values(groups).sort((a, b) => b.date.localeCompare(a.date))
})

const categoryLabel = (cat: string) => {
  const map: Record<string, string> = { 'ai-models': '模型', 'ai-products': '产品', 'ai-research': '研究', paper: '论文', tip: '技巧' }
  return map[cat] || cat
}

const loadData = async (append = false) => {
  if (append) { loadingMore.value = true } else { loading.value = true; cursor.value = undefined }
  try {
    const res = await fetchAllItems({
      take: 40,
      cursor: append ? cursor.value : undefined,
      q: searchQuery.value || undefined,
      category: activeCategory.value || undefined,
    })
    if (res.data.code === 0 && res.data.data) {
      const parsed = typeof res.data.data === 'string' ? JSON.parse(res.data.data) : res.data.data
      if (append) { items.value = [...items.value, ...(parsed.items || [])] }
      else { items.value = parsed.items || [] }
      hasNext.value = parsed.hasNext || false
      cursor.value = parsed.nextCursor || undefined
    }
  } finally { loading.value = false; loadingMore.value = false }
}

const setCategory = (cat: string) => { activeCategory.value = cat; loadData() }
const handleSearch = () => loadData()
const loadMore = () => loadData(true)
const goCreate = (item: any) => {
  router.push({ path: '/create', query: { topic: item.title } })
}

onMounted(() => loadData())
</script>

<style scoped>
.feed-page { max-width: 860px; margin: 0 auto; padding: 32px 24px; }

.page-header { margin-bottom: 20px; }
.page-title { font-size: 22px; font-weight: 700; margin: 0 0 4px; }
.page-subtitle { font-size: 13px; color: var(--color-text-muted); }

.feed-toolbar-row { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 24px; flex-wrap: wrap; }

.segmented { display: flex; gap: 0; border: 1px solid var(--color-border); border-radius: var(--radius-md); overflow: hidden; }
.segmented-btn { padding: 6px 14px; font-size: 13px; font-weight: 500; border: none; background: var(--color-background); color: var(--color-text-secondary); cursor: pointer; transition: all 0.15s; }
.segmented-btn:hover { background: var(--color-background-secondary); }
.segmented-btn.active { background: var(--color-primary); color: #fff; }

.filter-form { display: flex; gap: 6px; }
.filter-input { padding: 6px 12px; border: 1px solid var(--color-border); border-radius: var(--radius-sm); font-size: 13px; background: var(--color-background); color: var(--color-text); width: 180px; }
.filter-input::placeholder { color: var(--color-text-muted); }
.filter-submit { padding: 6px 14px; border: none; border-radius: var(--radius-sm); background: var(--color-primary); color: #fff; font-size: 13px; font-weight: 500; cursor: pointer; }

.timeline { display: flex; flex-direction: column; }
.timeline-day { margin-bottom: 8px; }
.timeline-day-head { padding: 12px 0 8px; }
.timeline-date { font-size: 13px; font-weight: 600; color: var(--color-text-muted); }
.timeline-day-items { display: flex; flex-direction: column; }

.timeline-item { display: flex; gap: 12px; padding: 12px 0; border-bottom: 1px solid var(--color-border-light, var(--color-border)); }
.timeline-item:last-child { border-bottom: none; }

.timeline-time { font-size: 12px; color: var(--color-text-muted); min-width: 40px; padding-top: 2px; font-family: monospace; }
.timeline-rail { display: flex; flex-direction: column; align-items: center; padding-top: 6px; }
.timeline-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--color-border); flex-shrink: 0; }

.timeline-card { flex: 1; min-width: 0; }
.timeline-card-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.timeline-head-left { display: flex; align-items: center; gap: 8px; }
.timeline-source { font-size: 11px; color: var(--color-text-muted); }
.timeline-cat { font-size: 10px; padding: 1px 5px; border-radius: 3px; background: var(--color-background-secondary); color: var(--color-text-muted); }

.timeline-title { font-size: 14px; font-weight: 600; margin: 0 0 4px; line-height: 1.5; }
.timeline-title a { color: var(--color-text); text-decoration: none; }
.timeline-title a:hover { color: var(--color-primary); }

.timeline-summary { font-size: 12px; color: var(--color-text-secondary); margin: 0 0 6px; line-height: 1.5; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

.timeline-footer-row { display: flex; justify-content: flex-end; }
.create-btn { font-size: 12px; padding: 0; }

.feed-pagination { text-align: center; margin-top: 24px; }

@media (max-width: 768px) {
  .feed-page { padding: 16px; }
  .feed-toolbar-row { flex-direction: column; align-items: stretch; }
  .timeline-time { display: none; }
  .timeline-rail { display: none; }
}
</style>