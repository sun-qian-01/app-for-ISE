export const studentDashboard = {
  todoCount: 4,
  unreadNoticeCount: 2,
  currentPartyStage: "预备党员",
  growthCount: 3,
};

export const profile = {
  studentNo: "20220001",
  name: "赵晨曦",
  grade: "2022",
  major: "软件工程",
  className: "软件工程2班",
  politicalStatusLabel: "预备党员",
  phoneMasked: "138****1234",
  email: "zhaochenxi@example.edu.cn",
  counselor: "李老师",
  statusLabel: "在读",
  tags: ["2022级", "软件工程", "development_candidate", "奖学金关注", "就业意向"],
  growthRecords: [
    { typeLabel: "科研竞赛", title: "大学生创新训练项目", date: "2025-11", summary: "院级立项，负责需求分析与原型设计。" },
    { typeLabel: "志愿服务", title: "学院迎新志愿服务", date: "2026-04", summary: "累计服务 8 小时，完成新生咨询与路线引导。" },
    { typeLabel: "干部任职", title: "软件工程2班学习委员", date: "2024-09 至今", summary: "协助课程通知、作业收集和学业帮扶。" },
  ],
};

export const qaArticles = [
  {
    title: "国家奖学金评定流程说明",
    categoryLabel: "奖助",
    version: "v3",
    publishStatus: "published",
    summary: "包含申请资格、名额分配、材料清单和公示流程。",
    source: "国家奖学金评定办法.pdf",
    keywords: ["奖学金", "国家奖学金"],
  },
  {
    title: "休学与复学办理指引",
    categoryLabel: "学籍",
    version: "v2",
    publishStatus: "published",
    summary: "说明休学申请条件、复学材料和学院审核路径。",
    source: "学籍异动办理指南.docx",
    keywords: ["休学", "复学"],
  },
  {
    title: "党员发展阶段材料清单",
    categoryLabel: "党团",
    version: "v4",
    publishStatus: "published",
    summary: "汇总积极分子、发展对象、预备党员各阶段所需材料。",
    source: "党员发展材料清单.xlsx",
    keywords: ["党员", "思想汇报"],
  },
  {
    title: "在读证明与成绩证明办理说明",
    categoryLabel: "证明",
    version: "v2",
    publishStatus: "published",
    summary: "说明在读证明、成绩证明的申请场景、用途填写要求、附件上传和生成文件领取方式。",
    source: "学生证明办理指南.pdf",
    keywords: ["证明", "在读证明", "成绩证明"],
  },
];

export const kbTemplates = [
  {
    name: "在读证明申请模板",
    categoryLabel: "证明",
    fileType: "docx",
    updatedAt: "2026-04-18",
    description: "用于校外实习、报名或签证材料准备。",
  },
  {
    name: "国家奖学金材料清单模板",
    categoryLabel: "奖助",
    fileType: "xlsx",
    updatedAt: "2026-04-16",
    description: "包含成绩、综测、获奖和附件核对项。",
  },
  {
    name: "思想汇报撰写模板",
    categoryLabel: "党团",
    fileType: "docx",
    updatedAt: "2026-04-12",
    description: "适用于积极分子、发展对象和预备党员阶段。",
  },
];

export const partyStages = [
  { name: "入党申请人", status: "approved", dueAt: "2025-09-30" },
  { name: "积极分子", status: "approved", dueAt: "2025-12-20" },
  { name: "发展对象", status: "approved", dueAt: "2026-03-20" },
  { name: "预备党员", status: "reviewing", dueAt: "2026-04-25" },
  { name: "正式党员", status: "pending", dueAt: "2027-04-25" },
];

export const notices = [
  {
    id: 101,
    title: "2026 年春季学期奖学金材料提交通知",
    audience: "2022级 + 奖学金关注",
    date: "2026-04-18",
    channelLabels: ["站内", "邮件", "微信"],
    read: false,
    statusLabel: "未读",
    tags: ["奖助", "材料提交"],
    stats: { delivered: 268, read: 201 },
    content: "请于 4 月 24 日前完成材料提交，逾期系统将自动关闭入口。",
  },
  {
    id: 102,
    title: "预备党员季度思想汇报提醒",
    audience: "党员发展对象",
    date: "2026-04-17",
    channelLabels: ["站内", "微信"],
    read: false,
    statusLabel: "未读",
    tags: ["党团", "材料提醒"],
    stats: { delivered: 71, read: 46 },
    content: "你所在支部需于本周内补齐季度思想汇报，请及时上传。",
  },
  {
    id: 103,
    title: "毕业生就业信息登记更新说明",
    audience: "2026届毕业生",
    date: "2026-04-15",
    channelLabels: ["站内", "邮件"],
    read: true,
    statusLabel: "已读",
    tags: ["就业", "信息校验"],
    stats: { delivered: 312, read: 287 },
    content: "就业去向信息已开放二次更新，请在学院平台完成信息校验。",
  },
];

export const applications = [
  {
    no: "APP20260418001",
    typeLabel: "在读证明",
    statusLabel: "审核中",
    approver: "辅导员 李老师",
    applicant: "赵晨曦",
    purpose: "实习单位提交材料",
    createdAt: "2026-04-18 14:30",
    attachmentCount: 2,
    generatedFileName: "",
  },
  {
    no: "APP20260410002",
    typeLabel: "成绩证明",
    statusLabel: "已通过",
    approver: "教学秘书 王老师",
    applicant: "赵晨曦",
    purpose: "交换项目报名",
    createdAt: "2026-04-10 09:12",
    attachmentCount: 1,
    generatedFileName: "成绩证明_APP20260410002.pdf",
  },
  {
    no: "APP20260405003",
    typeLabel: "党团材料盖章",
    statusLabel: "已驳回",
    approver: "学工办老师 张老师",
    applicant: "陈一诺",
    purpose: "支部季度材料归档",
    createdAt: "2026-04-05 16:08",
    attachmentCount: 3,
    generatedFileName: "",
  },
];

export const honors = [
  { title: "国家奖学金获得者", owner: "赵晨曦", year: "2025", categoryLabel: "国家奖学金", story: "综合成绩排名专业前 3%，参与创新训练项目和志愿服务。" },
  { title: "先进班集体", owner: "软件工程2班", year: "2025", categoryLabel: "先进集体", story: "班级学风建设成效明显，竞赛参与率和志愿服务时长居年级前列。" },
  { title: "优秀共青团干部", owner: "陈一诺", year: "2026", categoryLabel: "党团荣誉", story: "长期协助支部活动组织和材料收集，推动团员青年理论学习。" },
];

export const adminDashboard = {
  studentCount: 1200,
  pendingApprovalCount: 3,
  todayPushCount: 3,
  riskCount: 12,
  board: [
    ["知识库条目", "96"],
    ["政策附件", "34 个文件"],
    ["党团流程进行中", "71 人"],
    ["通知平均已读率", "82%"],
  ],
};

export const adminStudents = [
  { studentNo: "20220001", name: "赵晨曦", grade: "2022", major: "软件工程", className: "软件工程2班", phone: "13800181234", email: "zhaochenxi@example.edu.cn", politicalStatusLabel: "预备党员", statusText: "在读", tags: ["奖学金关注", "党员发展对象"] },
  { studentNo: "20220018", name: "陈一诺", grade: "2022", major: "软件工程", className: "软件工程2班", phone: "13900188818", email: "chenyinuo@example.edu.cn", politicalStatusLabel: "发展对象", statusText: "在读", tags: ["党员发展对象"] },
  { studentNo: "20260031", name: "林嘉禾", grade: "2026", major: "数据科学", className: "数据科学1班", phone: "13700180031", email: "linjiahe@example.edu.cn", politicalStatusLabel: "共青团员", statusText: "毕业年级", tags: ["就业意向"] },
];

export const auditLogs = [
  { actor: "辅导员 李老师", module: "学生画像", action: "查看学生敏感字段：联系方式", time: "2026-04-19 09:12", result: "成功" },
  { actor: "辅导员 李老师", module: "通知", action: "发布定向通知：奖学金材料提交", time: "2026-04-18 17:43", result: "成功" },
];

export const systemLogs = [
  { levelLabel: "错误", module: "application", requestId: "202605120930001001", path: "/api/v1/applications/approve", message: "审批状态冲突", detail: "同一申请被重复审批，后端返回 40900。" },
  { levelLabel: "警告", module: "frontend", requestId: "202605120914001112", path: "/student/profile", message: "学生画像页面渲染异常", detail: "前端捕获 TypeError，并通过 /system-logs/client-errors 上报。" },
];
