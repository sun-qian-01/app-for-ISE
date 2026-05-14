const appData = {
  authUsers: [
    { username: "20220001", roleCode: "student", roleLabel: "普通学生", redirect: "./student.html", permissions: ["student:self:view"] },
    { username: "20220018", roleCode: "class_cadre", roleLabel: "班团骨干", redirect: "./student.html", permissions: ["student:self:view", "cadre:party:todo:view", "party:instance:scope:view", "party:todo:remind"] },
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
    { name: "季度思想汇报", fileId: "file_20260418001", fileName: "思想汇报-赵晨曦.docx", submitStatus: "submitted", reviewStatus: "pending", reviewStatusLabel: "待审核", submittedAt: "2026-04-18 14:30" },
    { name: "志愿服务记录表", fileId: "file_20260417008", fileName: "志愿服务记录.xlsx", submitStatus: "supplemented", reviewStatus: "supplement_required", reviewStatusLabel: "需补充", submittedAt: "2026-04-17 18:20" }
  ],
  certificateTemplates: [
    {
      id: "tpl_study",
      templateName: "在读证明",
      type: "certificate",
      approvalRule: "辅导员初审 -> 学院办公室盖章",
      templateFileId: "file_tpl_001",
      formSchemaJson: [
        { name: "receiveOrg", label: "接收单位", type: "text", placeholder: "如：某某科技有限公司" },
        { name: "deliveryMode", label: "领取方式", type: "select", options: ["电子版", "纸质版"] }
      ]
    },
    {
      id: "tpl_score",
      templateName: "成绩证明",
      type: "certificate",
      approvalRule: "教学秘书审核 -> 学院办公室盖章",
      templateFileId: "file_tpl_002",
      formSchemaJson: [
        { name: "language", label: "语言版本", type: "select", options: ["中文", "英文", "中英文"] },
        { name: "copies", label: "份数", type: "number", placeholder: "1" }
      ]
    },
    {
      id: "tpl_seal",
      templateName: "盖章申请",
      type: "seal",
      approvalRule: "辅导员审核 -> 学院办公室盖章",
      templateFileId: "file_tpl_003",
      formSchemaJson: [
        { name: "documentName", label: "材料名称", type: "text", placeholder: "如：实习协议" },
        { name: "sealType", label: "用章类型", type: "select", options: ["学院公章", "党委章"] }
      ]
    }
  ],
  applications: [
    {
      id: "app_001",
      no: "APP20260418001",
      templateId: "tpl_study",
      type: "certificate",
      typeLabel: "在读证明",
      status: "reviewing",
      statusLabel: "审核中",
      approver: "辅导员 李老师",
      applicant: "赵晨曦",
      applicantSummary: "2022级 软件工程2班 预备党员",
      purpose: "实习单位提交材料",
      createdAt: "2026-04-18 14:30",
      formData: { receiveOrg: "星河智造科技有限公司", deliveryMode: "电子版" },
      attachmentFileIds: ["file_20260418077"],
      generatedFile: null,
      approvalRecords: [
        { nodeName: "提交申请", operator: "赵晨曦", action: "submit", opinion: "提交院内证明申请", operatedAt: "2026-04-18 14:30" },
        { nodeName: "辅导员审核", operator: "李老师", action: "pending", opinion: "待处理", operatedAt: "-" }
      ]
    },
    {
      id: "app_002",
      no: "APP20260410002",
      templateId: "tpl_score",
      type: "certificate",
      typeLabel: "成绩证明",
      status: "approved",
      statusLabel: "已通过",
      approver: "教学秘书 王老师",
      applicant: "赵晨曦",
      applicantSummary: "2022级 软件工程2班 预备党员",
      purpose: "交换项目报名",
      createdAt: "2026-04-10 09:12",
      formData: { language: "中英文", copies: "2" },
      attachmentFileIds: [],
      generatedFile: { fileId: "file_gen_20260410002", fileName: "成绩证明-赵晨曦.pdf" },
      approvalRecords: [
        { nodeName: "提交申请", operator: "赵晨曦", action: "submit", opinion: "申请交换项目报名材料", operatedAt: "2026-04-10 09:12" },
        { nodeName: "教学秘书审核", operator: "王老师", action: "approve", opinion: "信息无误，同意开具", operatedAt: "2026-04-10 16:40" }
      ]
    }
  ],
  honors: [
    { id: "honor_001", title: "国家奖学金获得者", owner: "赵晨曦", ownerType: "student", ownerTypeLabel: "个人", year: "2025", category: "scholarship", categoryLabel: "国家奖学金", publicStatus: "published", publicStatusLabel: "已发布", displayOrder: 1, visibleFrom: "2025-12-01", visibleTo: "2026-12-31", coverFileId: "file_honor_001", story: "综合成绩排名专业前 3%，参与创新训练项目和志愿服务。" },
    { id: "honor_002", title: "先进班集体", owner: "软件工程2班", ownerType: "class", ownerTypeLabel: "集体", year: "2025", category: "advanced_collective", categoryLabel: "先进集体", publicStatus: "published", publicStatusLabel: "已发布", displayOrder: 2, visibleFrom: "2025-11-01", visibleTo: "2026-11-30", coverFileId: "file_honor_002", story: "班级学风建设成效明显，竞赛参与率和志愿服务时长居年级前列。" },
    { id: "honor_003", title: "优秀共青团干部", owner: "陈一诺", ownerType: "student", ownerTypeLabel: "个人", year: "2026", category: "party_league_honor", categoryLabel: "党团荣誉", publicStatus: "draft", publicStatusLabel: "草稿", displayOrder: 3, visibleFrom: "2026-05-01", visibleTo: "2026-12-31", coverFileId: "file_honor_003", story: "长期协助支部活动组织和材料收集，推动团员青年理论学习。" }
  ],
  students: [
    { id: "stu_001", studentNo: "20220001", name: "赵晨曦", grade: "2022", graduationYear: "2026", major: "软件工程", className: "软件工程2班", politicalStatus: "probationary_party_member", politicalStatusLabel: "预备党员", status: "active", statusText: "在读", phoneMasked: "138****1234", phoneFull: "13800181234", email: "zhaochenxi@example.edu.cn", tags: ["奖学金关注", "党员发展对象"], growthRecords: ["大学生创新训练项目", "学院迎新志愿服务"] },
    { id: "stu_002", studentNo: "20220018", name: "陈一诺", grade: "2022", graduationYear: "2026", major: "软件工程", className: "软件工程2班", politicalStatus: "development_candidate", politicalStatusLabel: "发展对象", status: "active", statusText: "在读", phoneMasked: "139****8818", phoneFull: "13900188818", email: "chenyinuo@example.edu.cn", tags: ["党员发展对象", "班团骨干"], growthRecords: ["团支部理论学习组织", "志愿服务 18 小时"] },
    { id: "stu_003", studentNo: "20260031", name: "林嘉禾", grade: "2026", graduationYear: "2030", major: "数据科学", className: "数据科学1班", politicalStatus: "league_member", politicalStatusLabel: "共青团员", status: "graduating", statusText: "毕业年级", phoneMasked: "137****0631", phoneFull: "13700000631", email: "linjiahe@example.edu.cn", tags: ["就业意向"], growthRecords: ["就业信息二次核验"] },
    { id: "stu_004", studentNo: "20230007", name: "周明远", grade: "2023", graduationYear: "2027", major: "软件工程", className: "软件工程1班", politicalStatus: "league_member", politicalStatusLabel: "共青团员", status: "warning", statusText: "重点关注", phoneMasked: "136****3007", phoneFull: "13600003007", email: "zhoumingyuan@example.edu.cn", tags: ["学业预警"], growthRecords: ["学业帮扶谈话"] }
  ],
  tagCatalog: [
    { id: 1, tagName: "奖学金关注", tagType: "profile", status: "enabled" },
    { id: 2, tagName: "党员发展对象", tagType: "party", status: "enabled" },
    { id: 3, tagName: "就业意向", tagType: "profile", status: "enabled" },
    { id: 4, tagName: "学业预警", tagType: "risk", status: "enabled" }
  ],
  importTasks: [
    { taskNo: "IMP20260512001", status: "processing", statusLabel: "处理中", fileId: "file_import_001", progress: 68, successCount: 184, failCount: 6, errorFileId: "file_import_error_001" }
  ],
  approvals: [
    { id: "approval_001", applicationId: "app_001", applicant: "赵晨曦", type: "certificate", templateId: "tpl_study", typeLabel: "在读证明", priority: "高", status: "reviewing", statusLabel: "待审核", detail: "用途：实习单位提交材料；提交时间：2026-04-18 14:30", attachments: ["file_20260418077"], generatedFile: null },
    { id: "approval_002", applicationId: "party_mat_001", applicant: "陈一诺", type: "party_material", templateId: "party_material", typeLabel: "党团阶段材料", priority: "中", status: "reviewing", statusLabel: "待审核", detail: "需确认季度思想汇报与志愿服务记录。", attachments: ["file_20260417008"], generatedFile: null },
    { id: "approval_003", applicationId: "app_003", applicant: "林嘉禾", type: "seal", templateId: "tpl_seal", typeLabel: "盖章申请", priority: "中", status: "reviewing", statusLabel: "待审核", detail: "就业协议材料需学院盖章确认。", attachments: ["file_employment_001"], generatedFile: null }
  ],
  cadre: {
    scopes: [
      { scopeType: "class", scopeName: "软件工程2班", permissionCodes: ["cadre:party:todo:view", "party:instance:scope:view", "party:todo:remind"] },
      { scopeType: "branch", scopeName: "本科生第一党支部", permissionCodes: ["party:instance:scope:view"] }
    ],
    todos: [
      { studentId: "stu_002", studentName: "陈一诺", stageName: "发展对象", todo: "季度思想汇报未提交", dueAt: "2026-05-18", reminded: false },
      { studentId: "stu_001", studentName: "赵晨曦", stageName: "预备党员", todo: "志愿服务记录需补充", dueAt: "2026-05-20", reminded: false }
    ]
  },
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
  if (["approved", "published", "enabled", "已通过", "成功"].includes(status)) return "is-done";
  if (["reviewing", "pending", "processing", "draft", "审核中", "待审核"].includes(status)) return "is-current";
  if (["supplement_required", "rejected", "revoked", "offline", "warning", "需补充", "退回", "部分失败", "已退回"].includes(status)) return "is-warn";
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

function getCurrentRole() {
  const params = new URLSearchParams(window.location.search);
  const roleCode = params.get("role") || window.sessionStorage.getItem("demoRole") || "student";
  return appData.authUsers.find((item) => item.roleCode === roleCode) || appData.authUsers[0];
}

function hasPermission(permissionCode) {
  return (getCurrentRole().permissions || []).includes(permissionCode);
}

function nowText() {
  return new Date().toLocaleString("zh-CN", { hour12: false });
}

function fileIdFromName(fileName, bizType) {
  const safeName = String(fileName || "mock").replace(/\W+/g, "").slice(-8) || "mock";
  return `file_${bizType}_${safeName}_${Date.now().toString().slice(-5)}`;
}

function bindFileUploaders() {
  document.querySelectorAll("[data-file-uploader]").forEach((uploader) => {
    const input = uploader.querySelector("[data-file-name]");
    const trigger = uploader.querySelector("[data-upload-trigger]");
    const progress = uploader.querySelector("[data-upload-progress]");
    const result = uploader.querySelector("[data-upload-result]");
    if (!input || !trigger || !progress || !result) return;

    trigger.addEventListener("click", () => {
      const fileName = input.value.trim();
      const bizType = uploader.dataset.bizType || "common";
      const valid = /\.(pdf|doc|docx|xls|xlsx|png|jpg|jpeg)$/i.test(fileName);
      result.hidden = false;
      if (!fileName || !valid) {
        progress.style.width = "0%";
        result.textContent = "上传失败：文件类型不符合要求，requestId=REQ_FILE_TYPE_0001。";
        result.classList.add("is-error");
        uploader.dataset.fileId = "";
        return;
      }
      progress.style.width = "100%";
      const fileId = fileIdFromName(fileName, bizType);
      uploader.dataset.fileId = fileId;
      result.classList.remove("is-error");
      result.textContent = `上传成功：${fileId}，业务接口将只提交 fileId。`;

      const nextInput = uploader.nextElementSibling?.querySelector?.("input[name='fileId']");
      if (nextInput) nextInput.value = fileId;
    });
  });
}

function renderStudentPage() {
  setText("todoCount", appData.materials.filter((item) => item.reviewStatus !== "approved").length + appData.applications.filter((item) => item.status === "reviewing").length);
  setText("unreadCount", appData.notices.filter((notice) => !notice.read).length);
  setText("growthCount", appData.growthRecords.length);
  const role = getCurrentRole();
  setText("studentRoleCode", role.roleCode);
  setText("studentRoleLabel", role.roleLabel);

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
  renderApplicationTemplateOptions();
  bindApplicationForm();
  bindApplicationFilters();
  renderApplications();
  renderProfileDetail();
  renderGrowthRecords();
  bindHonorFilters();
  renderHonors("honorList", false);
  renderCadreExperience();
  bindFileUploaders();
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
    const fileId = formData.get("fileId") || "file_pending_upload";
    appData.materials.unshift({
      name: formData.get("materialName") || "补充材料",
      fileId,
      fileName: `${fileId}.docx`,
      submitStatus: "submitted",
      reviewStatus: "pending",
      reviewStatusLabel: "待审核",
      submittedAt: nowText()
    });
    form.reset();
    renderMaterials();
    setText("todoCount", appData.materials.filter((item) => item.reviewStatus !== "approved").length + appData.applications.filter((item) => item.status === "reviewing").length);
  });
}

function renderApplicationTemplateOptions() {
  const select = document.getElementById("applicationTemplate");
  const fields = document.getElementById("applicationDynamicFields");
  if (!select || !fields) return;

  select.innerHTML = appData.certificateTemplates
    .map((template) => `<option value="${template.id}">${template.templateName}</option>`)
    .join("");

  function updateFields() {
    const template = appData.certificateTemplates.find((item) => item.id === select.value) || appData.certificateTemplates[0];
    fields.innerHTML = template.formSchemaJson
      .map((field) => {
        if (field.type === "select") {
          const options = field.options.map((option) => `<option value="${option}">${option}</option>`).join("");
          return `<label><span>${field.label}</span><select name="field_${field.name}" class="input input--select">${options}</select></label>`;
        }
        return `<label><span>${field.label}</span><input name="field_${field.name}" class="input" type="${field.type}" placeholder="${field.placeholder || ""}"></label>`;
      })
      .join("");
  }

  select.addEventListener("change", updateFields);
  updateFields();
}

function bindApplicationForm() {
  const form = document.getElementById("applicationForm");
  const feedback = document.getElementById("applicationFeedback");
  if (!form || !feedback) return;
  form.addEventListener("submit", (event) => {
    event.preventDefault();
    const formData = new FormData(form);
    const template = appData.certificateTemplates.find((item) => item.id === formData.get("templateId")) || appData.certificateTemplates[0];
    const formValues = {};
    template.formSchemaJson.forEach((field) => {
      formValues[field.name] = formData.get(`field_${field.name}`) || "";
    });
    const uploader = form.querySelector("[data-file-uploader]");
    const attachmentFileIds = uploader?.dataset.fileId ? [uploader.dataset.fileId] : [];
    appData.applications.unshift({
      id: `app_${Date.now().toString().slice(-6)}`,
      no: `APP${Date.now().toString().slice(-10)}`,
      templateId: template.id,
      type: template.type,
      typeLabel: template.templateName,
      status: "reviewing",
      statusLabel: "审核中",
      approver: "辅导员 李老师",
      applicant: appData.profile.name,
      applicantSummary: `${appData.profile.grade}级 ${appData.profile.className} ${appData.profile.politicalStatusLabel}`,
      purpose: formData.get("purpose") || "未填写",
      createdAt: nowText(),
      formData: formValues,
      attachmentFileIds,
      generatedFile: null,
      approvalRecords: [
        { nodeName: "提交申请", operator: appData.profile.name, action: "submit", opinion: formData.get("remark") || "提交院内申请", operatedAt: nowText() },
        { nodeName: "审批处理", operator: template.approvalRule, action: "pending", opinion: "待处理", operatedAt: "-" }
      ]
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
  const typeFilter = document.getElementById("applicationTypeFilter");
  const statusFilter = document.getElementById("applicationStatusFilter");
  target.innerHTML = "";
  const result = appData.applications.filter((item) => {
    const typeMatched = !typeFilter || typeFilter.value === "all" || item.type === typeFilter.value;
    const statusMatched = !statusFilter || statusFilter.value === "all" || item.status === statusFilter.value;
    return typeMatched && statusMatched;
  });
  if (!result.length) {
    target.appendChild(createElement("div", "empty-state", "暂无符合筛选条件的申请。"));
    return;
  }
  result.forEach((item) => {
    const node =
      createElement(
        "article",
        `record ${statusClass(item.status)}`,
        `<div class="record__meta">${item.no} · ${item.createdAt}</div>
         <h3>${item.typeLabel}</h3>
         <p>用途：${item.purpose}</p>
         <div class="tag-group"><span class="pill">${item.statusLabel}</span><span class="tag">${item.approver}</span></div>`
      );
    const actions = createElement("div", "topbar__actions");
    const detail = createElement("button", "button", "查看详情");
    detail.addEventListener("click", () => renderApplicationDetail(item));
    actions.appendChild(detail);
    if (item.status === "reviewing") {
      const revoke = createElement("button", "button", "撤回申请");
      revoke.addEventListener("click", () => {
        const reason = window.prompt("请输入撤回原因", "材料需重新整理");
        if (!reason) return;
        item.status = "revoked";
        item.statusLabel = "已撤回";
        item.approvalRecords.push({ nodeName: "撤回申请", operator: appData.profile.name, action: "revoke", opinion: reason, operatedAt: nowText() });
        renderApplications();
        renderApplicationDetail(item);
      });
      actions.appendChild(revoke);
    }
    node.appendChild(actions);
    target.appendChild(node);
  });
}

function bindApplicationFilters() {
  ["applicationTypeFilter", "applicationStatusFilter"].forEach((id) => {
    const node = document.getElementById(id);
    if (node) node.addEventListener("change", renderApplications);
  });
}

function renderApplicationDetail(item) {
  const target = document.getElementById("applicationDetail");
  if (!target) return;
  const fields = Object.entries(item.formData || {}).map(([key, value]) => `<div><span>${key}</span><strong>${value || "-"}</strong></div>`).join("");
  const records = item.approvalRecords.map((record) => `<li>${record.operatedAt} · ${record.nodeName} · ${record.operator} · ${record.opinion}</li>`).join("");
  const fileLine = item.generatedFile ? `${item.generatedFile.fileName} (${item.generatedFile.fileId})` : "暂未生成";
  target.hidden = false;
  target.innerHTML = `
    <div class="section-head"><h3>${item.no} 申请详情</h3><span class="pill">${item.statusLabel}</span></div>
    <div class="info-list">${fields}<div><span>生成文件</span><strong>${fileLine}</strong></div></div>
    <h4>审批记录</h4>
    <ul class="feature-list">${records}</ul>
  `;
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
  const yearFilter = document.getElementById(adminMode ? "honorAdminYearFilter" : "honorYearFilter");
  const categoryFilter = document.getElementById("honorCategoryFilter");
  const ownerFilter = document.getElementById("honorOwnerTypeFilter");
  const statusFilter = document.getElementById("honorAdminStatusFilter");
  const result = appData.honors.filter((item) => {
    const yearMatched = !yearFilter || yearFilter.value === "all" || item.year === yearFilter.value;
    const categoryMatched = adminMode || !categoryFilter || categoryFilter.value === "all" || item.category === categoryFilter.value;
    const ownerMatched = adminMode || !ownerFilter || ownerFilter.value === "all" || item.ownerType === ownerFilter.value;
    const statusMatched = !statusFilter || statusFilter.value === "all" || item.publicStatus === statusFilter.value;
    return yearMatched && categoryMatched && ownerMatched && statusMatched;
  });
  if (!result.length) {
    target.appendChild(createElement("div", "empty-state", "暂无符合条件的荣誉记录。"));
    return;
  }
  result.forEach((item) => {
    const node =
      createElement(
        "article",
        "card honor-card",
        `<div class="honor-cover">${item.categoryLabel.slice(0, 2)}</div>
         <div class="card__meta">${item.year} · ${item.categoryLabel} · ${item.ownerTypeLabel}</div>
         <h3>${item.title}</h3>
         <p><strong>${item.owner}</strong></p>
         <p>${item.story}</p>
         <div class="tag-group"><span class="pill ${statusClass(item.publicStatus)}">${item.publicStatusLabel}</span><span class="tag">排序 ${item.displayOrder}</span></div>`
      );
    const actions = createElement("div", "topbar__actions");
    const detail = createElement("button", "button", adminMode ? "编辑" : "查看详情");
    detail.addEventListener("click", () => (adminMode ? renderHonorAdminEditor(item) : renderHonorDetail(item)));
    actions.appendChild(detail);
    if (adminMode) {
      const toggle = createElement("button", "button", item.publicStatus === "published" ? "下线" : "发布");
      toggle.addEventListener("click", () => {
        item.publicStatus = item.publicStatus === "published" ? "offline" : "published";
        item.publicStatusLabel = item.publicStatus === "published" ? "已发布" : "已下线";
        renderHonors(targetId, adminMode);
      });
      actions.appendChild(toggle);
    }
    node.appendChild(actions);
    target.appendChild(node);
  });
}

function bindHonorFilters() {
  ["honorYearFilter", "honorCategoryFilter", "honorOwnerTypeFilter"].forEach((id) => {
    const node = document.getElementById(id);
    if (node) node.addEventListener("change", () => renderHonors("honorList", false));
  });
}

function bindHonorAdminFilters() {
  ["honorAdminYearFilter", "honorAdminStatusFilter"].forEach((id) => {
    const node = document.getElementById(id);
    if (node) node.addEventListener("change", () => renderHonors("honorAdminList", true));
  });
}

function renderHonorDetail(item) {
  const target = document.getElementById("honorDetail");
  if (!target) return;
  target.hidden = false;
  target.innerHTML = `
    <div class="section-head"><h3>${item.title}</h3><span class="pill">${item.year}</span></div>
    <div class="info-list">
      <div><span>获奖对象</span><strong>${item.owner}</strong></div>
      <div><span>类别</span><strong>${item.categoryLabel}</strong></div>
      <div><span>图片 fileId</span><strong>${item.coverFileId}</strong></div>
    </div>
    <p>${item.story}</p>
  `;
}

function renderHonorAdminEditor(item) {
  const target = document.getElementById("honorAdminEditor");
  if (!target) return;
  target.hidden = false;
  target.innerHTML = `
    <div class="section-head"><h3>${item.title}</h3><span class="pill">${item.publicStatusLabel}</span></div>
    <div class="form admin-form-grid">
      <label><span>展示排序</span><input class="input" value="${item.displayOrder}"></label>
      <label><span>展示开始</span><input class="input" value="${item.visibleFrom}"></label>
      <label><span>展示结束</span><input class="input" value="${item.visibleTo}"></label>
      <label><span>图片 fileId</span><input class="input" value="${item.coverFileId}"></label>
    </div>
    <p class="subtle-note">此处为荣誉新增/编辑表单模型，正式实现时通过 POST /honors 保存。</p>
  `;
}

function renderCadreExperience() {
  const nav = document.getElementById("cadreNav");
  const scopeList = document.getElementById("cadreScopeList");
  const todoList = document.getElementById("cadreTodoStudentList");
  const canViewCadre = hasPermission("cadre:party:todo:view") || hasPermission("party:instance:scope:view");
  if (nav) nav.classList.toggle("is-hidden", !canViewCadre);
  if (!canViewCadre || !scopeList || !todoList) return;

  scopeList.innerHTML = "";
  appData.cadre.scopes.forEach((scope) => {
    scopeList.appendChild(
      createElement(
        "article",
        "record",
        `<div class="record__meta">${scope.scopeType}</div>
         <h3>${scope.scopeName}</h3>
         <p>${scope.permissionCodes.join("、")}</p>`
      )
    );
  });

  todoList.innerHTML = "";
  appData.cadre.todos.forEach((todo) => {
    const node = createElement(
      "article",
      "record",
      `<div class="record__meta">${todo.stageName} · 截止 ${todo.dueAt}</div>
       <h3>${todo.studentName}</h3>
       <p>${todo.todo}</p>
       <div class="tag-group"><span class="pill pill--warn">不可审批</span><span class="tag">GET /party/instances/students/${todo.studentId}</span></div>`
    );
    const actions = createElement("div", "topbar__actions");
    const remind = createElement("button", "button button--primary", todo.reminded ? "已催办" : "发送催办");
    remind.disabled = todo.reminded || !hasPermission("party:todo:remind");
    remind.addEventListener("click", () => {
      todo.reminded = true;
      renderCadreExperience();
    });
    actions.appendChild(remind);
    node.appendChild(actions);
    todoList.appendChild(node);
  });
}

function renderAdminPage() {
  setText("pendingApprovalCount", appData.approvals.length);
  setText("todayPushCount", appData.notices.length);
  renderAdminTodos();
  renderStudentTable();
  bindStudentImportExport();
  renderAdminKnowledge();
  renderPartyReviews();
  bindNoticePublishForm();
  renderNoticeStats();
  bindApprovalFilters();
  renderApprovals();
  bindHonorAdminFilters();
  renderHonors("honorAdminList", true);
  renderAuditLogs();
  renderSystemLogs();
  bindFileUploaders();
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
  const grade = document.getElementById("studentGradeFilter");
  const major = document.getElementById("studentMajorFilter");
  const political = document.getElementById("studentPoliticalFilter");
  const filter = document.getElementById("studentStatusFilter");
  const pager = document.getElementById("studentPager");
  if (!target || !search || !filter) return;
  const pageSize = 3;

  function update() {
    const keyword = search.value.trim().toLowerCase();
    const status = filter.value;
    const gradeValue = grade?.value || "all";
    const majorValue = major?.value || "all";
    const politicalValue = political?.value || "all";
    const result = appData.students.filter((student) => {
      const searchable = `${student.studentNo} ${student.name} ${student.grade} ${student.major} ${student.className} ${student.politicalStatusLabel} ${student.graduationYear} ${student.tags.join(" ")}`.toLowerCase();
      return (!keyword || searchable.includes(keyword))
        && (status === "all" || student.status === status)
        && (gradeValue === "all" || student.grade === gradeValue)
        && (majorValue === "all" || student.major === majorValue)
        && (politicalValue === "all" || student.politicalStatus === politicalValue);
    });
    const pageItems = result.slice(0, pageSize);
    target.innerHTML = `
      <div class="table-row table-head"><span>学号</span><span>姓名</span><span>班级</span><span>政治面貌</span><span>标签</span><span>操作</span></div>
    `;
    if (!pageItems.length) {
      target.appendChild(createElement("div", "empty-state", "暂无学生数据，或筛选条件过窄。"));
    }
    pageItems.forEach((item) => {
      const row = createElement("div", "table-row", `<span>${item.studentNo}</span><strong>${item.name}</strong><span>${item.className}</span><span>${item.politicalStatusLabel}</span><span>${item.tags.join("、")}</span>`);
      const action = createElement("span");
      const detail = createElement("button", "button", "详情");
      detail.addEventListener("click", () => renderStudentDetail(item));
      action.appendChild(detail);
      row.appendChild(action);
      target.appendChild(row);
    });
    if (pager) {
      const totalPages = Math.max(1, Math.ceil(result.length / pageSize));
      pager.innerHTML = `<span>第 1 / ${totalPages} 页</span><span>当前显示 ${pageItems.length} 条，共 ${result.length} 条</span>`;
    }
  }

  search.addEventListener("input", update);
  filter.addEventListener("change", update);
  [grade, major, political].forEach((node) => node?.addEventListener("change", update));
  update();
}

function renderStudentDetail(student) {
  const target = document.getElementById("studentDetailPanel");
  if (!target) return;
  target.hidden = false;
  target.innerHTML = `
    <div class="section-head">
      <h3>${student.name} 学生详情</h3>
      <div class="topbar__actions">
        <button id="showSensitiveButton" class="button">查看完整联系方式</button>
        <button id="updateTagsButton" class="button">更新标签</button>
        <button id="addGrowthButton" class="button">新增成长记录</button>
      </div>
    </div>
    <div class="info-list">
      <div><span>学号</span><strong>${student.studentNo}</strong></div>
      <div><span>年级专业</span><strong>${student.grade}级 ${student.major}</strong></div>
      <div><span>班级</span><strong>${student.className}</strong></div>
      <div><span>毕业年级</span><strong>${student.graduationYear}</strong></div>
      <div><span>政治面貌</span><strong>${student.politicalStatusLabel}</strong></div>
      <div><span>联系方式</span><strong id="studentPhoneValue">${student.phoneMasked}</strong></div>
      <div><span>邮箱</span><strong>${student.email}</strong></div>
      <div><span>标签</span><strong id="studentTagValue">${student.tags.join("、")}</strong></div>
    </div>
    <h4>成长记录</h4>
    <ul class="feature-list">${student.growthRecords.map((item) => `<li>${item}</li>`).join("")}</ul>
    <p id="sensitiveAuditTip" class="feedback" hidden>本次敏感信息访问已记录。</p>
  `;
  document.getElementById("showSensitiveButton")?.addEventListener("click", () => {
    setText("studentPhoneValue", student.phoneFull);
    document.getElementById("sensitiveAuditTip").hidden = false;
    appData.logs.unshift({ actor: "辅导员 李老师", module: "学生画像", action: `查看学生敏感字段：${student.name} 联系方式`, time: nowText(), result: "成功" });
    renderAuditLogs();
  });
  document.getElementById("updateTagsButton")?.addEventListener("click", () => {
    if (!student.tags.includes("重点跟进")) student.tags.push("重点跟进");
    setText("studentTagValue", student.tags.join("、"));
    renderStudentTable();
  });
  document.getElementById("addGrowthButton")?.addEventListener("click", () => {
    student.growthRecords.unshift(`新增成长记录：证明附件 fileId=${fileIdFromName(student.studentNo, "growth_record")}`);
    renderStudentDetail(student);
  });
}

function bindStudentImportExport() {
  const importButton = document.getElementById("studentImportButton");
  const exportButton = document.getElementById("studentExportButton");
  const panel = document.getElementById("studentImportPanel");
  if (importButton && panel) {
    importButton.addEventListener("click", () => {
      const task = appData.importTasks[0];
      panel.hidden = false;
      panel.innerHTML = `
        <div class="section-head"><h3>Excel 导入任务</h3><span class="pill">${task.statusLabel}</span></div>
        <div class="upload-progress"><span style="width:${task.progress}%"></span></div>
        <p>任务号 ${task.taskNo} · 成功 ${task.successCount} · 失败 ${task.failCount} · 错误文件 ${task.errorFileId}</p>
      `;
    });
  }
  if (exportButton && panel) {
    exportButton.addEventListener("click", () => {
      panel.hidden = false;
      panel.innerHTML = `
        <div class="section-head"><h3>学生信息导出</h3><span class="pill">GET /students/export</span></div>
        <p>导出将按当前筛选条件执行，并受当前用户 department 数据范围限制。</p>
      `;
    });
  }
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
  const typeFilter = document.getElementById("approvalTypeFilter");
  const statusFilter = document.getElementById("approvalStatusFilter");
  const templateFilter = document.getElementById("approvalTemplateFilter");
  if (templateFilter && templateFilter.options.length <= 1) {
    appData.certificateTemplates.forEach((template) => {
      templateFilter.appendChild(createElement("option", "", template.templateName));
      templateFilter.lastElementChild.value = template.id;
    });
    templateFilter.appendChild(createElement("option", "", "党团材料"));
    templateFilter.lastElementChild.value = "party_material";
  }
  target.innerHTML = "";
  const result = appData.approvals.filter((item) => {
    const typeMatched = !typeFilter || typeFilter.value === "all" || item.type === typeFilter.value;
    const statusMatched = !statusFilter || statusFilter.value === "all" || item.status === statusFilter.value;
    const templateMatched = !templateFilter || templateFilter.value === "all" || item.templateId === templateFilter.value;
    return typeMatched && statusMatched && templateMatched;
  });
  if (!result.length) {
    target.appendChild(createElement("div", "empty-state", "暂无待审批数据。"));
    return;
  }
  result.forEach((item) => {
    const node = createElement("article", "record", `<div class="record__meta">${item.statusLabel} · 优先级 ${item.priority}</div><h3>${item.applicant} - ${item.typeLabel}</h3><p>${item.detail}</p>`);
    const actions = createElement("div", "topbar__actions");
    const detail = createElement("button", "button", "详情");
    const approve = createElement("button", "button button--primary", "通过");
    const reject = createElement("button", "button", "退回");
    detail.addEventListener("click", () => renderApprovalDetail(item));
    approve.addEventListener("click", () => {
      const opinion = window.prompt("请输入审批意见", "材料齐全，同意办理");
      if (!opinion) return;
      const index = appData.approvals.findIndex((approval) => approval.id === item.id);
      if (index >= 0) appData.approvals.splice(index, 1);
      renderApprovals();
      renderAdminTodos();
      setText("pendingApprovalCount", appData.approvals.length);
    });
    reject.addEventListener("click", () => {
      const opinion = window.prompt("请输入驳回意见", "请补充材料后重新提交");
      if (!opinion) return;
      item.status = "rejected";
      item.statusLabel = "已退回";
      node.classList.add("is-warn");
      node.querySelector(".record__meta").textContent = `${item.statusLabel} · 优先级 ${item.priority}`;
      renderApprovalDetail(item);
    });
    actions.appendChild(detail);
    actions.appendChild(approve);
    actions.appendChild(reject);
    node.appendChild(actions);
    target.appendChild(node);
  });
}

function bindApprovalFilters() {
  ["approvalTypeFilter", "approvalStatusFilter", "approvalTemplateFilter"].forEach((id) => {
    const node = document.getElementById(id);
    if (node) node.addEventListener("change", renderApprovals);
  });
}

function renderApprovalDetail(item) {
  const target = document.getElementById("approvalDetailPanel");
  if (!target) return;
  const application = appData.applications.find((record) => record.id === item.applicationId);
  const applicantSummary = application?.applicantSummary || "授权范围内申请人";
  const generatedFile = item.generatedFile || application?.generatedFile;
  target.hidden = false;
  target.innerHTML = `
    <div class="section-head"><h3>${item.applicant} - ${item.typeLabel}</h3><span class="pill">${item.statusLabel}</span></div>
    <div class="info-list">
      <div><span>申请人摘要</span><strong>${applicantSummary}</strong></div>
      <div><span>附件</span><strong>${item.attachments.join("、") || "无"}</strong></div>
      <div><span>生成文件</span><strong>${generatedFile ? `${generatedFile.fileName} / ${generatedFile.fileId}` : "暂未生成"}</strong></div>
    </div>
    <label class="form"><span>审批意见</span><textarea class="input textarea" rows="3" placeholder="通过或驳回时需要填写审批意见"></textarea></label>
    <p class="subtle-note">若接口返回 409 状态冲突，前端应刷新详情并提示当前申请已被其他审批人处理。</p>
  `;
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
    window.sessionStorage.setItem("demoRole", matched.roleCode);
    window.setTimeout(() => {
      window.location.href = `${matched.redirect}?role=${matched.roleCode}`;
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
