<template>
  <section class="panel">
    <div class="section-head">
      <h2>党团流程</h2>
      <span class="pill">GET /party/instances/me</span>
    </div>
    <div class="timeline timeline--flow">
      <div v-for="stage in stages" :key="stage.name" class="timeline__item" :class="statusClass(stage.status)">
        <strong>{{ stage.name }}</strong>
        <span>{{ stageLabel(stage.status) }} · {{ stage.dueAt }}</span>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { fetchPartyStages } from "../../mocks/server";
import { stageLabel, statusClass } from "../../utils/status";

const stages = ref([]);

onMounted(async () => {
  stages.value = await fetchPartyStages();
});
</script>
