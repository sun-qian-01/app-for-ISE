<template>
  <div class="shell shell--centered">
    <section class="panel auth-panel">
      <div class="section-head">
        <h2>404 页面不存在</h2>
      </div>
      <p class="hero__text">当前路由不存在，可以从登录页重新进入。</p>
      <RouterLink class="button button--primary" to="/login">前往登录页</RouterLink>
    </section>
  </div>
</template>

<script setup>
import { RouterLink } from "vue-router";
</script>
