<template>
  <article class="metric-card">
    <span>{{ label }}</span>
    <strong :class="{ 'metric-text': textMode }">{{ value }}</strong>
    <small>{{ hint }}</small>
  </article>
</template>

<script setup>
defineProps({
  label: {
    type: String,
    required: true,
  },
  value: {
    type: [String, Number],
    required: true,
  },
  hint: {
    type: String,
    default: "",
  },
  textMode: {
    type: Boolean,
    default: false,
  },
});
</script>
