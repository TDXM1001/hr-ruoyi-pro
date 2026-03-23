<template>
  <div class="disposal-closure-card" :data-testid="`${testidPrefix}-closure-card`">
    <div class="disposal-closure-card__header">
      <div>
        <div class="disposal-closure-card__label">当前闭环状态</div>
        <div class="disposal-closure-card__title">{{ card.statusLabel }}</div>
      </div>
      <ElTag :type="card.statusTagType" effect="light">
        {{ card.statusLabel }}
      </ElTag>
    </div>

    <div class="disposal-closure-card__meta">
      <span class="disposal-closure-card__label">最近处置动作</span>
      <strong>{{ card.latestActionLabel }}</strong>
      <span>{{ card.latestActionTime || '-' }}</span>
    </div>

    <div class="disposal-closure-card__grid">
      <div class="disposal-closure-card__item">
        <span>当前责任归口</span>
        <strong>{{ card.responsibilityOwnerLabel }}</strong>
      </div>
      <div class="disposal-closure-card__item">
        <span>责任动作</span>
        <strong>{{ card.responsibilityActionLabel }}</strong>
      </div>
      <div class="disposal-closure-card__item">
        <span>最近责任人</span>
        <strong>{{ card.latestOwnerLabel }}</strong>
      </div>
    </div>

    <div class="disposal-closure-card__desc">{{ card.latestActionDesc }}</div>
    <div class="disposal-closure-card__desc disposal-closure-card__desc--emphasis">
      {{ card.responsibilityHint }}
    </div>

    <div class="disposal-closure-card__next-step">
      <div class="disposal-closure-card__next-label">下一步建议</div>
      <div class="disposal-closure-card__next-value">{{ card.nextStep }}</div>
    </div>

    <div v-if="$slots.actions" class="disposal-closure-card__actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup lang="ts">
  import type { DisposalClosureCard } from './disposal-overview'

  defineProps<{
    card: DisposalClosureCard
    testidPrefix: string
  }>()
</script>

<style scoped lang="scss">
  .disposal-closure-card {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 14px 16px;
    border: 1px solid #f3e2c8;
    border-radius: 12px;
    background: linear-gradient(135deg, #fffdf8 0%, #fff7ec 100%);
  }

  .disposal-closure-card__header,
  .disposal-closure-card__meta,
  .disposal-closure-card__actions {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    flex-wrap: wrap;
  }

  .disposal-closure-card__label,
  .disposal-closure-card__next-label {
    font-size: 12px;
    color: #7b6d59;
  }

  .disposal-closure-card__title {
    margin-top: 6px;
    font-size: 18px;
    font-weight: 700;
    color: #3a2d1b;
  }

  .disposal-closure-card__grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
    gap: 10px;
  }

  .disposal-closure-card__item {
    display: flex;
    flex-direction: column;
    gap: 6px;
    padding: 10px 12px;
    border: 1px solid #f3e7d8;
    border-radius: 10px;
    background: rgba(255, 255, 255, 0.75);

    span {
      font-size: 12px;
      color: #7b6d59;
    }

    strong {
      color: #3a2d1b;
      font-size: 14px;
    }
  }

  .disposal-closure-card__desc,
  .disposal-closure-card__next-value {
    font-size: 12px;
    line-height: 1.7;
    color: #5f7392;
    word-break: break-word;
  }

  .disposal-closure-card__desc--emphasis {
    color: #7b5a20;
  }

  .disposal-closure-card__actions {
    justify-content: flex-end;
    padding-top: 4px;
  }
</style>
