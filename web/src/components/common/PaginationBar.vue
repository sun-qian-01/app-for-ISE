<template>
  <div class="pagination-bar">
    <span>第 {{ pageNo }} 页 / 共 {{ totalPages }} 页</span>
    <span>共 {{ total }} 条</span>
    <div class="pagination-bar__actions">
      <button class="button" type="button" :disabled="pageNo <= 1" @click="$emit('change', pageNo - 1)">
        上一页
      </button>
      <button class="button" type="button" :disabled="pageNo >= totalPages" @click="$emit('change', pageNo + 1)">
        下一页
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";

defineEmits(["change"]);

const props = defineProps({
  pageNo: {
    type: Number,
    default: 1,
  },
  pageSize: {
    type: Number,
    default: 10,
  },
  total: {
    type: Number,
    default: 0,
  },
});

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.pageSize)));
</script>
