<template>
  <div class="shell shell--centered">
    <section class="panel auth-panel">
      <div class="section-head">
        <h2>403 无权限访问</h2>
        <span class="pill pill--warn">40300</span>
      </div>
      <p class="hero__text">当前账号没有访问该页面的权限。前端已拦截，后端仍需继续做最终鉴权。</p>
      <RouterLink class="button button--primary" to="/login">返回登录页</RouterLink>
    </section>
  </div>
</template>

<script setup>
import { RouterLink } from "vue-router";
</script>
