import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

describe('Asset Report View', () => {
  const reportSource = readFileSync(resolve(process.cwd(), 'src/views/asset/report/index.vue'), 'utf8')

  it('displays business type document status workflow status and archive status columns', () => {
    expect(reportSource).toContain('businessType')
    expect(reportSource).toContain('status')
    expect(reportSource).toContain('wfStatus')
    expect(reportSource).toContain('archiveStatus')
  })
})
