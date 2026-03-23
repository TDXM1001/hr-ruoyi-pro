<template>
  <section class="occupancy-section" data-testid="occupancy-summary-section">
    <div class="occupancy-section__header">
      <div class="occupancy-section__eyebrow">管理摘要区</div>
      <div class="occupancy-section__title">主档联动与变更判断</div>
      <div class="occupancy-section__desc">
        这里用于辅助资产管理员核对当前主档口径、确认最近一次变更影响和状态矩阵，不承载高阶治理工具。
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
