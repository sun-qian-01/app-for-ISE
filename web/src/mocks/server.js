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

let currentRoleCode = "student";

function delay(data) {
  return new Promise((resolve) => {
    window.setTimeout(() => resolve(data), 120);
  });
}

export async function mockLogin(payload) {
  currentRoleCode = payload.roleCode || "student";
  return delay(mockUsers[currentRoleCode]);
}

export async function mockMe() {
  return delay(mockUsers[currentRoleCode]);
}

export async function fetchStudentDashboard() {
  return delay(studentDashboard);
}

export async function fetchProfile() {
  return delay(profile);
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
  return delay(adminStudents);
}

export async function fetchAuditLogs() {
  return delay(auditLogs);
}

export async function fetchSystemLogs() {
  return delay(systemLogs);
}
