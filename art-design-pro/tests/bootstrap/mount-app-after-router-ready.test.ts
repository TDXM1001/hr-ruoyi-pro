import { describe, expect, it, vi } from 'vitest'

import { mountAppAfterRouterReady } from '@/bootstrap/mountAppAfterRouterReady'

function createDeferred<T>() {
  let resolve!: (value: T | PromiseLike<T>) => void
  let reject!: (reason?: unknown) => void
  const promise = new Promise<T>((res, rej) => {
    resolve = res
    reject = rej
  })
  return { promise, resolve, reject }
}

describe('mountAppAfterRouterReady', () => {
  it('等待路由 ready 后再挂载应用', async () => {
    const deferred = createDeferred<void>()
    const mount = vi.fn()
    const app = {
      mount
    }
    const router = {
      isReady: vi.fn(() => deferred.promise)
    }

    const mountingPromise = mountAppAfterRouterReady(app as any, router as any)

    expect(router.isReady).toHaveBeenCalledTimes(1)
    expect(mount).not.toHaveBeenCalled()

    deferred.resolve()
    await mountingPromise

    expect(mount).toHaveBeenCalledTimes(1)
    expect(mount).toHaveBeenCalledWith('#app')
  })
})
