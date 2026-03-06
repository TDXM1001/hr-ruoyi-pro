import App from './App.vue'
import { createApp } from 'vue'
import { initStore } from './store'                 // Store
import { initRouter } from './router'               // Router
import language from './locales'                    // 国际化
import '@styles/core/tailwind.css'                  // tailwind
import '@styles/index.scss'                         // 样式
import '@utils/sys/console.ts'                      // 控制台输出内容
import { setupGlobDirectives } from './directives'
import { setupErrorHandle } from './utils/sys/error-handle'
import DictTag from '@/components/DictTag/index.vue'
import { hasPermi } from '@/directives/business/permi'
import { hasRole } from '@/directives/business/role'

document.addEventListener(
  'touchstart',
  function () { },
  { passive: false }
)

const app = createApp(App)
initStore(app)
initRouter(app)
setupGlobDirectives(app)
setupErrorHandle(app)

// 注册全局字典标签组件
app.component('DictTag', DictTag)

// 注册权限指令
app.directive('hasPermi', hasPermi)
app.directive('hasRole', hasRole)

app.use(language)
app.mount('#app')