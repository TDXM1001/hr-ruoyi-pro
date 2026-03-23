<template>
  <ElCard data-testid="disposal-entry-card" class="disposal-entry-card" shadow="never">
    <div data-testid="disposal-source-context">
      <div class="disposal-entry-card__header">
        <div class="disposal-entry-card__title-wrap">
          <div class="disposal-entry-card__title">{{ context.entryTitle }}</div>
          <div class="disposal-entry-card__desc">{{ context.entryDescription }}</div>
          <div class="disposal-entry-card__sub-desc">{{ context.sourceDescription }}</div>
        </div>
      <ElSpace wrap>
        <ElTag type="success" effect="light">{{ context.intentLabel }}</ElTag>
        <ElTag type="warning" effect="light">{{ context.preferredTabLabel }}</ElTag>
        <ElButton
          v-if="context.returnRoute"
          data-testid="disposal-return-real-estate"
          type="primary"
          link
          @click="$emit('back')"
        >
          返回不动产详情
        </ElButton>
      </ElSpace>
      </div>

      <div class="disposal-entry-card__grid">
        <div class="disposal-entry-card__item">
          <span>资产编码</span>
          <strong>{{ context.assetCode || '-' }}</strong>
        </div>
        <div class="disposal-entry-card__item">
          <span>资产名称</span>
          <strong>{{ context.assetName || '-' }}</strong>
        </div>
        <div class="disposal-entry-card__item">
          <span>当前意图</span>
          <strong>{{ context.intentLabel }}</strong>
        </div>
        <div class="disposal-entry-card__item">
          <span>首屏落点</span>
          <strong>{{ context.preferredTabLabel }}</strong>
        </div>
      </div>

      <div class="disposal-entry-card__panels">
        <div data-testid="disposal-source-scope" class="disposal-entry-card__panel">
          <span>{{ context.scopeTitle }}</span>
          <strong>{{ context.preferredTabLabel }}</strong>
          <p>{{ context.scopeDescription }}</p>
        </div>
        <div data-testid="disposal-source-next-step" class="disposal-entry-card__panel">
          <span>下一步建议</span>
          <p>{{ context.nextStepSuggestion }}</p>
        </div>
      </div>

      <div class="disposal-entry-card__actions">
        <div class="disposal-entry-card__action-copy">
          <div class="disposal-entry-card__action-title">{{ context.primaryActionLabel }}</div>
          <div class="disposal-entry-card__action-desc">{{ context.primaryActionDescription }}</div>
        </div>
        <ElButton
          data-testid="disposal-entry-primary-action"
          :type="primaryActionType"
          @click="$emit('primary-action')"
        >
          {{ context.primaryActionLabel }}
        </ElButton>
      </div>
    </div>
  </ElCard>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import type { DisposalSourceContext } from '../disposal-source-context'

  const props = defineProps<{
    context: DisposalSourceContext
  }>()

  defineEmits<{
    back: []
    'primary-action': []
  }>()

  const primaryActionType = computed(() => {
    return props.context.preferredTab === 'record' ? 'success' : 'primary'
  })
</script>

<style scoped lang="scss">
  .disposal-entry-card {
    border: 1px solid rgba(64, 158, 255, 0.18);
    background: linear-gradient(180deg, rgba(236, 245, 255, 0.9), rgba(255, 255, 255, 0.98));
  }

  .disposal-entry-card__header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 12px;
  }

  .disposal-entry-card__title-wrap {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .disposal-entry-card__title {
    font-size: 16px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .disposal-entry-card__desc {
    font-size: 13px;
    line-height: 1.6;
    color: var(--el-text-color-regular);
  }

  .disposal-entry-card__sub-desc {
    font-size: 12px;
    line-height: 1.7;
    color: var(--el-text-color-secondary);
  }

  .disposal-entry-card__grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
    gap: 10px;
  }

  .disposal-entry-card__item {
    display: flex;
    flex-direction: column;
    gap: 6px;
    padding: 10px 12px;
    border-radius: 10px;
    background: rgba(255, 255, 255, 0.78);
    border: 1px solid rgba(64, 158, 255, 0.14);

    span {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }

    strong {
      font-size: 14px;
      color: var(--el-text-color-primary);
      word-break: break-word;
    }
  }

  .disposal-entry-card__panels {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
    gap: 12px;
    margin-top: 12px;
  }

  .disposal-entry-card__panel {
    padding: 12px 14px;
    border-radius: 12px;
    background: rgba(255, 255, 255, 0.92);
    border: 1px solid rgba(64, 158, 255, 0.14);

    span {
      display: block;
      margin-bottom: 6px;
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }

    strong {
      display: block;
      margin-bottom: 6px;
      font-size: 14px;
      color: #1d2f4f;
    }

    p {
      margin: 0;
      font-size: 13px;
      line-height: 1.7;
      color: var(--el-text-color-regular);
    }
  }

  .disposal-entry-card__actions {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-top: 14px;
    padding: 12px 14px;
    border-radius: 12px;
    background: rgba(255, 255, 255, 0.92);
    border: 1px solid rgba(64, 158, 255, 0.14);
  }

  .disposal-entry-card__action-copy {
    display: flex;
    flex-direction: column;
    gap: 4px;
  }

  .disposal-entry-card__action-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .disposal-entry-card__action-desc {
    font-size: 12px;
    line-height: 1.6;
    color: var(--el-text-color-secondary);
  }

  @media (max-width: 768px) {
    .disposal-entry-card__header,
    .disposal-entry-card__actions {
      flex-direction: column;
      align-items: stretch;
    }
  }
</style>
