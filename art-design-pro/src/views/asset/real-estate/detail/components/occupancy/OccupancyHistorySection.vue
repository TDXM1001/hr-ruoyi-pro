<template>
  <section class="occupancy-section" data-testid="occupancy-history-section">
    <div class="occupancy-section__header">
      <div class="occupancy-section__eyebrow">历史轨迹区</div>
      <div class="occupancy-section__title">占用历史轨迹</div>
      <div class="occupancy-section__desc">
        这里保留状态、时间、排序和关键字筛选，帮助资产管理员回看占用全过程。
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
