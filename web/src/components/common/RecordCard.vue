<template>
  <article class="record-card" :class="cardClass">
    <div class="record__meta">{{ meta }}</div>
    <h3>{{ title }}</h3>
    <p v-if="description">{{ description }}</p>
    <div v-if="$slots.tags" class="tag-group record-card__tags">
      <slot name="tags" />
    </div>
    <div v-if="$slots.actions" class="topbar__actions record-card__actions">
      <slot name="actions" />
    </div>
    <div v-if="$slots.extra" class="log-detail">
      <slot name="extra" />
    </div>
  </article>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  meta: {
    type: String,
    default: "",
  },
  title: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    default: "",
  },
  tone: {
    type: String,
    default: "",
  },
});

const cardClass = computed(() => {
  if (props.tone === "success") return "record is-done";
  if (props.tone === "warn") return "record is-warn";
  if (props.tone === "current") return "record is-current";
  return "record";
});
</script>
