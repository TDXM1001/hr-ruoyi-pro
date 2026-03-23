<template>
  <section class="occupancy-section" data-testid="occupancy-governance-section">
    <div class="occupancy-section__header occupancy-section__header--row">
      <div class="occupancy-section__header-content">
        <div class="occupancy-section__eyebrow">治理工具区</div>
        <div class="occupancy-section__title">高级治理与分析能力</div>
        <div class="occupancy-section__desc">
          这里收纳趋势、预设、导入、快照和审计能力，默认折叠，不打断日常占用主流程。
        </div>
      </div>
      <div class="occupancy-section__actions">
        <slot name="actions" />
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

  .occupancy-section__header--row {
    flex-direction: row;
    align-items: flex-start;
    justify-content: space-between;
    gap: 16px;
  }

  .occupancy-section__header-content {
    display: flex;
    flex: 1;
    flex-direction: column;
    gap: 6px;
  }

  .occupancy-section__actions {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
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

  @media (max-width: 960px) {
    .occupancy-section__header--row {
      flex-direction: column;
    }

    .occupancy-section__actions {
      width: 100%;
    }
  }
</style>
