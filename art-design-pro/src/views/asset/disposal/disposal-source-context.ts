export type DisposalSourceIntent = 'start' | 'view' | ''

export type DisposalSourceContext = {
  hasSource: boolean
  source: string
  sourceLabel: string
  sourceDescription: string
  intent: DisposalSourceIntent
  intentLabel: string
  intentDescription: string
  assetId?: number
  assetCode: string
  assetName: string
  preferredTab: 'pool' | 'record'
  preferredTabLabel: string
  scopeTitle: string
  scopeDescription: string
  nextStepSuggestion: string
  returnRoute?: {
    path: string
    query: Record<string, string>
  }
}

const readString = (value: unknown) => {
  return typeof value === 'string' ? value : ''
}

const readTab = (value: unknown) => {
  return value === 'record' ? 'record' : value === 'pool' ? 'pool' : ''
}

const readIntent = (value: unknown): DisposalSourceIntent => {
  return value === 'start' || value === 'view' ? value : ''
}

export function buildDisposalSourceContext(query: Record<string, unknown>): DisposalSourceContext {
  const source = readString(query.source)
  const assetId = Number(query.assetId || 0) || undefined
  const assetCode = readString(query.assetCode)
  const assetName = readString(query.assetName)
  const explicitTab = readTab(query.tab)
  const intent = readIntent(query.intent)
  const hasSource = source === 'real-estate-disposal-tab'

  const preferredTab: 'pool' | 'record' =
    explicitTab || (intent === 'view' ? 'record' : 'pool')

  const preferredTabLabel = preferredTab === 'record' ? '处置记录' : '待处置资产池'

  const sourceLabel = hasSource ? '来自不动产档案处置联动' : ''
  const intentLabel =
    intent === 'view' ? '查看处置进展' : intent === 'start' ? '发起处置' : '处置联动'
  const intentDescription =
    intent === 'view'
      ? '当前进入的是处置记录视图，可先核对该资产的处置记录与审批进展。'
      : intent === 'start'
        ? '当前进入的是处置办理视图，请继续发起或补齐处置流程。'
        : '当前来源于不动产详情壳的处置联动入口。'

  const scopeTitle = '当前锁定范围'
  const scopeDescription =
    preferredTab === 'record'
      ? `当前已按资产 ${assetCode || '-'} 锁定处置记录视图。`
      : `当前已按资产 ${assetCode || '-'} 锁定待处置资产池视图。`
  const nextStepSuggestion =
    intent === 'view'
      ? '请先核对该资产的处置记录、审批进展和最近责任归口，再决定是否返回不动产详情继续回看。'
      : intent === 'start'
        ? '请先确认该资产是否出现在待处置资产池，再继续提交处置审批或补齐处置资料。'
        : '当前来源于不动产详情壳的处置联动入口，可按需查看记录或继续办理。'

  return {
    hasSource,
    source,
    sourceLabel,
    sourceDescription: '不动产详情壳只负责发起与回看，正式流程仍在统一资产处置页继续办理。',
    intent,
    intentLabel,
    intentDescription,
    assetId,
    assetCode,
    assetName,
    preferredTab,
    preferredTabLabel,
    scopeTitle,
    scopeDescription,
    nextStepSuggestion,
    returnRoute: assetId
      ? {
          path: `/asset/real-estate/detail/${assetId}`,
          query: {
            tab: 'disposal',
            from: 'asset-disposal'
          }
        }
      : undefined
  }
}
