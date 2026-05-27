<template>
  <div class="app-shell">
    <aside :class="sidebarClass">
      <RouterLink class="brand" :to="brandTo">
        <span class="brand__mark">{{ brandMark }}</span>
        <span class="brand__text">{{ brandText }}</span>
      </RouterLink>
      <nav class="nav">
        <RouterLink v-for="item in navItems" :key="item.to" class="nav__item" :to="item.to">
          {{ item.label }}
        </RouterLink>
      </nav>
    </aside>
    <main class="content">
      <header class="topbar">
        <div>
          <div class="eyebrow">{{ eyebrow }}</div>
          <h1>{{ title }}</h1>
        </div>
        <div class="topbar__actions">
          <slot name="topbar-actions" />
        </div>
      </header>
      <slot />
    </main>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { RouterLink } from "vue-router";

const props = defineProps({
  brandTo: {
    type: String,
    required: true,
  },
  brandMark: {
    type: String,
    required: true,
  },
  brandText: {
    type: String,
    required: true,
  },
  navItems: {
    type: Array,
    required: true,
  },
  eyebrow: {
    type: String,
    required: true,
  },
  title: {
    type: String,
    required: true,
  },
  admin: {
    type: Boolean,
    default: false,
  },
});

const sidebarClass = computed(() => (props.admin ? "sidebar sidebar--admin" : "sidebar"));
</script>
