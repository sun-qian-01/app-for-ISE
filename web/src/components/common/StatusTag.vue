<template>
  <span :class="tagClass">{{ label }}</span>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  label: {
    type: String,
    required: true,
  },
  tone: {
    type: String,
    default: "default",
  },
});

const tagClass = computed(() => {
  if (props.tone === "success") return "pill pill--success";
  if (props.tone === "warn") return "pill pill--warn";
  return "pill";
});
</script>
