<template>
  <section class="panel">
    <div class="section-head">
      <h2>党团流程</h2>
      <span class="subtle-note">按节点查看当前进度与截止时间</span>
    </div>
    <div class="timeline timeline--flow">
      <div v-for="stage in stages" :key="stage.stageRecordId" class="timeline__item" :class="statusClass(stage.status)">
        <strong>{{ stage.name }}</strong>
        <span>{{ stageLabel(stage.status) }} · {{ stage.dueAt }}</span>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { getMyPartyStages } from "../../api/modules/partyApi";
import { stageLabel, statusClass } from "../../utils/status";

const stages = ref([]);

onMounted(async () => {
  const instance = await getMyPartyStages();
  stages.value = (instance.stages || []).map((stage) => ({
    stageRecordId: stage.stageRecordId,
    name: stage.stageName,
    status: stage.stageStatus,
    dueAt: stage.dueAt,
  }));
});
</script>
