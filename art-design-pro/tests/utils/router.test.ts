import { describe, it, expect } from 'vitest'
import { generateRoutes } from '../../src/utils/router'

describe('Router Utils', () => {
  it('should transform backend menu data to vue-router format', () => {
    const backendData = [{ path: '/system', component: 'Layout', children: [] }]
    const routes = generateRoutes(backendData)
    expect(routes[0].path).toBe('/system')
    // 断言 component 发生映射解析
  })
})
