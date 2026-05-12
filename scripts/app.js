const appData = {
  authUsers: [
    { username: "20220001", roleCode: "student", roleLabel: "普通学生", redirect: "./student.html" },
    { username: "20220018", roleCode: "class_cadre", roleLabel: "班团骨干", redirect: "./student.html" },
    { username: "teacher001", roleCode: "teacher_admin", roleLabel: "管理老师", redirect: "./admin.html" },
    { username: "leader001", roleCode: "college_leader", roleLabel: "学院领导", redirect: "./admin.html" }
  ],
  profile: {
    studentNo: "20220001",
    name: "赵晨曦",
    grade: "2022",
    major: "软件工程",
    className: "软件工程2班",
    politicalStatus: "probationary_party_member",
    politicalStatusLabel: "预备党员",
    phoneMasked: "138****1234",
    email: "zhaochenxi@example.edu.cn",
    counselor: "李老师",
    status: "active",
    statusLabel: "在读"
  },
  tags: ["2022级", "软件工程", "development_candidate", "奖学金关注", "就业意向"],
  growthRecords: [
    { type: "competition", typeLabel: "科研竞赛", title: "大学生创新训练项目", date: "2025-11", summary: "院级立项，负责需求分析与原型设计。" },
    { type: "volunteer", typeLabel: "志愿服务", title: "学院迎新志愿服务", date: "2026-04", summary: "累计服务 8 小时，完成新生咨询与路线引导。" },
    { type: "cadre", typeLabel: "干部任职", title: "软件工程2班学习委员", date: "2024-09 至今", summary: "协助课程通知、作业收集和学业帮扶。" }
  ],
  knowledge: [
    {
      title: "国家奖学金评定流程说明",
      category: "scholarship",
      categoryLabel: "奖助",
      summary: "包含申请资格、名额分配、材料清单和公示流程。",
      source: "国家奖学金评定办法.pdf",
      version: "v3",
      publishStatus: "published",
      keywords: ["奖学金", "国家奖学金", "评定", "材料"]
    },
    {
      title: "休学与复学办理指南",
      category: "student_status",
      categoryLabel: "学籍",
      summary: "说明休学申请条件、复学材料和学院审核路径。",
      source: "学籍异动办理指南.docx",
      version: "v2",
      publishStatus: "published",
      keywords: ["休学", "复学", "学籍"]
    },
    {
      title: "党员发展阶段材料清单",
      category: "party_league",
      categoryLabel: "党团",
      summary: "汇总积极分子、发展对象、预备党员各阶段所需材料。",
      source: "党员发展材料清单.xlsx",
      version: "v4",
      publishStatus: "published",
      keywords: ["党员", "党团", "思想汇报", "材料"]
    },
    {
      title: "在读证明与成绩证明模板下载",
      category: "certificate",
      categoryLabel: "证明",
      summary: "提供常用证明模板、用途示例和线上审批说明。",
      source: "证明模板包.zip",
      version: "v1",
      publishStatus: "published",
      keywords: ["证明", "模板", "在读证明", "成绩证明"]
    },
    {
      title: "毕业生就业信息登记说明",
      category: "employment",
      categoryLabel: "就业",
      summary: "说明就业去向填报、协议材料上传和二次核验规则。",
      source: "就业信息登记说明.pdf",
      version: "v1",
      publishStatus: "published",
      keywords: ["就业", "毕业", "登记"]
    }
  ],
  notices: [
    {
      title: "2026 年春季学期奖学金材料提交通知",
      audience: "2022级 + 奖学金关注",
      date: "2026-04-18",
      tags: ["奖助", "2022级"],
      channels: ["site", "email", "wechat"],
      channelLabels: ["站内", "邮件", "微信"],
      status: "published",
      read: false,
      content: "请于 4 月 24 日前完成材料提交，逾期系统将自动关闭入口。",
      stats: { total: 86, read: 72, failed: 3 }
    },
    {
      title: "预备党员季度思想汇报提醒",
      audience: "党员发展对象",
      date: "2026-04-17",
      tags: ["党团"],
      channels: ["site", "wechat"],
      channelLabels: ["站内", "微信"],
      status: "published",
      read: false,
      content: "你所在支部需于本周内补齐季度思想汇报，请及时上传。",
      stats: { total: 34, read: 22, failed: 1 }
    },
    {
      title: "毕业生就业信息登记更新说明",
      audience: "2026届 毕业生",
      date: "2026-04-15",
      tags: ["就业", "毕业年级"],
      channels: ["site", "email"],
      channelLabels: ["站内", "邮件"],
      status: "published",
      read: true,
      content: "就业去向信息已开放二次更新，请在学院平台完成信息核验。",
      stats: { total: 118, read: 104, failed: 5 }
    }
  ],
  stages: [
    { code: "applicant", name: "入党申请人", status: "approved", dueAt: "2025-09-30" },
    { code: "activist", name: "积极分子", status: "approved", dueAt: "2025-12-20" },
    { code: "development_candidate", name: "发展对象", status: "approved", dueAt: "2026-03-20" },
    { code: "probationary_party_member", name: "预备党员", status: "reviewing", dueAt: "2026-04-25" },
    { code: "party_member", name: "正式党员", status: "pending", dueAt: "2027-04-25" }
  ],
  materials: [
    { name: "季度思想汇报", fileName: "思想汇报-赵晨曦.docx", submitStatus: "submitted", reviewStatus: "pending", reviewStatusLabel: "待审核", submittedAt: "2026-04-18 14:30" },
    { name: "志愿服务记录表", fileName: "志愿服务记录.xlsx", submitStatus: "supplemented", reviewStatus: "supplement_required", reviewStatusLabel: "需补充", submittedAt: "2026-04-17 18:20" }
  ],
  applications: [
    { no: "APP20260418001", type: "certificate", typeLabel: "在读证明", status: "reviewing", statusLabel: "审核中", approver: "辅导员 李老师", purpose: "实习单位提交材料", createdAt: "2026-04-18 14:30" },
    { no: "APP20260410002", type: "certificate", typeLabel: "成绩证明", status: "approved", statusLabel: "已通过", approver: "教学秘书 王老师", purpose: "交换项目报名", createdAt: "2026-04-10 09:12" }
  ],
  honors: [
    { title: "国家奖学金获得者", owner: "赵晨曦", year: "2025", category: "national_scholarship", categoryLabel: "国家奖学金", publicStatus: "published", story: "综合成绩排名专业前 3%，参与创新训练项目和志愿服务。" },
    { title: "先进班集体", owner: "软件工程2班", year: "2025", category: "advanced_collective", categoryLabel: "先进集体", publicStatus: "published", story: "班级学风建设成效明显，竞赛参与率和志愿服务时长居年级前列。" },
    { title: "优秀共青团干部", owner: "陈一诺", year: "2026", category: "party_league_honor", categoryLabel: "党团荣誉", publicStatus: "published", story: "长期协助支部活动组织和材料收集，推动团员青年理论学习。" }
  ],
  students: [
    { studentNo: "20220001", name: "赵晨曦", grade: "2022", major: "软件工程", className: "软件工程2班", status: "active", statusText: "在读", tags: ["奖学金关注", "党员发展对象"] },
    { studentNo: "20220018", name: "陈一诺", grade: "2022", major: "软件工程", className: "软件工程2班", status: "active", statusText: "在读", tags: ["党员发展对象"] },
    { studentNo: "20260031", name: "林嘉禾", grade: "2026", major: "数据科学", className: "数据科学1班", status: "graduated", statusText: "毕业年级", tags: ["就业意向"] },
    { studentNo: "20230007", name: "周明远", grade: "2023", major: "软件工程", className: "软件工程1班", status: "active", statusText: "重点关注", tags: ["学业预警"] }
  ],
  approvals: [
    { applicant: "赵晨曦", type: "certificate", typeLabel: "在读证明", priority: "高", status: "reviewing", statusLabel: "待审核", detail: "用途：实习单位提交材料；提交时间：2026-04-18 14:30" },
    { applicant: "陈一诺", type: "party_material", typeLabel: "党团阶段材料", priority: "中", status: "reviewing", statusLabel: "待审核", detail: "需确认季度思想汇报与志愿服务记录。" },
    { applicant: "林嘉禾", type: "seal", typeLabel: "盖章申请", priority: "中", status: "reviewing", statusLabel: "待审核", detail: "就业协议材料需学院盖章确认。" }
  ],
  logs: [
    { actor: "辅导员 李老师", module: "学生画像", action: "查看学生敏感字段：联系方式", time: "2026-04-19 09:12", result: "成功" },
    { actor: "辅导员 李老师", module: "通知", action: "发布定向通知：奖学金材料提交", time: "2026-04-18 17:43", result: "成功" },
    { actor: "超级管理员", module: "权限", action: "调整角色权限：新增证明审批菜单", time: "2026-04-18 15:26", result: "成功" },
    { actor: "教学秘书 王老师", module: "导入", action: "导入 2026 届毕业生就业信息", time: "2026-04-17 10:02", result: "部分失败" }
  ],
  systemLogs: [
    { level: "error", levelLabel: "错误", type: "api_error", module: "application", requestId: "202605120930001001", path: "/api/v1/applications/approve", message: "审批状态冲突", detail: "同一申请被重复审批，后端返回 40900。" },
    { level: "warn", levelLabel: "警告", type: "client_error", module: "frontend", requestId: "202605120914001112", path: "/student/profile", message: "学生画像页面渲染异常", detail: "前端捕获 TypeError，并通过 /system-logs/client-errors 上报。" },
    { level: "info", levelLabel: "信息", type: "message_error", module: "notice", requestId: "202605120902009876", path: "/api/v1/notices/18/publish", message: "微信提醒模拟发送失败", detail: "站内消息已生成，微信通道记录 failed，不影响主流程。" }
  ]
};

function createElement(tag, className, html) {
  const element = document.createElement(tag);
  if (className) element.className = className;
  if (html !== undefined) element.innerHTML = html;
  return element;
}

function text(value) {
  return String(value ?? "");
}

function statusClass(status) {
  if (["approved", "已通过", "成功"].includes(status)) return "is-done";
  if (["reviewing", "审核中", "待审核"].includes(status)) return "is-current";
  if (["supplement_required", "需补充", "退回", "部分失败", "已退回"].includes(status)) return "is-warn";
  return "";
}

function levelClass(level) {
  if (level === "error") return "is-current";
  if (level === "warn") return "is-warn";
  return "is-done";
}

function bindNavigation() {
  const navItems = document.querySelectorAll(".nav__item");
  const views = document.querySelectorAll(".view");
  if (!navItems.length) return;

  navItems.forEach((item) => {
    item.addEventListener("click", () => {
      const target = item.dataset.target;
      navItems.forEach((nav) => nav.classList.remove("is-active"));
      item.classList.add("is-active");
      views.forEach((view) => {
        view.classList.toggle("is-visible", view.id === `view-${target}`);
      });
    });
  });

  document.querySelectorAll("[data-jump]").forEach((button) => {
    button.addEventListener("click", () => {
      document.querySelector(`.nav__item[data-target="${button.dataset.jump}"]`)?.click();
      window.scrollTo({ top: 0, behavior: "smooth" });
    });
  });
}

function setText(id, value) {
  const node = document.getElementById(id);
  if (node) node.textContent = text(value);
}

function renderStudentPage() {
  setText("todoCount", appData.materials.filter((item) => item.reviewStatus !== "approved").length + appData.applications.filter((item) => item.status === "reviewing").length);
  setText("unreadCount", appData.notices.filter((notice) => !notice.read).length);
  setText("growthCount", appData.growthRecords.length);
  setText("studentRoleCode", "student");

  renderTags("studentTags", appData.tags);
  renderProfileSummary();
  renderProgress("progressMini");
  renderTimeline();
  renderMaterials();
  renderNoticePreview();
  bindKnowledgeSearch();
  bindQaForm();
  bindNoticeCenter();
  bindMaterialForm();
  bindApplicationForm();
  renderApplications();
  renderProfileDetail();
  renderGrowthRecords();
  renderHonors("honorList", false);
}

function renderTags(targetId, tags) {
  const target = document.getElementById(targetId);
  if (!target) return;
  target.innerHTML = "";
  tags.forEach((tag) => target.appendChild(createElement("span", "tag", tag)));
}

function renderProfileSummary() {
  const target = document.getElementById("profileSummary");
  if (!target) return;
  const rows = [
    ["班级", appData.profile.className],
    ["辅导员", appData.profile.counselor],
    ["培养状态", appData.profile.statusLabel]
  ];
  target.innerHTML = rows.map(([label, value]) => `<div><span>${label}</span><strong>${value}</strong></div>`).join("");
}

function renderProgress(targetId) {
  const target = document.getElementById(targetId);
  if (!target) return;
  target.innerHTML = "";
  appData.stages.forEach((stage) => {
    target.appendChild(createElement("span", `stage-chip ${statusClass(stage.status)}`, stage.name));
  });
}

function renderTimeline() {
  const target = document.getElementById("timeline");
  if (!target) return;
  target.innerHTML = "";
  appData.stages.forEach((stage) => {
    target.appendChild(
      createElement(
        "div",
        `timeline__item ${statusClass(stage.status)}`,
        `<strong>${stage.name}</strong><span>${stageLabel(stage.status)} · ${stage.dueAt}</span>`
      )
    );
  });
}

function stageLabel(status) {
  const map = { approved: "已完成", reviewing: "审核中", pending: "未开始" };
  return map[status] || status;
}

function renderNoticePreview() {
  const target = document.getElementById("noticePreview");
  if (!target) return;
  target.innerHTML = "";
  appData.notices.slice(0, 2).forEach((notice) => target.appendChild(renderNoticeCard(notice)));
}

function renderNoticeCard(notice, full = false) {
  const channels = notice.channelLabels.map((item) => `<span class="tag">${item}</span>`).join("");
  return createElement(
    "article",
    `notice-card ${notice.read ? "" : "is-unread"}`.trim(),
    `<div class="notice-card__meta">${notice.date} · ${notice.audience}</div>
     <h3>${notice.title}</h3>
     <p>${full ? notice.content : notice.content.slice(0, 42) + "..."}</p>
     <div class="tag-group">${channels}</div>`
  );
}

function bindKnowledgeSearch() {
  const list = document.getElementById("knowledgeList");
  const keywordInput = document.getElementById("knowledgeSearch");
  const categorySelect = document.getElementById("knowledgeCategory");
  if (!list || !keywordInput || !categorySelect) return;

  function update() {
    const keyword = keywordInput.value.trim().toLowerCase();
    const category = categorySelect.value;
    const result = appData.knowledge.filter((item) => {
      const searchable = `${item.title} ${item.summary} ${item.source} ${item.keywords.join(" ")}`.toLowerCase();
      return (category === "all" || item.category === category) && (!keyword || searchable.includes(keyword));
    });
    list.innerHTML = "";
    if (!result.length) {
      list.appendChild(createElement("div", "record", "未找到匹配条目。"));
      return;
    }
    result.forEach((item) => {
      list.appendChild(
        createElement(
          "article",
          "knowledge-item",
          `<div class="notice-card__meta">${item.categoryLabel} · ${item.version} · ${item.publishStatus}</div>
           <h3>${item.title}</h3>
           <p>${item.summary}</p>
           <div class="source-line">来源：${item.source}</div>`
        )
      );
    });
  }

  keywordInput.addEventListener("input", update);
  categorySelect.addEventListener("change", update);
  update();
}

function bindQaForm() {
  const form = document.getElementById("qaForm");
  const result = document.getElementById("qaResult");
  if (!form || !result) return;
  form.addEventListener("submit", (event) => {
    event.preventDefault();
    const question = new FormData(form).get("question")?.toString().trim();
    const matched = appData.knowledge.find((item) => question && item.keywords.some((keyword) => question.includes(keyword))) || appData.knowledge[0];
    result.hidden = false;
    result.innerHTML = `
      <strong>回答</strong>
      <p>${matched.summary} 具体办理以学院当年通知为准。</p>
      <div class="source-line">依据：${matched.title} · ${matched.source}</div>
      <div class="source-line">模拟接口：POST /api/v1/kb/qa</div>
    `;
  });
}

function bindNoticeCenter() {
  const list = document.getElementById("noticeList");
  const filter = document.getElementById("noticeFilter");
  const markAllRead = document.getElementById("markAllRead");
  if (!list || !filter || !markAllRead) return;

  function update() {
    list.innerHTML = "";
    const result = appData.notices.filter((notice) => {
      if (filter.value === "read") return notice.read;
      if (filter.value === "unread") return !notice.read;
      return true;
    });
    result.forEach((notice) => {
      const card = renderNoticeCard(notice, true);
      const action = createElement("button", "button", notice.read ? "已读" : "标记已读");
      action.disabled = notice.read;
      action.addEventListener("click", () => {
        notice.read = true;
        update();
        setText("unreadCount", appData.notices.filter((item) => !item.read).length);
      });
      card.appendChild(action);
      list.appendChild(card);
    });
  }

  filter.addEventListener("change", update);
  markAllRead.addEventListener("click", () => {
    appData.notices.forEach((notice) => {
      notice.read = true;
    });
    setText("unreadCount", 0);
    update();
  });
  update();
}

function renderMaterials() {
  const target = document.getElementById("materialList");
  if (!target) return;
  target.innerHTML = "";
  appData.materials.forEach((item) => {
    target.appendChild(
      createElement(
        "article",
        `record ${statusClass(item.reviewStatus)}`,
        `<div class="record__meta">${item.submittedAt}</div>
         <h3>${item.name}</h3>
         <p>${item.fileName}</p>
         <span class="pill">${item.reviewStatusLabel}</span>`
      )
    );
  });
}

function bindMaterialForm() {
  const form = document.getElementById("materialForm");
  if (!form) return;
  form.addEventListener("submit", (event) => {
    event.preventDefault();
    const formData = new FormData(form);
    appData.materials.unshift({
      name: formData.get("materialName") || "补充材料",
      fileName: formData.get("fileName") || "未命名文件.docx",
      submitStatus: "submitted",
      reviewStatus: "pending",
      reviewStatusLabel: "待审核",
      submittedAt: new Date().toLocaleString("zh-CN", { hour12: false })
    });
    form.reset();
    renderMaterials();
    setText("todoCount", appData.materials.filter((item) => item.reviewStatus !== "approved").length + appData.applications.filter((item) => item.status === "reviewing").length);
  });
}

function bindApplicationForm() {
  const form = document.getElementById("applicationForm");
  const feedback = document.getElementById("applicationFeedback");
  if (!form || !feedback) return;
  form.addEventListener("submit", (event) => {
    event.preventDefault();
    const formData = new FormData(form);
    appData.applications.unshift({
      no: `APP${Date.now().toString().slice(-10)}`,
      type: "certificate",
      typeLabel: formData.get("applicationType"),
      status: "reviewing",
      statusLabel: "审核中",
      approver: "辅导员 李老师",
      purpose: formData.get("purpose") || "未填写",
      createdAt: new Date().toLocaleString("zh-CN", { hour12: false })
    });
    feedback.hidden = false;
    feedback.textContent = "申请已提交，当前状态为“审核中”。";
    form.reset();
    renderApplications();
    setText("todoCount", appData.materials.filter((item) => item.reviewStatus !== "approved").length + appData.applications.filter((item) => item.status === "reviewing").length);
  });
}

function renderApplications() {
  const target = document.getElementById("applicationList");
  if (!target) return;
  target.innerHTML = "";
  appData.applications.forEach((item) => {
    target.appendChild(
      createElement(
        "article",
        `record ${statusClass(item.status)}`,
        `<div class="record__meta">${item.no} · ${item.createdAt}</div>
         <h3>${item.typeLabel}</h3>
         <p>用途：${item.purpose}</p>
         <div class="tag-group"><span class="pill">${item.statusLabel}</span><span class="tag">${item.approver}</span></div>`
      )
    );
  });
}

function renderProfileDetail() {
  const target = document.getElementById("profileDetail");
  if (!target) return;
  const rows = [
    ["学号", appData.profile.studentNo],
    ["姓名", appData.profile.name],
    ["年级专业", `${appData.profile.grade}级 ${appData.profile.major}`],
    ["班级", appData.profile.className],
    ["政治面貌", appData.profile.politicalStatusLabel],
    ["联系方式", appData.profile.phoneMasked],
    ["邮箱", appData.profile.email],
    ["辅导员", appData.profile.counselor]
  ];
  target.innerHTML = rows.map(([label, value]) => `<div><span>${label}</span><strong>${value}</strong></div>`).join("");
}

function renderGrowthRecords() {
  const target = document.getElementById("growthList");
  if (!target) return;
  target.innerHTML = "";
  appData.growthRecords.forEach((item) => {
    target.appendChild(createElement("article", "record", `<div class="record__meta">${item.typeLabel} · ${item.date}</div><h3>${item.title}</h3><p>${item.summary}</p>`));
  });
}

function renderHonors(targetId, adminMode) {
  const target = document.getElementById(targetId);
  if (!target) return;
  target.innerHTML = "";
  appData.honors.forEach((item) => {
    const actions = adminMode ? `<div class="topbar__actions"><button class="button">发布</button><button class="button">下线</button></div>` : "";
    target.appendChild(
      createElement(
        "article",
        "card honor-card",
        `<div class="card__meta">${item.year} · ${item.categoryLabel}</div>
         <h3>${item.title}</h3>
         <p><strong>${item.owner}</strong></p>
         <p>${item.story}</p>${actions}`
      )
    );
  });
}

function renderAdminPage() {
  setText("pendingApprovalCount", appData.approvals.length);
  setText("todayPushCount", appData.notices.length);
  renderAdminTodos();
  renderStudentTable();
  renderAdminKnowledge();
  renderPartyReviews();
  bindNoticePublishForm();
  renderNoticeStats();
  renderApprovals();
  renderHonors("honorAdminList", true);
  renderAuditLogs();
  renderSystemLogs();
}

function renderAdminTodos() {
  const target = document.getElementById("adminTodoList");
  if (!target) return;
  target.innerHTML = "";
  appData.approvals.forEach((item) => {
    target.appendChild(createElement("article", "record", `<div class="record__meta">优先级：${item.priority}</div><h3>${item.typeLabel}</h3><p>${item.applicant} · ${item.detail}</p>`));
  });
}

function renderStudentTable() {
  const target = document.getElementById("studentTable");
  const search = document.getElementById("studentSearch");
  const filter = document.getElementById("studentStatusFilter");
  if (!target || !search || !filter) return;

  function update() {
    const keyword = search.value.trim().toLowerCase();
    const status = filter.value;
    const result = appData.students.filter((student) => {
      const searchable = `${student.studentNo} ${student.name} ${student.grade} ${student.major} ${student.className} ${student.tags.join(" ")}`.toLowerCase();
      return (!keyword || searchable.includes(keyword)) && (status === "all" || student.status === status);
    });
    target.innerHTML = `
      <div class="table-row table-head"><span>学号</span><span>姓名</span><span>班级</span><span>状态</span><span>标签</span></div>
      ${result.map((item) => `<div class="table-row"><span>${item.studentNo}</span><strong>${item.name}</strong><span>${item.className}</span><span>${item.statusText}</span><span>${item.tags.join("、")}</span></div>`).join("")}
    `;
  }

  search.addEventListener("input", update);
  filter.addEventListener("change", update);
  update();
}

function renderAdminKnowledge() {
  const list = document.getElementById("knowledgeAdminList");
  const templates = document.getElementById("templateAdminList");
  if (list) {
    list.innerHTML = "";
    appData.knowledge.forEach((item) => {
      list.appendChild(createElement("article", "knowledge-item", `<div class="notice-card__meta">${item.categoryLabel} · ${item.version} · ${item.publishStatus}</div><h3>${item.title}</h3><p>${item.summary}</p><div class="source-line">来源：${item.source}</div>`));
    });
  }
  if (templates) {
    templates.innerHTML = "";
    appData.knowledge.filter((item) => item.category === "certificate" || item.source.endsWith(".xlsx")).forEach((item) => {
      templates.appendChild(createElement("article", "record", `<div class="record__meta">file_resource</div><h3>${item.source}</h3><p>${item.title}</p><span class="pill">可下载</span>`));
    });
  }
}

function renderPartyReviews() {
  const reviews = document.getElementById("partyReviewList");
  const cadre = document.getElementById("cadreTodoList");
  if (reviews) {
    reviews.innerHTML = "";
    appData.materials.forEach((item) => {
      const node = createElement("article", "record", `<div class="record__meta">${item.submittedAt}</div><h3>${item.name}</h3><p>赵晨曦 · ${item.fileName}</p>`);
      const actions = createElement("div", "topbar__actions");
      actions.appendChild(createElement("button", "button button--primary", "通过"));
      actions.appendChild(createElement("button", "button", "退回"));
      node.appendChild(actions);
      reviews.appendChild(node);
    });
  }
  if (cadre) {
    cadre.innerHTML = "";
    [
      ["软件工程2班", "3 人材料未提交", "班长可协助提醒，不具备审批权限"],
      ["本科生第一党支部", "5 人处于预备党员考察", "团支书可查看进度摘要"]
    ].forEach(([title, meta, summary]) => {
      cadre.appendChild(createElement("article", "record", `<div class="record__meta">${meta}</div><h3>${title}</h3><p>${summary}</p>`));
    });
  }
}

function bindNoticePublishForm() {
  const form = document.getElementById("noticeForm");
  const feedback = document.getElementById("noticeFeedback");
  if (!form || !feedback) return;
  form.addEventListener("submit", (event) => {
    event.preventDefault();
    const formData = new FormData(form);
    const channels = formData.get("channels").toString().split(/[，,]/).map((item) => item.trim()).filter(Boolean);
    appData.notices.unshift({
      title: formData.get("title") || "未命名通知",
      audience: formData.get("scope"),
      date: new Date().toISOString().slice(0, 10),
      tags: ["定向"],
      channels: channels.map((item) => item.toLowerCase()),
      channelLabels: channels,
      status: "published",
      read: false,
      content: "管理端模拟发布的定向通知。",
      stats: { total: 48, read: 0, failed: 0 }
    });
    feedback.hidden = false;
    feedback.textContent = "通知已模拟发布，并生成站内消息与渠道发送记录。";
    form.reset();
    renderNoticeStats();
    setText("todayPushCount", appData.notices.length);
  });
}

function renderNoticeStats() {
  const target = document.getElementById("noticeStatsList");
  if (!target) return;
  target.innerHTML = "";
  appData.notices.forEach((notice) => {
    const unread = notice.stats.total - notice.stats.read;
    target.appendChild(createElement("article", "record", `<div class="record__meta">${notice.audience}</div><h3>${notice.title}</h3><p>总人数 ${notice.stats.total} · 已读 ${notice.stats.read} · 未读 ${unread} · 失败 ${notice.stats.failed}</p>`));
  });
}

function renderApprovals() {
  const target = document.getElementById("approvalList");
  if (!target) return;
  target.innerHTML = "";
  appData.approvals.forEach((item, index) => {
    const node = createElement("article", "record", `<div class="record__meta">${item.statusLabel} · 优先级 ${item.priority}</div><h3>${item.applicant} - ${item.typeLabel}</h3><p>${item.detail}</p>`);
    const actions = createElement("div", "topbar__actions");
    const approve = createElement("button", "button button--primary", "通过");
    const reject = createElement("button", "button", "退回");
    approve.addEventListener("click", () => {
      appData.approvals.splice(index, 1);
      renderApprovals();
      renderAdminTodos();
      setText("pendingApprovalCount", appData.approvals.length);
    });
    reject.addEventListener("click", () => {
      item.status = "rejected";
      item.statusLabel = "已退回";
      node.classList.add("is-warn");
      node.querySelector(".record__meta").textContent = `${item.statusLabel} · 优先级 ${item.priority}`;
    });
    actions.appendChild(approve);
    actions.appendChild(reject);
    node.appendChild(actions);
    target.appendChild(node);
  });
}

function renderAuditLogs() {
  const target = document.getElementById("auditLogList");
  if (!target) return;
  target.innerHTML = "";
  appData.logs.forEach((item) => {
    target.appendChild(createElement("article", `log-item ${statusClass(item.result)}`, `<div class="record__meta">${item.time} · ${item.module}</div><h3>${item.actor}</h3><p>${item.action}</p><span class="pill">${item.result}</span>`));
  });
}

function renderSystemLogs() {
  const target = document.getElementById("systemLogList");
  const filter = document.getElementById("systemLogLevelFilter");
  if (!target || !filter) return;

  function update() {
    target.innerHTML = "";
    const result = appData.systemLogs.filter((item) => filter.value === "all" || item.level === filter.value);
    result.forEach((item) => {
      target.appendChild(
        createElement(
          "article",
          `log-item ${levelClass(item.level)}`,
          `<div class="record__meta">${item.levelLabel} · ${item.module} · ${item.requestId}</div>
           <h3>${item.message}</h3>
           <p>${item.path}</p>
           <div class="log-detail">${item.detail}</div>`
        )
      );
    });
  }

  filter.addEventListener("change", update);
  update();
}

function bindLoginForm() {
  const form = document.getElementById("loginForm");
  const feedback = document.getElementById("loginFeedback");
  if (!form || !feedback) return;

  form.addEventListener("submit", (event) => {
    event.preventDefault();
    const formData = new FormData(form);
    const roleCode = String(formData.get("roleCode") || "student");
    const matched = appData.authUsers.find((item) => item.roleCode === roleCode) || appData.authUsers[0];
    feedback.hidden = false;
    feedback.textContent = `模拟登录成功：${matched.roleLabel}，即将进入对应工作台。`;
    window.setTimeout(() => {
      window.location.href = matched.redirect;
    }, 500);
  });
}

function main() {
  bindNavigation();
  bindLoginForm();
  const page = document.body.dataset.page;
  if (page === "student") renderStudentPage();
  if (page === "admin") renderAdminPage();
}

main();
