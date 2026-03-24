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
  entryTitle: string
  entryDescription: string
  workflowTitle: string
  workflowLabel: string
  workflowDescription: string
  primaryActionLabel: string
  primaryActionDescription: string
  secondaryActionLabel: string
  secondaryActionDescription: string
  returnRoute?: {
    path: string
    query: Record<string, string>
  }
}

export type DisposalSummaryBarContext = {
  currentViewLabel: string
  currentIntentLabel: string
  workflowLabel: string
  workflowDescription: string
  nextStepSuggestion: string
  primaryActionLabel: string
  primaryActionDescription: string
  secondaryActionLabel: string
  secondaryActionDescription: string
  refreshActionLabel: string
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
  const sourceDescription = '不动产详情壳只负责发起与回看，正式处置流程仍在统一资产处置页继续办理。'

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
      ? '先查看该资产的处置记录、审批轨迹和最近责任归口，再决定是否返回不动产详情继续回看。'
      : intent === 'start'
        ? '先确认该资产是否在待处置资产池中，再继续发起处置或补齐处置资料。'
        : '按需查看处置记录或继续办理资产处置。'

  const entryTitle =
    intent === 'view' ? '处置闭环回看入口' : intent === 'start' ? '处置闭环办理入口' : '处置闭环入口'

  const entryDescription =
    intent === 'view'
      ? '当前更适合先回看该资产的处置记录、审批结果和责任归口。'
      : intent === 'start'
        ? '当前更适合继续进入待处置资产池，确认该资产的处置办理入口。'
        : '当前入口已承接不动产详情壳传入的资产上下文。'

  const primaryActionLabel = preferredTab === 'record' ? '查看处置记录' : '进入待处置资产池'
  const primaryActionDescription =
    preferredTab === 'record'
      ? '继续核对该资产的处置记录、审批进展和责任归口。'
      : '继续进入待处置资产池，发起或补齐该资产的处置流程。'

  const workflowTitle = '当前办理视图'
  const workflowLabel = preferredTab === 'record' ? '处置记录回看' : '待处置资产池办理'
  const workflowDescription =
    preferredTab === 'record'
      ? '当前更适合回看该资产的处置记录、审批状态和责任归口，再决定是否继续办理。'
      : '当前更适合在待处置资产池中确认该资产并继续办理处置流程。'

  const secondaryActionLabel = preferredTab === 'record' ? '去待处置资产池' : '查看处置记录'
  const secondaryActionDescription =
    preferredTab === 'record'
      ? '如需继续推进该资产处置，可切到待处置资产池继续办理。'
      : '如需回看审批和确认结果，可切到处置记录查看进展。'

  return {
    hasSource,
    source,
    sourceLabel,
    sourceDescription,
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
    entryTitle,
    entryDescription,
    workflowTitle,
    workflowLabel,
    workflowDescription,
    primaryActionLabel,
    primaryActionDescription,
    secondaryActionLabel,
    secondaryActionDescription,
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

export function buildDisposalActiveViewContext(
  context: DisposalSourceContext,
  activeTab: 'pool' | 'record'
): DisposalSourceContext {
  const preferredTabLabel = activeTab === 'record' ? '处置记录' : '待处置资产池'
  const scopeDescription =
    activeTab === 'record'
      ? `当前已按资产 ${context.assetCode || '-'} 锁定处置记录视图。`
      : `当前已按资产 ${context.assetCode || '-'} 锁定待处置资产池视图。`

  const entryDescription =
    activeTab === 'record'
      ? '当前更适合先回看该资产的处置记录、审批结果和责任归口。'
      : '当前更适合继续进入待处置资产池，确认该资产的处置办理入口。'

  const nextStepSuggestion =
    activeTab === 'record'
      ? '先核对该资产的处置记录、审批轨迹和责任归口，再决定是否继续办理。'
      : '先确认该资产是否在待处置资产池中，再继续发起处置或补齐处置资料。'

  const workflowLabel = activeTab === 'record' ? '处置记录回看' : '待处置资产池办理'
  const workflowDescription =
    activeTab === 'record'
      ? '当前更适合回看该资产的处置记录、审批状态和责任归口，再决定是否继续办理。'
      : '当前更适合在待处置资产池中确认该资产并继续办理处置流程。'

  const primaryActionLabel = activeTab === 'record' ? '查看处置记录' : '进入待处置资产池'
  const primaryActionDescription =
    activeTab === 'record'
      ? '继续核对该资产的处置记录、审批进展和责任归口。'
      : '继续进入待处置资产池，发起或补齐该资产的处置流程。'

  const secondaryActionLabel = activeTab === 'record' ? '去待处置资产池' : '查看处置记录'
  const secondaryActionDescription =
    activeTab === 'record'
      ? '如需继续推进该资产处置，可切到待处置资产池继续办理。'
      : '如需回看审批和确认结果，可切到处置记录查看进展。'

  return {
    ...context,
    preferredTab: activeTab,
    preferredTabLabel,
    scopeDescription,
    entryDescription,
    nextStepSuggestion,
    workflowLabel,
    workflowDescription,
    primaryActionLabel,
    primaryActionDescription,
    secondaryActionLabel,
    secondaryActionDescription
  }
}

export function buildDisposalSummaryBarContext(
  context: DisposalSourceContext
): DisposalSummaryBarContext {
  const genericWorkflowDescription =
    context.preferredTab === 'record'
      ? '当前可回看处置记录、审批状态和责任归口，并判断是否需要继续办理。'
      : '当前可在待处置资产池中确认资产，并继续办理处置流程。'

  const genericNextStepSuggestion =
    context.preferredTab === 'record'
      ? '可按处置单号、类型或日期筛选记录，并继续核对审批进展。'
      : '可先筛选待处置资产，再继续确认处置或切回记录页回看。'

  return {
    currentViewLabel: context.preferredTabLabel,
    // 中文注释：没有来源时不展示意图标签，避免把通用入口误写成联动场景。
    currentIntentLabel: context.hasSource ? context.intentLabel : '',
    workflowLabel: context.workflowLabel,
    workflowDescription: context.hasSource
      ? context.workflowDescription
      : genericWorkflowDescription,
    nextStepSuggestion: context.hasSource
      ? context.nextStepSuggestion
      : genericNextStepSuggestion,
    primaryActionLabel: context.primaryActionLabel,
    primaryActionDescription: context.primaryActionDescription,
    secondaryActionLabel: context.secondaryActionLabel,
    secondaryActionDescription: context.secondaryActionDescription,
    refreshActionLabel: context.preferredTab === 'record' ? '刷新记录' : '刷新资产池'
  }
}
