import type { App } from 'vue'
import type { Router } from 'vue-router'

export async function mountAppAfterRouterReady(
  app: App<Element>,
  router: Router,
  selector = '#app'
) {
  // 等待路由完成首次解析，避免直链场景先闪出 404 再回正。
  await router.isReady()
  app.mount(selector)
}
