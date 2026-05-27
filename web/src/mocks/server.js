import {
  adminDashboard,
  adminStudents,
  applications,
  auditLogs,
  honors,
  kbTemplates,
  notices,
  partyStages,
  profile,
  qaArticles,
  studentDashboard,
  systemLogs,
} from "./data";

const mockUsers = {
  student: {
    token: "mock-student-token",
    password: "123456",
    user: {
      id: 1,
      username: "20220001",
      realName: "赵晨曦",
      userType: "student",
      roles: ["student"],
      permissions: [
        "student:dashboard:view",
        "student:kb:view",
        "student:party:view",
        "student:notice:view",
        "student:application:view",
        "student:profile:view",
        "student:honor:view",
      ],
      dataScopes: [{ scopeType: "self", scopeValue: "1" }],
    },
  },
  class_cadre: {
    token: "mock-cadre-token",
    password: "123456",
    user: {
      id: 2,
      username: "20220018",
      realName: "陈一诺",
      userType: "student",
      roles: ["class_cadre"],
      permissions: [
        "student:dashboard:view",
        "student:kb:view",
        "student:party:view",
        "student:notice:view",
        "student:application:view",
        "student:profile:view",
        "student:honor:view",
        "cadre:party:todo:view",
      ],
      dataScopes: [{ scopeType: "class", scopeValue: "软件工程2班" }],
    },
  },
  teacher_admin: {
    token: "mock-teacher-token",
    password: "123456",
    user: {
      id: 8,
      username: "teacher001",
      realName: "李老师",
      userType: "teacher",
      roles: ["teacher_admin"],
      permissions: [
        "admin:dashboard:view",
        "admin:student:view",
        "admin:kb:view",
        "admin:party:view",
        "admin:notice:view",
        "admin:application:view",
        "admin:honor:view",
        "admin:audit:view",
        "admin:system-log:view",
      ],
      dataScopes: [{ scopeType: "department", scopeValue: "信息科学与工程学院" }],
    },
  },
  college_leader: {
    token: "mock-leader-token",
    password: "123456",
    user: {
      id: 10,
      username: "leader001",
      realName: "学院领导",
      userType: "leader",
      roles: ["college_leader"],
      permissions: ["admin:dashboard:view", "admin:audit:view", "admin:system-log:view"],
      dataScopes: [{ scopeType: "department", scopeValue: "信息科学与工程学院" }],
    },
  },
};

let nextStudentId = 1000;
let currentRoleCode = "student";
let currentUsername = "20220001";

function delay(data) {
  return new Promise((resolve) => {
    window.setTimeout(() => resolve(data), 120);
  });
}

export async function mockLogin(payload) {
  const userEntry = findUserByUsername(payload.username, payload.roleCode);
  if (!userEntry || userEntry.account.password !== payload.password) {
    throw new Error("账号或密码不正确");
  }
  currentRoleCode = userEntry.roleCode;
  currentUsername = userEntry.account.user.username;
  return delay(toLoginData(userEntry.account));
}

export async function mockMe() {
  const userEntry = findUserByUsername(currentUsername, currentRoleCode);
  return delay(toLoginData(userEntry?.account ?? mockUsers[currentRoleCode]));
}

export async function registerStudent(payload) {
  const studentNo = normalize(payload.studentNo);
  const name = normalize(payload.name);
  const password = String(payload.password ?? "");

  if (!studentNo || !name || !password || !normalize(payload.grade) || !normalize(payload.major) || !normalize(payload.className)) {
    throw new Error("请填写完整注册信息");
  }
  if (findUserByUsername(studentNo)) {
    throw new Error("该学号已经注册");
  }

  const created = createStudentAccount({
    studentNo,
    name,
    password,
    grade: normalize(payload.grade),
    major: normalize(payload.major),
    className: normalize(payload.className),
    phone: normalize(payload.phone),
    email: normalize(payload.email),
    politicalStatusLabel: normalize(payload.politicalStatusLabel) || "群众",
  });
  return delay({ studentNo: created.user.username, name: created.user.realName });
}

export async function changePassword(payload) {
  const userEntry = findUserByUsername(currentUsername, currentRoleCode);
  if (!userEntry) {
    throw new Error("当前登录状态已失效");
  }
  if (userEntry.account.password !== payload.oldPassword) {
    throw new Error("原密码不正确");
  }
  userEntry.account.password = payload.newPassword;
  return delay({ changed: true });
}

export async function batchRegisterStudents(rows) {
  const result = {
    successCount: 0,
    skippedCount: 0,
    failedCount: 0,
    messages: [],
  };

  rows.forEach((row, index) => {
    const lineNo = index + 2;
    const studentNo = normalize(row.studentNo);
    const name = normalize(row.name);
    if (!studentNo || !name) {
      result.failedCount += 1;
      result.messages.push(`第 ${lineNo} 行缺少学号或姓名`);
      return;
    }
    if (findUserByUsername(studentNo)) {
      result.skippedCount += 1;
      result.messages.push(`第 ${lineNo} 行学号 ${studentNo} 已存在`);
      return;
    }
    createStudentAccount({
      studentNo,
      name,
      password: "info666",
      grade: normalize(row.grade),
      major: normalize(row.major),
      className: normalize(row.className),
      phone: normalize(row.phone),
      email: normalize(row.email),
      politicalStatusLabel: normalize(row.politicalStatusLabel) || "群众",
    });
    result.successCount += 1;
  });

  return delay(result);
}

export async function fetchStudentDashboard() {
  return delay(studentDashboard);
}

export async function fetchProfile() {
  const student = adminStudents.find((item) => item.studentNo === currentUsername);
  if (!student) {
    return delay(profile);
  }

  return delay({
    ...profile,
    studentNo: student.studentNo,
    name: student.name,
    grade: student.grade || "",
    major: student.major || "",
    className: student.className || "",
    politicalStatusLabel: student.politicalStatusLabel || "群众",
    phone: student.phone || "",
    phoneMasked: maskPhone(student.phone),
    email: student.email || "",
    tags: student.tags,
    growthRecords: currentUsername === "20220001" ? profile.growthRecords : [],
  });
}

export async function updateProfile(payload) {
  const student = adminStudents.find((item) => item.studentNo === currentUsername);
  if (!student) {
    throw new Error("未找到当前学生档案");
  }
  student.phone = normalize(payload.phone);
  student.email = normalize(payload.email);
  student.politicalStatusLabel = normalize(payload.politicalStatusLabel) || "群众";
  return fetchProfile();
}

export async function fetchKnowledgeList() {
  return delay(qaArticles);
}

export async function fetchKnowledgeTemplates() {
  return delay(kbTemplates);
}

export async function fetchPartyStages() {
  return delay(partyStages);
}

export async function fetchNotices() {
  return delay(notices.map((item) => ({ ...item })));
}

export async function fetchApplications() {
  return delay(applications.map((item) => ({ ...item })));
}

export async function fetchHonors() {
  return delay(honors);
}

export async function fetchAdminDashboard() {
  return delay(adminDashboard);
}

export async function fetchAdminStudents() {
  return delay(adminStudents.map((item) => ({ ...item, tags: [...item.tags] })));
}

export async function fetchAuditLogs() {
  return delay(auditLogs);
}

export async function fetchSystemLogs() {
  return delay(systemLogs);
}

function createStudentAccount({ studentNo, name, password, grade, major, className, phone, email, politicalStatusLabel }) {
  nextStudentId += 1;
  const account = {
    token: `mock-student-token-${studentNo}`,
    password,
    user: {
      id: nextStudentId,
      username: studentNo,
      realName: name,
      userType: "student",
      roles: ["student"],
      permissions: [
        "student:dashboard:view",
        "student:kb:view",
        "student:party:view",
        "student:notice:view",
        "student:application:view",
        "student:profile:view",
        "student:honor:view",
      ],
      dataScopes: [{ scopeType: "self", scopeValue: String(nextStudentId) }],
    },
  };
  mockUsers[`student:${studentNo}`] = account;
  upsertAdminStudent({ studentNo, name, grade, major, className, phone, email, politicalStatusLabel });
  return account;
}

function upsertAdminStudent({ studentNo, name, grade, major, className, phone, email, politicalStatusLabel }) {
  const existing = adminStudents.find((item) => item.studentNo === studentNo);
  const payload = {
    studentNo,
    name,
    grade,
    major,
    className,
    phone,
    email,
    politicalStatusLabel: politicalStatusLabel || "群众",
    statusText: "在读",
    tags: grade ? [`${grade}级`] : [],
  };

  if (existing) {
    Object.assign(existing, payload);
    return;
  }
  adminStudents.unshift(payload);
}

function findUserByUsername(username, roleCode) {
  const normalized = normalize(username);
  if (roleCode && roleCode !== "student") {
    const roleAccount = mockUsers[roleCode];
    if (roleAccount?.user.username === normalized) {
      return { roleCode, account: roleAccount };
    }
    return null;
  }

  const studentAccount = mockUsers[`student:${normalized}`];
  if (studentAccount?.user.username === normalized) {
    return { roleCode: `student:${normalized}`, account: studentAccount };
  }

  if (roleCode === "student" && mockUsers.student.user.username === normalized) {
    return { roleCode: "student", account: mockUsers.student };
  }

  const entries = Object.entries(mockUsers);
  const found = entries.find(([, account]) => account.user.username === normalized);
  if (!found) return null;
  return { roleCode: found[0], account: found[1] };
}

function toLoginData(account) {
  return {
    token: account.token,
    user: { ...account.user },
  };
}

function normalize(value) {
  return String(value ?? "").trim();
}

function maskPhone(phone) {
  const normalized = normalize(phone);
  if (!normalized) return "未填写";
  if (normalized.length < 7) return normalized;
  return `${normalized.slice(0, 3)}****${normalized.slice(-4)}`;
}
