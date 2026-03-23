<template>
  <div
    class="disposal-responsibility-chain"
    :class="`disposal-responsibility-chain--${mode}`"
    :data-testid="resolvedTestid"
  >
    <template v-if="mode === 'card'">
      <div class="disposal-responsibility-chain__title">最近动作责任链</div>
      <div class="disposal-responsibility-chain__grid">
        <div class="disposal-responsibility-chain__item">
          <span>最近动作</span>
          <strong>{{ chain.recentActionLabel }}</strong>
        </div>
        <div class="disposal-responsibility-chain__item">
          <span>责任归口</span>
          <strong>{{ chain.ownerLabel }}</strong>
        </div>
        <div class="disposal-responsibility-chain__item">
          <span>责任动作</span>
          <strong>{{ chain.actionLabel }}</strong>
        </div>
        <div class="disposal-responsibility-chain__item">
          <span>最近责任人</span>
          <strong>{{ chain.latestOwnerLabel }}</strong>
        </div>
      </div>
      <div class="disposal-responsibility-chain__hint">{{ chain.hint }}</div>
    </template>

    <template v-else>
      <div class="disposal-responsibility-chain__inline-text">
        最近动作责任链：{{ chain.recentActionLabel }} · 责任归口：{{ chain.ownerLabel }} · 责任动作：{{
          chain.actionLabel
        }} · 最近责任人：{{ chain.latestOwnerLabel }}
      </div>
      <div v-if="showHint" class="disposal-responsibility-chain__inline-hint">{{ chain.hint }}</div>
    </template>
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue'
  import type { DisposalResponsibilityChain } from './disposal-overview'

  const props = withDefaults(
    defineProps<{
      chain: DisposalResponsibilityChain
      testidPrefix: string
      mode?: 'card' | 'inline'
      showHint?: boolean
    }>(),
    {
      mode: 'inline',
      showHint: true
    }
  )

  const resolvedTestid = computed(() => {
    return props.mode === 'card' ? `${props.testidPrefix}-chain` : props.testidPrefix
  })
</script>

<style scoped lang="scss">
  .disposal-responsibility-chain {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .disposal-responsibility-chain__title {
    font-size: 12px;
    color: #7b6d59;
  }

  .disposal-responsibility-chain__grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
    gap: 10px;
  }

  .disposal-responsibility-chain__item {
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

  .disposal-responsibility-chain__hint,
  .disposal-responsibility-chain__inline-text,
  .disposal-responsibility-chain__inline-hint {
    font-size: 12px;
    line-height: 1.7;
    color: #5f7392;
    word-break: break-word;
  }

  .disposal-responsibility-chain__hint,
  .disposal-responsibility-chain__inline-hint {
    color: #7b5a20;
  }
</style>
