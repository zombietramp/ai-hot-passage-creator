<template>
  <aside class="sidebar" :class="{ collapsed: collapsed }">
    <div class="sidebar-brand">
      <RouterLink to="/" class="brand-link">
        <span class="brand-logo-ai">AI</span>
        <span class="brand-logo-hot">HOT</span>
      </RouterLink>
      <button class="sidebar-toggle" @click="collapsed = !collapsed" aria-label="收起导航">
        <MenuFoldOutlined v-if="!collapsed" />
        <MenuUnfoldOutlined v-else />
      </button>
    </div>

    <nav class="side-nav">
      <RouterLink
        v-for="item in visibleItems"
        :key="item.key"
        :to="item.key"
        :class="['side-link', { active: isActive(item.key) }]"
        :data-tooltip="item.label"
      >
        <component :is="item.icon" class="side-icon" />
        <span class="side-label">{{ item.label }}</span>
      </RouterLink>
    </nav>

    <div class="sidebar-footer">
      <a class="side-link theme-toggle-link" @click="toggleTheme">
        <BulbOutlined v-if="isDark" class="side-icon" />
        <BulbFilled v-else class="side-icon" />
        <span class="side-label">{{ isDark ? '浅色' : '深色' }}</span>
      </a>

      <template v-if="loginUserStore.loginUser.id">
        <div class="user-block">
          <a-avatar :src="loginUserStore.loginUser.userAvatar" :size="32" class="user-avatar" />
          <span v-if="!collapsed" class="user-name">{{ loginUserStore.loginUser.userName ?? '无名' }}</span>
        </div>
        <a v-if="!collapsed" class="side-link logout-link" @click="doLogout">
          <LogoutOutlined class="side-icon" />
          <span class="side-label">退出</span>
        </a>
      </template>
      <RouterLink v-else to="/user/login" class="side-link login-link">
        <LoginOutlined class="side-icon" />
        <span class="side-label">登录</span>
      </RouterLink>
    </div>
  </aside>

  <!-- Mobile bottom bar -->
  <div class="mobile-bar">
    <RouterLink
      v-for="item in mobileItems"
      :key="item.key"
      :to="item.key"
      :class="['mobile-link', { active: isActive(item.key) }]"
    >
      <component :is="item.icon" class="mobile-icon" />
      <span class="mobile-label">{{ item.label }}</span>
    </RouterLink>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { userLogout } from '@/api/userController'
import {
  HomeOutlined,
  FireOutlined,
  EditOutlined,
  UnorderedListOutlined,
  SettingOutlined,
  BarChartOutlined,
  CloudDownloadOutlined,
  LogoutOutlined,
  LoginOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  OrderedListOutlined,
  ReadOutlined,
  BulbOutlined,
  BulbFilled,
} from '@ant-design/icons-vue'

const loginUserStore = useLoginUserStore()
const router = useRouter()
const route = useRoute()
const collapsed = ref(false)

const originItems = [
  { key: '/hot', icon: FireOutlined, label: '精选' },
  { key: '/all', icon: OrderedListOutlined, label: '全部 AI 动态' },
  { key: '/daily', icon: ReadOutlined, label: 'AI 日报' },
  { key: '/create', icon: EditOutlined, label: '创作' },
  { key: '/article/list', icon: UnorderedListOutlined, label: '历史' },
  { key: '/admin/userManage', icon: SettingOutlined, label: '管理', admin: true },
  { key: '/admin/crawlSource', icon: CloudDownloadOutlined, label: '抓取源', admin: true },
  { key: '/admin/statistics', icon: BarChartOutlined, label: '数据', admin: true },
]

const visibleItems = computed(() => {
  return originItems.filter((item) => {
    if ((item as any).admin) {
      const loginUser = loginUserStore.loginUser
      return loginUser && loginUser.userRole === 'admin'
    }
    return true
  })
})

const mobileItems = computed(() => {
  return originItems.filter((item) => !(item as any).admin).slice(0, 4)
})

const isActive = (key: string) => {
  if (key === '/') return route.path === '/'
  return route.path.startsWith(key)
}

const isDark = ref(false)

const initTheme = () => {
  const saved = localStorage.getItem('theme')
  isDark.value = saved === 'dark'
  applyTheme()
}

const toggleTheme = () => {
  isDark.value = !isDark.value
  localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
  applyTheme()
}

const applyTheme = () => {
  document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : 'light')
}

onMounted(initTheme)

const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({ userName: '未登录' })
    message.success('已退出')
    await router.push('/user/login')
  }
}
</script>

<!-- PLACEHOLDER_SIDEBAR_STYLE -->

<style scoped>
.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: 160px;
  background: var(--color-background);
  border-right: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  z-index: 100;
  transition: width 0.2s ease;
  overflow: hidden;
}

.sidebar.collapsed {
  width: 64px;
}

.sidebar.collapsed .side-label,
.sidebar.collapsed .user-name,
.sidebar.collapsed .brand-logo-hot {
  display: none;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 16px;
  border-bottom: 1px solid var(--color-border-light);
}

.brand-link {
  display: flex;
  align-items: center;
  gap: 2px;
  text-decoration: none;
  font-size: 20px;
  font-weight: 800;
  letter-spacing: -0.5px;
}

.brand-logo-ai {
  color: var(--color-text);
}

.brand-logo-hot {
  color: #ff4d4f;
}

.sidebar-toggle {
  background: none;
  border: none;
  cursor: pointer;
  color: var(--color-text-secondary);
  font-size: 16px;
  padding: 4px;
  border-radius: var(--radius-sm);
  transition: all var(--transition-fast);
}

.sidebar-toggle:hover {
  background: var(--color-background-secondary);
  color: var(--color-text);
}

.side-nav {
  flex: 1;
  padding: 12px 8px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow-y: auto;
}

.side-link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: var(--radius-md);
  color: var(--color-text-secondary);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all var(--transition-fast);
  white-space: nowrap;
}

.side-link:hover {
  background: var(--color-background-secondary);
  color: var(--color-text);
}

.side-link.active {
  background: rgba(34, 197, 94, 0.1);
  color: var(--color-primary-dark);
  font-weight: 600;
}

.side-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.sidebar-footer {
  padding: 12px 8px;
  border-top: 1px solid var(--color-border-light);
}

.user-block {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  margin-bottom: 4px;
}

.user-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
}

.logout-link {
  cursor: pointer;
}

.login-link {
  color: var(--color-primary);
}

.theme-toggle-link {
  cursor: pointer;
  margin-bottom: 8px;
}

/* Mobile bar */
.mobile-bar {
  display: none;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 56px;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  border-top: 1px solid var(--color-border);
  z-index: 100;
  justify-content: space-around;
  align-items: center;
}

.mobile-link {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  color: var(--color-text-secondary);
  text-decoration: none;
  font-size: 11px;
  padding: 4px 12px;
  border-radius: var(--radius-sm);
  transition: color var(--transition-fast);
}

.mobile-link.active {
  color: var(--color-primary-dark);
}

.mobile-icon {
  font-size: 20px;
}

@media (max-width: 768px) {
  .sidebar {
    display: none;
  }

  .mobile-bar {
    display: flex;
  }
}
</style>
