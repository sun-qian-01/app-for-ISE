<template>
  <div class="table-like">
    <div class="table-row table-head" :style="gridStyle">
      <span v-for="column in columns" :key="column.key">{{ column.label }}</span>
    </div>
    <EmptyState v-if="!rows.length" :title="emptyTitle" :description="emptyDescription" />
    <div v-for="row in rows" :key="row[rowKey]" class="table-row" :style="gridStyle">
      <template v-for="column in columns" :key="column.key">
        <slot :name="`cell-${column.key}`" :row="row">
          <span>{{ row[column.key] }}</span>
        </slot>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import EmptyState from "./EmptyState.vue";

const props = defineProps({
  columns: {
    type: Array,
    required: true,
  },
  rows: {
    type: Array,
    required: true,
  },
  rowKey: {
    type: String,
    default: "id",
  },
  templateColumns: {
    type: String,
    default: "",
  },
  emptyTitle: {
    type: String,
    default: "暂无数据",
  },
  emptyDescription: {
    type: String,
    default: "当前筛选条件下没有可展示内容。",
  },
});

const gridStyle = computed(() => {
  if (!props.templateColumns) return {};
  return { gridTemplateColumns: props.templateColumns };
});
</script>
