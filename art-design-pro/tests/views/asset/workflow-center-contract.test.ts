import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

describe('Workflow Center Views', () => {
  const todoSource = readFileSync(resolve(process.cwd(), 'src/views/asset/workflow/todo/index.vue'), 'utf8')
  const doneSource = readFileSync(resolve(process.cwd(), 'src/views/asset/workflow/done/index.vue'), 'utf8')

  it('renders unified bizNo bizType and wfStatus fields in todo page', () => {
    expect(todoSource).toContain('bizNo')
    expect(todoSource).toContain('bizType')
    expect(todoSource).toContain('wfStatus')
  })

  it('renders unified bizNo bizType and wfStatus fields in done page', () => {
    expect(doneSource).toContain('bizNo')
    expect(doneSource).toContain('bizType')
    expect(doneSource).toContain('wfStatus')
  })
})
