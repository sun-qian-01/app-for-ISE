<template>
  <section class="panel">
    <PageHeader title="学生画像管理" api="GET /students">
      <template #actions>
        <StatusTag v-if="canViewSensitive" label="具备敏感字段查看权限" tone="success" />
        <StatusTag v-else label="当前仅展示摘要信息" tone="warn" />
      </template>
    </PageHeader>
    <SearchBar>
      <input v-model="keyword" class="input" type="search" placeholder="搜索学号、姓名、班级" />
      <select v-model="statusFilter" class="input input--select">
        <option value="all">全部状态</option>
        <option value="在读">在读</option>
        <option value="毕业年级">毕业年级</option>
        <option value="重点关注">重点关注</option>
      </select>
    </SearchBar>
    <div class="import-box">
      <div>
        <strong>批量注册学生账号</strong>
        <p class="subtle-note">支持 CSV/TSV 表格，表头包含学号、姓名、年级、班级、专业。导入后账号为学号，初始密码统一为 info666。</p>
      </div>
      <div class="import-box__actions">
        <input class="input" type="file" accept=".csv,.tsv,text/csv,text/tab-separated-values" @change="handleImportFile" />
        <button class="button" type="button" @click="downloadTemplate">下载模板</button>
      </div>
    </div>
    <p v-if="importFeedback" class="feedback">{{ importFeedback }}</p>
    <LoadingState v-if="loading" text="学生列表加载中..." />
    <ErrorState v-else-if="error" description="学生列表加载失败，请稍后重试。" @retry="loadData" />
    <DataTable
      v-else
      :columns="columns"
      :rows="filteredItems"
      row-key="studentNo"
      template-columns="1fr 0.8fr 0.7fr 1fr 1.2fr 0.8fr 1.4fr"
      empty-title="没有匹配学生"
      empty-description="请调整搜索词或筛选条件。"
    >
      <template #cell-name="{ row }">
        <strong>{{ row.name }}</strong>
      </template>
      <template #cell-tags="{ row }">
        <span>{{ row.tags.join("、") }}</span>
      </template>
    </DataTable>
    <PaginationBar
      v-if="!loading && !error && studentTotal > pageSize"
      :page-no="pageNo"
      :page-size="pageSize"
      :total="studentTotal"
      @change="changePage"
    />
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import DataTable from "../../components/common/DataTable.vue";
import ErrorState from "../../components/common/ErrorState.vue";
import LoadingState from "../../components/common/LoadingState.vue";
import PageHeader from "../../components/common/PageHeader.vue";
import PaginationBar from "../../components/common/PaginationBar.vue";
import SearchBar from "../../components/common/SearchBar.vue";
import StatusTag from "../../components/common/StatusTag.vue";
import { useAsyncPage } from "../../composables/useAsyncPage";
import { usePermission } from "../../composables/usePermission";
import { batchRegisterStudentsApi, getStudentList } from "../../api/modules/studentApi";

const items = ref([]);
const studentTotal = ref(0);
const pageNo = ref(1);
const pageSize = 20;
const keyword = ref("");
const statusFilter = ref("all");
const importFeedback = ref("");
const { hasPermission } = usePermission();
const { loading, error, run } = useAsyncPage(() =>
  getStudentList({
    pageNo: pageNo.value,
    pageSize,
    keyword: keyword.value.trim() || undefined,
    status: statusFilter.value === "all" ? undefined : statusFilter.value,
  }),
);

const columns = [
  { key: "studentNo", label: "学号" },
  { key: "name", label: "姓名" },
  { key: "grade", label: "年级" },
  { key: "major", label: "专业" },
  { key: "className", label: "班级" },
  { key: "statusText", label: "状态" },
  { key: "tags", label: "标签" },
];

const canViewSensitive = computed(() => hasPermission("student:sensitive:view"));

const filteredItems = computed(() =>
  items.value.filter((item) => {
    const matchKeyword = !keyword.value || `${item.studentNo} ${item.name} ${item.className}`.includes(keyword.value);
    const matchStatus = statusFilter.value === "all" || item.statusText === statusFilter.value;
    return matchKeyword && matchStatus;
  }),
);

onMounted(async () => {
  loadData();
});

watch([keyword, statusFilter], () => {
  pageNo.value = 1;
  loadData();
});

async function loadData() {
  try {
    const page = await run();
    studentTotal.value = Number(page.total) || 0;
    items.value = (page.records || []).map((item) => ({
      studentNo: item.studentNo,
      name: item.name,
      grade: item.grade,
      major: item.major,
      className: item.className,
      statusText: item.status,
      tags: item.tags || [],
    }));
  } catch {}
}

async function changePage(nextPageNo) {
  pageNo.value = nextPageNo;
  await loadData();
}

async function handleImportFile(event) {
  const file = event.target.files?.[0];
  importFeedback.value = "";
  if (!file) return;

  try {
    const text = await file.text();
    const rows = parseCsv(text);
    if (!rows.length) {
      importFeedback.value = "没有识别到可导入的学生数据。";
      return;
    }
    const result = await batchRegisterStudentsApi(rows);
    pageNo.value = 1;
    await loadData();
    importFeedback.value = `导入完成：成功 ${result.successCount} 人，跳过 ${result.skippedCount} 人，失败 ${result.failedCount} 人。${result.messages.slice(0, 3).join("；")}`;
  } catch (error) {
    importFeedback.value = error.message || "文件解析失败，请检查 CSV 格式。";
  } finally {
    event.target.value = "";
  }
}

function parseCsv(text) {
  const lines = text
    .replace(/^\uFEFF/, "")
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean);
  if (lines.length < 2) return [];

  const delimiter = detectDelimiter(lines[0]);
  const headers = splitDelimitedLine(lines[0], delimiter).map(normalizeHeader);
  return lines.slice(1).map((line) => {
    const cells = splitDelimitedLine(line, delimiter);
    const row = {};
    headers.forEach((header, index) => {
      row[header] = cells[index]?.trim() ?? "";
    });
    return {
      studentNo: row.studentNo,
      name: row.name,
      grade: row.grade,
      className: row.className,
      major: row.major,
      phone: row.phone,
      email: row.email,
      politicalStatusLabel: row.politicalStatusLabel,
    };
  });
}

function detectDelimiter(headerLine) {
  return headerLine.includes("\t") ? "\t" : ",";
}

function splitDelimitedLine(line, delimiter) {
  const cells = [];
  let current = "";
  let inQuotes = false;

  for (let index = 0; index < line.length; index += 1) {
    const char = line[index];
    const next = line[index + 1];
    if (char === '"' && next === '"') {
      current += '"';
      index += 1;
    } else if (char === '"') {
      inQuotes = !inQuotes;
    } else if (char === delimiter && !inQuotes) {
      cells.push(current);
      current = "";
    } else {
      current += char;
    }
  }
  cells.push(current);
  return cells;
}

function normalizeHeader(header) {
  const normalized = header.trim().toLowerCase();
  const map = {
    学号: "studentNo",
    账号: "studentNo",
    studentno: "studentNo",
    student_no: "studentNo",
    姓名: "name",
    name: "name",
    年级: "grade",
    grade: "grade",
    班级: "className",
    classname: "className",
    class_name: "className",
    专业: "major",
    major: "major",
    手机: "phone",
    联系方式: "phone",
    phone: "phone",
    邮箱: "email",
    email: "email",
    政治面貌: "politicalStatusLabel",
    politicalstatuslabel: "politicalStatusLabel",
    political_status: "politicalStatusLabel",
  };
  return map[normalized] ?? normalized;
}

function downloadTemplate() {
  const content = "学号,姓名,年级,班级,专业\n20260001,张三,2026,软件工程1班,软件工程\n";
  const blob = new Blob(["\uFEFF", content], { type: "text/csv;charset=utf-8" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = "学生批量注册模板.csv";
  link.click();
  URL.revokeObjectURL(url);
}
</script>
