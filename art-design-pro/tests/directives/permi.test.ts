import { describe, it, expect } from 'vitest'
import { hasPermi } from '../../src/directives/business/permi'

describe('hasPermi Directive', () => {
  it('should expose mounted hook', () => {
    expect(hasPermi.mounted).toBeDefined()
  })
})
