<template>
  <div
    :data-testid="rootTestId"
    class="disposal-summary-bar"
    :class="{ 'is-compact': compact }"
  >
    <div class="disposal-summary-bar__content">
      <div class="disposal-summary-bar__meta">
        <ElTag type="primary" effect="light" size="small">
          {{ context.currentViewLabel }}
        </ElTag>
        <ElTag
          v-if="context.currentIntentLabel"
          type="success"
          effect="light"
          size="small"
        >
          {{ context.currentIntentLabel }}
        </ElTag>
        <span class="disposal-summary-bar__meta-text">办理摘要条</span>
      </div>

      <div :data-testid="workflowTestId" class="disposal-summary-bar__workflow">
        <span class="disposal-summary-bar__workflow-label">当前办理视图</span>
        <strong>{{ context.workflowLabel }}</strong>
        <p>{{ context.workflowDescription }}</p>
      </div>

      <div :data-testid="nextStepTestId" class="disposal-summary-bar__next-step">
        <span>下一步建议</span>
        <p>{{ context.nextStepSuggestion }}</p>
      </div>
    </div>

    <!-- 中文注释：搜索区只展示摘要信息，操作按钮只在入口卡和 tab 头部保留。 -->
    <div v-if="showActions" class="disposal-summary-bar__actions">
      <div v-if="!compact" class="disposal-summary-bar__action-copy">
        <div class="disposal-summary-bar__action-title">{{ context.primaryActionLabel }}</div>
        <div class="disposal-summary-bar__action-desc">
          {{ context.primaryActionDescription }}
        </div>
      </div>

      <div class="disposal-summary-bar__buttons">
        <ElButton
          :data-testid="secondaryActionTestId"
          plain
          @click="$emit('secondary-action')"
        >
          {{ context.secondaryActionLabel }}
        </ElButton>
        <ElButton
          :data-testid="primaryActionTestId"
          :type="primaryActionType"
          @click="$emit('primary-action')"
        >
          {{ context.primaryActionLabel }}
        </ElButton>
        <ElButton
          :data-testid="refreshActionTestId"
          text
          type="primary"
          icon="ri:refresh-line"
          @click="$emit('refresh-action')"
        >
          {{ context.refreshActionLabel }}
        </ElButton>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import type { DisposalSummaryBarContext } from '../disposal-source-context'

  const props = withDefaults(
    defineProps<{
      context: DisposalSummaryBarContext
      compact?: boolean
      showActions?: boolean
      rootTestId: string
      workflowTestId: string
      nextStepTestId: string
      primaryActionTestId: string
      secondaryActionTestId: string
      refreshActionTestId: string
    }>(),
    {
      compact: false,
      showActions: true
    }
  )

  defineEmits<{
    'primary-action': []
    'secondary-action': []
    'refresh-action': []
  }>()

  const primaryActionType = computed(() => {
    return props.context.currentViewLabel === '处置记录' ? 'success' : 'primary'
  })
</script>

<style scoped lang="scss">
  .disposal-summary-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding: 14px 16px;
    border-radius: 14px;
    border: 1px solid rgba(47, 102, 255, 0.16);
    background: linear-gradient(180deg, rgba(248, 251, 255, 0.98), rgba(255, 255, 255, 0.96));
  }

  .disposal-summary-bar__content {
    display: flex;
    flex: 1;
    flex-direction: column;
    gap: 8px;
    min-width: 0;
  }

  .disposal-summary-bar__meta {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
  }

  .disposal-summary-bar__meta-text {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .disposal-summary-bar__workflow {
    .disposal-summary-bar__workflow-label {
      display: block;
      margin-bottom: 4px;
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }

    strong {
      display: block;
      margin-bottom: 4px;
      font-size: 15px;
      color: var(--el-text-color-primary);
    }

    p {
      margin: 0;
      font-size: 13px;
      line-height: 1.7;
      color: var(--el-text-color-regular);
    }
  }

  .disposal-summary-bar__next-step {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    span {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }

    p {
      margin: 0;
      flex: 1;
      min-width: 0;
      font-size: 12px;
      line-height: 1.7;
      color: var(--el-text-color-regular);
    }
  }

  .disposal-summary-bar__actions {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 12px;
    flex-wrap: wrap;
  }

  .disposal-summary-bar__action-copy {
    max-width: 220px;
  }

  .disposal-summary-bar__action-title {
    margin-bottom: 4px;
    font-size: 13px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .disposal-summary-bar__action-desc {
    font-size: 12px;
    line-height: 1.6;
    color: var(--el-text-color-secondary);
  }

  .disposal-summary-bar__buttons {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 8px;
    flex-wrap: wrap;
  }

  .disposal-summary-bar.is-compact {
    padding: 12px 14px;

    .disposal-summary-bar__workflow {
      strong {
        font-size: 14px;
      }

      p {
        font-size: 12px;
      }
    }
  }

  @media (max-width: 768px) {
    .disposal-summary-bar,
    .disposal-summary-bar__actions {
      flex-direction: column;
      align-items: stretch;
    }

    .disposal-summary-bar__buttons {
      justify-content: stretch;
    }
  }
</style>
