<template>
  <section class="occupancy-section" data-testid="occupancy-business-section">
    <div class="occupancy-section__header">
      <div class="occupancy-section__eyebrow">业务操作区</div>
      <div class="occupancy-section__title">当前占用与直接动作</div>
      <div class="occupancy-section__desc">
        这里优先展示当前有效占用、发起占用、变更占用和释放占用，不穿插治理分析内容。
      </div>
    </div>
    <slot />
  </section>
</template>

<style scoped>
  .occupancy-section {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .occupancy-section__header {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  .occupancy-section__eyebrow {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .occupancy-section__title {
    font-size: 18px;
    font-weight: 700;
    color: var(--el-text-color-primary);
  }

  .occupancy-section__desc {
    font-size: 13px;
    line-height: 1.6;
    color: var(--el-text-color-secondary);
  }
</style>
