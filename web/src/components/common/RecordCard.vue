<template>
  <article
    class="record-card"
    :class="[cardClass, { 'record-card--link': isLink }]"
    :tabindex="isLink ? 0 : undefined"
    @click="openLink($event)"
    @keydown.enter.prevent="openLink"
    @keydown.space.prevent="openLink"
  >
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
import { useRouter } from "vue-router";

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
  to: {
    type: [String, Object],
    default: "",
  },
});

const router = useRouter();

const cardClass = computed(() => {
  if (props.tone === "success") return "record is-done";
  if (props.tone === "warn") return "record is-warn";
  if (props.tone === "current") return "record is-current";
  return "record";
});

const isLink = computed(() => Boolean(props.to));

function openLink(event) {
  if (!isLink.value) {
    return;
  }
  if (event?.target instanceof Element) {
    const blockedTarget = event.target.closest("button, a, input, textarea, select, [data-no-card-link='true']");
    if (blockedTarget) {
      return;
    }
  }
  router.push(props.to);
}
</script>
