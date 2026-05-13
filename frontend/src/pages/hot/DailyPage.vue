<template>
  <div class="daily-layout">
    <aside class="daily-side">
      <div class="daily-side-latest">
        <div class="daily-side-latest-label">最新一期</div>
        <div class="daily-side-latest-date">{{ dailyList[0]?.date || '' }}</div>
      </div>

      <div class="daily-side-archive">
        <div v-for="monthGroup in monthGroups" :key="monthGroup.label" class="daily-side-month">
          <div class="daily-side-month-head">
            <span class="daily-side-month-name">{{ monthGroup.label }}</span>
            <span class="daily-side-month-count">{{ monthGroup.items.length }}</span>
          </div>
          <div class="daily-side-day-list">
            <div
              v-for="d in monthGroup.items"
              :key="d.date"
              :class="['daily-side-day', { 'is-active': d.date === selectedDate }]"
              @click="goDate(d.date)"
            >
              <span class="daily-side-day-num">{{ parseInt(d.date.slice(-2)) }} 日</span>
              <span class="daily-side-day-headline">{{ d.leadTitle }}</span>
            </div>
          </div>
        </div>
      </div>
    </aside>

    <main class="daily-main">
      <a-spin :spinning="loading">
        <header class="daily-masthead">
          <div class="daily-masthead-meta">
            <span>VOL.{{ volLabel }}</span>
            <span class="daily-masthead-meta-rule"></span>
            <span>{{ totalItems }} STORIES</span>
            <span class="daily-masthead-meta-rule"></span>
            <span>AI HOT DAILY</span>
          </div>

          <h1 class="daily-masthead-title">
            <span class="title-ai">AI</span>
            <span class="title-hot">HOT</span>
            <span class="title-zh">日报</span>
          </h1>

          <div class="daily-masthead-tagline">
            <span class="tagline-date">{{ chineseDate }}</span>
            <span class="tagline-weekday">{{ weekdayLabel }}</span>
            <span class="tagline-spacer"></span>
            <span class="tagline-badge">DAILY · 每早八时</span>
          </div>
        </header>

        <div v-if="daily && daily.sections" class="daily-sections">
          <section v-for="(section, idx) in daily.sections" :key="section.label" class="daily-section">
            <header class="daily-section-header">
              <div class="daily-section-no">{{ String(idx + 1).padStart(2, '0') }}</div>
              <h2 class="daily-section-title">{{ section.label }}</h2>
              <span class="daily-section-subtitle">{{ sectionEnLabel(section.label) }}</span>
              <div class="daily-section-count"><strong>{{ section.items?.length || 0 }}</strong> 篇</div>
            </header>
            <div class="daily-section-articles">
              <article v-for="item in section.items" :key="item.sourceUrl" class="daily-article">
                <h3 class="daily-article-title">
                  <a :href="item.sourceUrl" target="_blank" rel="noopener">{{ item.title }}</a>
                </h3>
                <div class="daily-article-source">
                  <span class="role-tag">{{ inferRoleTag(item.sourceName) }}</span>
                  <span class="source-name">{{ item.sourceName }}</span>
                </div>
                <p class="daily-article-summary">{{ item.summary }}</p>
                <div class="daily-article-actions">
                  <a-button type="link" size="small" @click="goCreate(item)">
                    <EditOutlined /> 一键创作
                  </a-button>
                </div>
              </article>
            </div>
          </section>
        </div>

        <nav class="daily-prev-next" v-if="dailyList.length > 1">
          <a v-if="prevDate" @click="goDate(prevDate)">← 前一日</a>
          <span v-else style="opacity: 0.3">← 前一日</span>
          <a v-if="nextDate" @click="goDate(nextDate)">后一日 →</a>
          <span v-else style="opacity: 0.3">后一日 →</span>
        </nav>

        <footer class="daily-footer">AI HOT · 编辑系统自动生成</footer>
        <a-empty v-if="!loading && !daily" description="暂无日报数据" />
      </a-spin>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { EditOutlined } from '@ant-design/icons-vue'
import { fetchDaily, fetchDailyByDate, fetchDailies } from '@/api/hotController'
import dayjs from 'dayjs'

const router = useRouter()
const loading = ref(false)
const daily = ref<any>(null)
const dailyList = ref<any[]>([])
const selectedDate = ref('')

const dayNum = computed(() => selectedDate.value ? parseInt(selectedDate.value.slice(-2)) : '')
const volLabel = computed(() => selectedDate.value?.replace(/-/g, '.') || '')
const chineseDate = computed(() => {
  if (!selectedDate.value) return ''
  const d = dayjs(selectedDate.value)
  const numMap = ['零','一','二','三','四','五','六','七','八','九','十']
  const y = selectedDate.value.slice(0, 4)
  const yStr = y.split('').map((c: string) => numMap[parseInt(c)]).join('')
  const m = d.month() + 1
  const day = d.date()
  const mStr = m <= 10 ? numMap[m] : `十${numMap[m - 10]}`
  const dStr = day <= 10 ? numMap[day] : (day < 20 ? `十${numMap[day - 10]}` : (day === 20 ? '二十' : (day < 30 ? `二十${numMap[day - 20]}` : `三十${numMap[day - 30]}`)))
  return `${yStr}年${mStr}月${dStr}日`
})
const weekdayLabel = computed(() => {
  if (!selectedDate.value) return ''
  const weekdays = ['星期日','星期一','星期二','星期三','星期四','星期五','星期六']
  return weekdays[dayjs(selectedDate.value).day()]
})

const totalItems = computed(() => {
  if (!daily.value?.sections) return 0
  return daily.value.sections.reduce((sum: number, s: any) => sum + (s.items?.length || 0), 0)
})

const currentIdx = computed(() => dailyList.value.findIndex(d => d.date === selectedDate.value))
const prevDate = computed(() => dailyList.value[currentIdx.value + 1]?.date || null)
const nextDate = computed(() => currentIdx.value > 0 ? dailyList.value[currentIdx.value - 1]?.date : null)

const monthGroups = computed(() => {
  const groups: Record<string, { label: string; items: any[] }> = {}
  for (const d of dailyList.value) {
    const key = d.date.slice(0, 7)
    const [y, m] = key.split('-')
    if (!groups[key]) { groups[key] = { label: `${y} 年 ${parseInt(m)} 月`, items: [] } }
    groups[key].items.push(d)
  }
  return Object.values(groups)
})

const sectionEnLabel = (label: string) => {
  const map: Record<string, string> = {
    '模型发布/更新': 'MODEL RELEASES',
    '产品发布/更新': 'PRODUCT',
    '行业动态': 'INDUSTRY',
    '论文研究': 'RESEARCH',
    '技巧与观点': 'TIPS & TAKES',
  }
  return map[label] || ''
}

const inferRoleTag = (sourceName: string) => {
  if (!sourceName) return ''
  if (sourceName.includes('官方') || sourceName.includes('Official')) return '官方'
  if (sourceName.includes('X：')) return 'X'
  if (sourceName.includes('RSS')) return 'RSS'
  return '综合'
}

const loadDailyList = async () => {
  const res = await fetchDailies({ take: 30 })
  if (res.data.code === 0 && res.data.data) {
    const parsed = typeof res.data.data === 'string' ? JSON.parse(res.data.data) : res.data.data
    dailyList.value = parsed.items || []
    if (dailyList.value.length > 0 && !selectedDate.value) {
      selectedDate.value = dailyList.value[0].date
    }
  }
}

const loadDaily = async () => {
  loading.value = true
  try {
    const res = selectedDate.value
      ? await fetchDailyByDate(selectedDate.value)
      : await fetchDaily()
    if (res.data.code === 0 && res.data.data) {
      daily.value = typeof res.data.data === 'string' ? JSON.parse(res.data.data) : res.data.data
      if (daily.value?.date) selectedDate.value = daily.value.date
    }
  } finally { loading.value = false }
}

const goDate = (date: string) => { selectedDate.value = date; loadDaily() }
const goCreate = (item: any) => { router.push({ path: '/create', query: { topic: item.title } }) }

onMounted(async () => { await loadDailyList(); await loadDaily() })
</script>

<!-- DAILY_STYLE -->

<style scoped>
.daily-layout { display: flex; min-height: 100vh; }

.daily-side {
  width: 240px; flex-shrink: 0; padding: 24px 16px;
  border-right: 1px solid var(--color-border);
  overflow-y: auto; position: sticky; top: 0; height: 100vh;
}

.daily-side-latest { margin-bottom: 20px; padding-bottom: 16px; border-bottom: 1px solid var(--color-border); }
.daily-side-latest-label { font-size: 11px; font-weight: 600; color: var(--color-primary); background: rgba(34,197,94,0.1); display: inline-block; padding: 2px 8px; border-radius: 3px; margin-bottom: 4px; }
.daily-side-latest-date { font-size: 13px; color: var(--color-text-secondary); }

.daily-side-month { margin-bottom: 16px; }
.daily-side-month-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.daily-side-month-name { font-size: 13px; font-weight: 600; color: var(--color-text); }
.daily-side-month-count { font-size: 11px; color: var(--color-text-muted); }

.daily-side-day-list { display: flex; flex-direction: column; gap: 1px; }
.daily-side-day { display: flex; align-items: flex-start; gap: 8px; padding: 6px 8px; border-radius: var(--radius-sm); cursor: pointer; transition: background 0.15s; }
.daily-side-day:hover { background: var(--color-background-secondary); }
.daily-side-day.is-active { background: rgba(34,197,94,0.08); border-left: 2px solid var(--color-primary); }
.daily-side-day-num { font-size: 12px; font-weight: 600; color: var(--color-text); min-width: 28px; }
.daily-side-day-headline { font-size: 11px; color: var(--color-text-secondary); line-height: 1.4; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

.daily-main { flex: 1; padding: 40px 48px; max-width: 800px; }

.daily-masthead { text-align: center; margin-bottom: 40px; padding-bottom: 32px; border-bottom: 1px solid var(--color-border); }
.daily-masthead-meta { display: flex; justify-content: center; align-items: center; gap: 12px; font-size: 11px; color: var(--color-text-muted); text-transform: uppercase; letter-spacing: 1.5px; margin-bottom: 20px; }
.daily-masthead-meta-rule { width: 1px; height: 12px; background: var(--color-border); }

.daily-masthead-title { font-size: 56px; font-weight: 900; margin: 0 0 16px; line-height: 1.1; letter-spacing: -2px; }
.title-ai { color: var(--color-text); }
.title-hot { color: var(--color-primary); margin-left: 4px; }
.title-zh { color: var(--color-text); margin-left: 8px; font-size: 48px; }

.daily-masthead-tagline { display: flex; justify-content: center; align-items: center; gap: 16px; font-size: 14px; color: var(--color-text-secondary); }
.tagline-spacer { flex: 1; max-width: 40px; }
.tagline-badge { font-size: 11px; color: var(--color-text-muted); letter-spacing: 1px; }

.daily-sections { }

.daily-section { margin-bottom: 36px; }
.daily-section-header { display: flex; align-items: baseline; gap: 12px; margin-bottom: 18px; padding-bottom: 12px; border-bottom: 2px solid var(--color-primary); }
.daily-section-no { font-size: 28px; font-weight: 900; color: var(--color-primary); }
.daily-section-title { font-size: 20px; font-weight: 700; margin: 0; }
.daily-section-subtitle { font-size: 11px; color: var(--color-text-muted); text-transform: uppercase; letter-spacing: 1px; }
.daily-section-count { font-size: 12px; color: var(--color-text-muted); margin-left: auto; }

.daily-section-articles { display: flex; flex-direction: column; gap: 20px; }

.daily-article { padding-bottom: 20px; border-bottom: 1px solid var(--color-border-light, var(--color-border)); }
.daily-article:last-child { border-bottom: none; padding-bottom: 0; }
.daily-article-title { font-size: 16px; font-weight: 700; margin: 0 0 8px; line-height: 1.5; }
.daily-article-title a { color: var(--color-text); text-decoration: none; }
.daily-article-title a:hover { color: var(--color-primary); }
.daily-article-source { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.role-tag { font-size: 10px; font-weight: 600; padding: 1px 5px; border-radius: 3px; background: var(--color-background-secondary); color: var(--color-text-muted); }
.source-name { font-size: 12px; color: var(--color-text-muted); }
.daily-article-summary { font-size: 14px; color: var(--color-text-secondary); margin: 0 0 10px; line-height: 1.8; }
.daily-article-actions { display: flex; justify-content: flex-end; }

.daily-prev-next { display: flex; justify-content: center; gap: 24px; font-size: 13px; margin-top: 32px; padding-top: 20px; border-top: 1px solid var(--color-border); }
.daily-prev-next a { color: var(--color-primary); cursor: pointer; }
.daily-prev-next a:hover { text-decoration: underline; }

.daily-footer { text-align: center; font-size: 12px; color: var(--color-text-muted); margin-top: 32px; }

@media (max-width: 768px) {
  .daily-layout { flex-direction: column; }
  .daily-side { width: 100%; height: auto; position: static; border-right: none; border-bottom: 1px solid var(--color-border); padding: 16px; }
  .daily-main { padding: 24px 16px; }
  .daily-masthead-title { font-size: 36px; }
  .title-zh { font-size: 30px; }
}
</style>