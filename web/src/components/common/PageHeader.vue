<template>
  <div class="section-head">
    <div>
      <h2>{{ title }}</h2>
      <p v-if="description" class="subtle-note page-header__desc">{{ description }}</p>
    </div>
    <div class="page-header__meta">
      <slot name="meta" />
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    default: "",
  },
});
</script>
