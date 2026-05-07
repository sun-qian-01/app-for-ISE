const profile = {
  name: '赵晨曦',
  studentNo: '20220001',
  major: '软件工程',
  currentStage: '发展对象',
  unreadNoticeCount: 2,
  todoCount: 1
};

const quickEntries = [
  { key: 'kb', title: '政策知识库', tip: '查询高频问题' },
  { key: 'progress', title: '党团进度', tip: '查看当前阶段' },
  { key: 'notice', title: '通知中心', tip: '查看精准推送' },
  { key: 'certificate', title: '证明申请', tip: '提交或撤回申请' }
];

const kbCategories = [
  { id: 0, name: '全部' },
  { id: 1, name: '学籍事务' },
  { id: 2, name: '奖助政策' },
  { id: 3, name: '党团事务' }
];

const kbArticles = [
  {
    id: 101,
    categoryId: 1,
    title: '休学与复学办理指南',
    summary: '说明休学条件、所需材料和办理流程。',
    keywords: ['休学', '复学', '学籍']
  },
  {
    id: 102,
    categoryId: 2,
    title: '春季学期奖学金申请说明',
    summary: '包含时间节点、材料清单和评审流程。',
    keywords: ['奖学金', '申请', '材料']
  },
  {
    id: 103,
    categoryId: 3,
    title: '入党流程阶段与材料模板',
    summary: '介绍党团流程节点及阶段材料要求。',
    keywords: ['入党', '流程', '模板']
  }
];

const progress = {
  currentStage: '发展对象',
  currentStatus: 'processing',
  records: [
    { stageName: '申请人', status: 'completed', time: '2026-03-01', comment: '已提交申请' },
    { stageName: '积极分子', status: 'completed', time: '2026-03-20', comment: '培训合格' },
    { stageName: '发展对象', status: 'processing', time: '2026-04-18', comment: '材料审核中' },
    { stageName: '预备党员', status: 'pending', time: '-', comment: '待进入本阶段' }
  ]
};

const notices = [
  {
    id: 201,
    title: '2026 春季奖学金材料提交通知',
    content: '请于 4 月 24 日前完成系统材料提交。',
    publishAt: '2026-04-19 12:00:00',
    read: false
  },
  {
    id: 202,
    title: '党团阶段材料补交通知',
    content: '发展对象阶段需补交思想汇报。',
    publishAt: '2026-04-20 10:20:00',
    read: false
  },
  {
    id: 203,
    title: '五一假期安全提醒',
    content: '请合理安排返乡时间，注意出行安全。',
    publishAt: '2026-04-26 08:30:00',
    read: true
  }
];

const certificateTemplates = [
  { id: 1, name: '在读证明', desc: '用于实习和资格审核' },
  { id: 2, name: '成绩证明', desc: '用于升学申请或交换项目' }
];

const certificateApplications = [
  {
    id: 301,
    templateId: 1,
    templateName: '在读证明',
    purpose: '实习单位提交',
    status: 'reviewing',
    createdAt: '2026-04-21 14:33:00'
  },
  {
    id: 302,
    templateId: 2,
    templateName: '成绩证明',
    purpose: '交换项目申请',
    status: 'approved',
    createdAt: '2026-04-08 09:12:00'
  }
];

module.exports = {
  profile,
  quickEntries,
  kbCategories,
  kbArticles,
  progress,
  notices,
  certificateTemplates,
  certificateApplications
};
