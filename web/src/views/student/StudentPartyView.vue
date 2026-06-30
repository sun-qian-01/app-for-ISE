<template>
  <section class="panel">
    <div class="section-head">
      <h2>党团流程</h2>
      <span class="subtle-note">按节点查看当前进度与截止时间</span>
    </div>

    <LoadingState v-if="loading" text="党团流程加载中..." />
    <ErrorState v-else-if="error" description="党团流程加载失败，请稍后重试。" @retry="loadData" />
    <EmptyState
      v-else-if="!visibleFlows.length"
      title="暂无党团流程"
      description="当前账号还没有党团流程记录。"
    />
    <div v-else class="flow-stack">
      <div v-for="flow in visibleFlows" :key="flow.flowCode || flow.flowName" class="flow-group">
        <div class="flow-group__head">
          <h3>{{ flow.flowName }}</h3>
          <span class="subtle-note">{{ flow.stages.length }} 个节点</span>
        </div>
        <div class="timeline timeline--flow">
          <div
            v-for="stage in flow.stages"
            :key="stage.stageRecordId || `${flow.flowCode}-${stage.stageCode}`"
            class="timeline__item"
            :class="stageStatusClass(flow, stage)"
          >
            <strong>{{ stage.stageName }}</strong>
            <span>{{ stageDisplayLabel(flow, stage) }} · {{ stage.dueAt }}</span>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import EmptyState from "../../components/common/EmptyState.vue";
import ErrorState from "../../components/common/ErrorState.vue";
import LoadingState from "../../components/common/LoadingState.vue";
import { getMyPartyFlows } from "../../api/modules/partyApi";
import { stageLabel, statusClass } from "../../utils/status";

const flows = ref([]);
const loading = ref(false);
const error = ref(null);
const visibleFlows = computed(() => (flows.value || []).filter((flow) => (flow.stages || []).length));

onMounted(() => {
  loadData();
});

async function loadData() {
  loading.value = true;
  error.value = null;
  try {
    flows.value = await getMyPartyFlows();
  } catch (err) {
    error.value = err;
  } finally {
    loading.value = false;
  }
}

function isCurrentStage(flow, stage) {
  return Boolean(flow.currentStageCode) && stage.stageCode === flow.currentStageCode;
}

function stageDisplayLabel(flow, stage) {
  return isCurrentStage(flow, stage) ? "所处阶段" : stageLabel(stage.stageStatus);
}

function stageStatusClass(flow, stage) {
  return isCurrentStage(flow, stage) ? "is-current" : statusClass(stage.stageStatus);
}
</script>
