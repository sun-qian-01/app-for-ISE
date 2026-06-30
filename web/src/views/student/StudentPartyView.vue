<template>
  <section class="panel">
    <div class="section-head">
      <h2>党团流程</h2>
      <span class="subtle-note">按节点查看当前进度与截止时间</span>
    </div>

    <LoadingState v-if="loading" text="党团流程加载中..." />
    <ErrorState v-else-if="error" description="党团流程加载失败，请稍后重试。" @retry="loadData" />
    <EmptyState
      v-else-if="!stages.length"
      title="暂无党团流程"
      description="当前账号还没有党团流程记录。"
    />
    <div v-else class="timeline timeline--flow">
      <div v-for="stage in stages" :key="stage.stageRecordId" class="timeline__item" :class="stageStatusClass(stage)">
        <strong>{{ stage.stageName }}</strong>
        <span>{{ stageDisplayLabel(stage) }} · {{ stage.dueAt }}</span>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from "vue";
import EmptyState from "../../components/common/EmptyState.vue";
import ErrorState from "../../components/common/ErrorState.vue";
import LoadingState from "../../components/common/LoadingState.vue";
import { getMyPartyStages } from "../../api/modules/partyApi";
import { stageLabel, statusClass } from "../../utils/status";

const stages = ref([]);
const currentStageCode = ref("");
const loading = ref(false);
const error = ref(null);

onMounted(() => {
  loadData();
});

async function loadData() {
  loading.value = true;
  error.value = null;
  try {
    const instance = await getMyPartyStages();
    currentStageCode.value = instance.currentStageCode || "";
    stages.value = instance.stages || [];
  } catch (err) {
    error.value = err;
  } finally {
    loading.value = false;
  }
}

function isCurrentStage(stage) {
  return Boolean(currentStageCode.value) && stage.stageCode === currentStageCode.value;
}

function stageDisplayLabel(stage) {
  return isCurrentStage(stage) ? "所处阶段" : stageLabel(stage.stageStatus);
}

function stageStatusClass(stage) {
  return isCurrentStage(stage) ? "is-current" : statusClass(stage.stageStatus);
}
</script>
