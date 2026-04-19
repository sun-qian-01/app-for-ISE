const appData = {
  knowledge: [
    {
      title: "国家奖学金评定流程说明",
      category: "奖助",
      summary: "包含申请资格、名额分配、材料清单和公示流程。",
      keywords: ["奖学金", "国家奖学金", "评定"]
    },
    {
      title: "休学与复学办理指南",
      category: "学籍",
      summary: "说明休学申请条件、复学材料和学院审核路径。",
      keywords: ["休学", "复学", "学籍"]
    },
    {
      title: "党员发展阶段材料清单",
      category: "党团",
      summary: "汇总积极分子、发展对象、预备党员各阶段所需材料。",
      keywords: ["党员", "党团", "材料"]
    },
    {
      title: "在读证明与成绩证明模板下载",
      category: "证明",
      summary: "提供常用证明模板、用途示例和线上审批说明。",
      keywords: ["证明", "模板", "在读证明"]
    }
  ],
  notices: [
    {
      title: "2026 年春季学期奖学金材料提交通知",
      audience: "2022级 本科生",
      date: "2026-04-18",
      read: false,
      content: "请于 4 月 24 日前完成材料提交，逾期系统将自动关闭入口。"
    },
    {
      title: "预备党员季度思想汇报提醒",
      audience: "党员发展对象",
      date: "2026-04-17",
      read: false,
      content: "你所在支部需于本周内补齐季度思想汇报，请及时上传。"
    },
    {
      title: "毕业生就业信息登记更新说明",
      audience: "2026届 毕业生",
      date: "2026-04-15",
      read: true,
      content: "就业去向信息已开放二次更新，请在学院平台完成信息核验。"
    }
  ],
  process: [
    { name: "申请人", state: "done" },
    { name: "积极分子", state: "done" },
    { name: "发展对象", state: "done" },
    { name: "预备党员", state: "current" },
    { name: "正式党员", state: "pending" }
  ],
  certificates: [
    {
      type: "在读证明",
      status: "审核中",
      createdAt: "2026-04-18 14:30",
      purpose: "实习单位提交材料"
    },
    {
      type: "成绩证明",
      status: "已通过",
      createdAt: "2026-04-10 09:12",
      purpose: "交换项目报名"
    }
  ],
  approvals: [
    {
      applicant: "赵晨曦",
      type: "在读证明",
      priority: "高",
      status: "待审核",
      detail: "用途：实习单位提交材料；提交时间：2026-04-18 14:30"
    },
    {
      applicant: "陈一诺",
      type: "党员材料补录",
      priority: "中",
      status: "待审核",
      detail: "需确认季度思想汇报与志愿服务记录。"
    }
  ],
  studentSegments: [
    {
      title: "就业意向人群",
      summary: "共 86 人，适合推送实习、宣讲会和毕业手续信息。"
    },
    {
      title: "党员发展对象",
      summary: "共 34 人，需重点关注阶段节点、材料补录和支部活动通知。"
    },
    {
      title: "学业预警关注",
      summary: "共 12 人，需跟进成绩单解析和学分完成情况。"
    }
  ],
  logs: [
    {
      actor: "辅导员 李老师",
      action: "导出 2022级 奖学金候选名单",
      time: "2026-04-19 09:12"
    },
    {
      actor: "超级管理员",
      action: "调整角色权限：新增证明审批菜单",
      time: "2026-04-18 17:43"
    },
    {
      actor: "辅导员 李老师",
      action: "查看学生敏感字段：联系方式",
      time: "2026-04-18 15:26"
    }
  ]
};

function createElement(tag, className, html) {
  const element = document.createElement(tag);
  if (className) element.className = className;
  if (html !== undefined) element.innerHTML = html;
  return element;
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
      const target = button.dataset.jump;
      document.querySelector(`.nav__item[data-target="${target}"]`)?.click();
    });
  });
}

function renderStudentPage() {
  document.getElementById("todoCount").textContent = String(
    appData.certificates.filter((item) => item.status === "审核中").length + 1
  );
  document.getElementById("unreadCount").textContent = String(
    appData.notices.filter((notice) => !notice.read).length
  );

  const progressMini = document.getElementById("progressMini");
  appData.process.forEach((item) => {
    const chip = createElement("span", `stage-chip ${stateClass(item.state)}`, item.name);
    progressMini.appendChild(chip);
  });

  const noticePreview = document.getElementById("noticePreview");
  appData.notices.slice(0, 2).forEach((notice) => {
    noticePreview.appendChild(renderNoticeCard(notice));
  });

  const knowledgeList = document.getElementById("knowledgeList");
  const knowledgeSearch = document.getElementById("knowledgeSearch");
  const knowledgeCategory = document.getElementById("knowledgeCategory");

  function updateKnowledge() {
    const keyword = knowledgeSearch.value.trim().toLowerCase();
    const category = knowledgeCategory.value;
    knowledgeList.innerHTML = "";

    const result = appData.knowledge.filter((item) => {
      const matchCategory = category === "all" || item.category === category;
      const targetText = `${item.title} ${item.summary} ${item.keywords.join(" ")}`.toLowerCase();
      const matchKeyword = !keyword || targetText.includes(keyword);
      return matchCategory && matchKeyword;
    });

    if (!result.length) {
      knowledgeList.appendChild(createElement("div", "record", "未找到匹配条目。"));
      return;
    }

    result.forEach((item) => {
      const node = createElement(
        "article",
        "knowledge-item",
        `<div class="notice-card__meta">${item.category}</div><h3>${item.title}</h3><p>${item.summary}</p>`
      );
      knowledgeList.appendChild(node);
    });
  }

  knowledgeSearch.addEventListener("input", updateKnowledge);
  knowledgeCategory.addEventListener("change", updateKnowledge);
  updateKnowledge();

  const timeline = document.getElementById("timeline");
  appData.process.forEach((item) => {
    const node = createElement(
      "div",
      `timeline__item ${stateClass(item.state)}`,
      `<strong>${item.name}</strong><span>${stateText(item.state)}</span>`
    );
    timeline.appendChild(node);
  });

  const noticeList = document.getElementById("noticeList");
  const markAllRead = document.getElementById("markAllRead");

  function updateNotices() {
    noticeList.innerHTML = "";
    appData.notices.forEach((notice, index) => {
      const card = renderNoticeCard(notice, true);
      const action = createElement("button", "button", notice.read ? "已读" : "标记已读");
      action.disabled = notice.read;
      action.addEventListener("click", () => {
        appData.notices[index].read = true;
        updateNotices();
        document.getElementById("unreadCount").textContent = String(
          appData.notices.filter((item) => !item.read).length
        );
      });
      card.appendChild(action);
      noticeList.appendChild(card);
    });
  }

  markAllRead.addEventListener("click", () => {
    appData.notices.forEach((notice) => {
      notice.read = true;
    });
    updateNotices();
    document.getElementById("unreadCount").textContent = "0";
  });
  updateNotices();

  const certificateList = document.getElementById("certificateList");
  const feedback = document.getElementById("certificateFeedback");
  const form = document.getElementById("certificateForm");

  function updateCertificates() {
    certificateList.innerHTML = "";
    appData.certificates.forEach((item) => {
      const node = createElement(
        "article",
        "record",
        `<div class="record__meta">${item.createdAt}</div><h3>${item.type}</h3><p>用途：${item.purpose}</p><span class="pill">${item.status}</span>`
      );
      certificateList.appendChild(node);
    });
  }

  form.addEventListener("submit", (event) => {
    event.preventDefault();
    const formData = new FormData(form);
    appData.certificates.unshift({
      type: formData.get("type"),
      status: "审核中",
      createdAt: new Date().toLocaleString("zh-CN", { hour12: false }),
      purpose: formData.get("purpose") || "未填写"
    });
    updateCertificates();
    feedback.hidden = false;
    feedback.textContent = "申请已提交，当前状态为“审核中”。";
    form.reset();
    document.getElementById("todoCount").textContent = String(
      appData.certificates.filter((item) => item.status === "审核中").length + 1
    );
  });

  updateCertificates();
}

function renderNoticeCard(notice, full = false) {
  return createElement(
    "article",
    `notice-card ${notice.read ? "" : "is-unread"}`.trim(),
    `
      <div class="notice-card__meta">${notice.date} · ${notice.audience}</div>
      <h3>${notice.title}</h3>
      <p>${full ? notice.content : notice.content.slice(0, 36) + "..."}</p>
    `
  );
}

function renderAdminPage() {
  document.getElementById("pendingApprovalCount").textContent = String(appData.approvals.length);

  const adminTodoList = document.getElementById("adminTodoList");
  appData.approvals.forEach((item) => {
    adminTodoList.appendChild(
      createElement(
        "article",
        "record",
        `<div class="record__meta">优先级：${item.priority}</div><h3>${item.type}</h3><p>${item.applicant} · ${item.detail}</p>`
      )
    );
  });

  const approvalList = document.getElementById("approvalList");
  appData.approvals.forEach((item) => {
    const node = createElement(
      "article",
      "record",
      `<div class="record__meta">${item.status} · 优先级 ${item.priority}</div><h3>${item.applicant} - ${item.type}</h3><p>${item.detail}</p>`
    );
    const actions = createElement("div", "topbar__actions");
    actions.appendChild(createElement("button", "button button--primary", "通过"));
    actions.appendChild(createElement("button", "button", "退回"));
    node.appendChild(actions);
    approvalList.appendChild(node);
  });

  const knowledgeAdminList = document.getElementById("knowledgeAdminList");
  appData.knowledge.forEach((item) => {
    knowledgeAdminList.appendChild(
      createElement(
        "article",
        "knowledge-item",
        `<div class="notice-card__meta">${item.category}</div><h3>${item.title}</h3><p>${item.summary}</p>`
      )
    );
  });

  const studentSegmentList = document.getElementById("studentSegmentList");
  appData.studentSegments.forEach((item) => {
    studentSegmentList.appendChild(
      createElement("article", "record", `<h3>${item.title}</h3><p>${item.summary}</p>`)
    );
  });

  const auditLogList = document.getElementById("auditLogList");
  appData.logs.forEach((item) => {
    auditLogList.appendChild(
      createElement(
        "article",
        "log-item",
        `<div class="record__meta">${item.time}</div><h3>${item.actor}</h3><p>${item.action}</p>`
      )
    );
  });
}

function stateClass(state) {
  if (state === "done") return "is-done";
  if (state === "current") return "is-current";
  return "";
}

function stateText(state) {
  if (state === "done") return "已完成";
  if (state === "current") return "当前阶段";
  return "未开始";
}

function main() {
  bindNavigation();
  const page = document.body.dataset.page;
  if (page === "student") renderStudentPage();
  if (page === "admin") renderAdminPage();
}

main();
