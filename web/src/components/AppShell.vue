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
      <section class="panel panel--banner">
        <div class="section-head">
          <h2>当前访问上下文</h2>
          <span class="pill">{{ contextApi }}</span>
        </div>
        <div class="scope-grid">
          <div class="scope-card">
            <span>{{ roleLabelText }}</span>
            <strong>{{ roleValue }}</strong>
          </div>
          <div class="scope-card">
            <span>{{ scopeLabelText }}</span>
            <strong>{{ scopeValue }}</strong>
          </div>
          <div class="scope-card">
            <span>{{ homeApiLabelText }}</span>
            <strong>{{ homeApiValue }}</strong>
          </div>
        </div>
      </section>
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
  contextApi: {
    type: String,
    default: "GET /api/v1/auth/me",
  },
  roleLabelText: {
    type: String,
    default: "角色代码",
  },
  roleValue: {
    type: String,
    required: true,
  },
  scopeLabelText: {
    type: String,
    default: "数据范围",
  },
  scopeValue: {
    type: String,
    required: true,
  },
  homeApiLabelText: {
    type: String,
    default: "当前首页接口",
  },
  homeApiValue: {
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
