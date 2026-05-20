package com.ise.platform.modules;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ise.platform.common.security.AuthFilter;
import com.ise.platform.modules.auth.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
class ApiIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private AuthService authService;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
            .addFilter(new AuthFilter(authService, objectMapper), "/api/v1/*")
            .build();
    }

    @Test
    void loginShouldReturnToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "20220001",
                      "password": "123456"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.token").isNotEmpty())
            .andExpect(jsonPath("$.data.user.username").value("20220001"));
    }

    @Test
    void meWithoutTokenShouldReturn401BusinessCode() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    void authMeShouldContainMenus() throws Exception {
        String token = loginAndGetToken("20220001", "123456");
        mockMvc.perform(get("/api/v1/auth/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.menus").isArray());
    }

    @Test
    void kbQaShouldReturnFallbackWhenNoReliableSource() throws Exception {
        String token = loginAndGetToken("20220001", "123456");
        mockMvc.perform(post("/api/v1/kb/qa")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "question": "一个和业务无关的问题"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.answer").value("未检索到可靠依据"))
            .andExpect(jsonPath("$.data.sources").isArray());
    }

    @Test
    void partyFlowsShouldReturnStageDefinitions() throws Exception {
        String token = loginAndGetToken("20220001", "123456");
        mockMvc.perform(get("/api/v1/party/flows")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data[0].stages").isArray());
    }

    @Test
    void markNoticeReadShouldAffectUnreadFilter() throws Exception {
        String token = loginAndGetToken("20220001", "123456");

        mockMvc.perform(post("/api/v1/notices/1/read")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/v1/notices/my")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .param("readStatus", "unread"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.records[*].id").isArray());
    }

    @Test
    void createApplicationShouldReturnSubmittedStatus() throws Exception {
        String token = loginAndGetToken("20220001", "123456");

        mockMvc.perform(post("/api/v1/applications")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "applicationType": "certificate",
                      "templateId": 1,
                      "title": "在读证明申请",
                      "purpose": "实习材料"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.status").value("submitted"));
    }

    @Test
    void applicationDetailShouldBeAccessibleByOwner() throws Exception {
        String token = loginAndGetToken("20220001", "123456");
        mockMvc.perform(get("/api/v1/applications/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.applicationNo").value("APP20260418001"));
    }

    @Test
    void studentCanCreateGrowthRecord() throws Exception {
        String token = loginAndGetToken("20220001", "123456");
        mockMvc.perform(post("/api/v1/students/1/growth-records")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "recordType": "practice",
                      "title": "企业实习",
                      "startDate": "2026-06-01",
                      "endDate": "2026-07-01",
                      "description": "参与后端开发"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.recordType").value("practice"));
    }

    @Test
    void teacherCanUpdateStudentTags() throws Exception {
        String token = loginAndGetToken("teacher001", "123456");
        mockMvc.perform(put("/api/v1/students/1/tags")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "tagIds": [1, 4]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void applicationApproveFlowShouldWork() throws Exception {
        String studentToken = loginAndGetToken("20220001", "123456");
        MvcResult createResult = mockMvc.perform(post("/api/v1/applications")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "applicationType": "certificate",
                      "templateId": 1,
                      "title": "在读证明申请",
                      "purpose": "实习材料"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andReturn();

        String applicationNo = objectMapper.readTree(createResult.getResponse().getContentAsString())
            .path("data")
            .path("applicationNo")
            .asText();
        assertThat(applicationNo).isNotBlank();

        Long applicationId = findApplicationIdByNo(studentToken, applicationNo);
        String teacherToken = loginAndGetToken("teacher001", "123456");
        mockMvc.perform(post("/api/v1/applications/{applicationId}/approve", applicationId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + teacherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "comment": "材料齐全，同意通过"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.status").value("approved"));
    }

    @Test
    void studentProfileShouldReturnMeData() throws Exception {
        String token = loginAndGetToken("20220001", "123456");
        mockMvc.perform(get("/api/v1/students/me/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.student.studentNo").value("20220001"));
    }

    @Test
    void studentCannotAccessOtherGrowthRecords() throws Exception {
        String token = loginAndGetToken("20220001", "123456");
        mockMvc.perform(get("/api/v1/students/2/growth-records")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(40300));
    }

    @Test
    void teacherCanAccessStudentGrowthRecords() throws Exception {
        String token = loginAndGetToken("teacher001", "123456");
        mockMvc.perform(get("/api/v1/students/1/growth-records")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void teacherCanQueryStudentList() throws Exception {
        String token = loginAndGetToken("teacher001", "123456");
        mockMvc.perform(get("/api/v1/students")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .param("grade", "2022"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    void studentCannotQueryStudentList() throws Exception {
        String token = loginAndGetToken("20220001", "123456");
        mockMvc.perform(get("/api/v1/students")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(40300));
    }

    @Test
    void studentCannotRequestSensitiveDetail() throws Exception {
        String token = loginAndGetToken("20220001", "123456");
        mockMvc.perform(get("/api/v1/students/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .param("includeSensitive", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(40300));
    }

    @Test
    void teacherCanRequestSensitiveDetail() throws Exception {
        String token = loginAndGetToken("teacher001", "123456");
        mockMvc.perform(get("/api/v1/students/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .param("includeSensitive", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.sensitiveInfo.phone").value("13800181234"));
    }

    @Test
    void dictsShouldSupportTypesFilter() throws Exception {
        String token = loginAndGetToken("teacher001", "123456");
        mockMvc.perform(get("/api/v1/dicts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .param("types", "student_status,application_status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.student_status").isArray())
            .andExpect(jsonPath("$.data.application_status").isArray());
    }

    @Test
    void uploadAndDownloadFileShouldWork() throws Exception {
        String token = loginAndGetToken("20220001", "123456");
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "demo.txt",
            "text/plain",
            "hello-api".getBytes()
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/v1/files/upload")
                .file(file)
                .param("bizType", "party_material")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andReturn();

        JsonNode uploadRoot = objectMapper.readTree(uploadResult.getResponse().getContentAsString());
        long fileId = uploadRoot.path("data").path("fileId").asLong();

        mockMvc.perform(get("/api/v1/files/{fileId}/download", fileId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk());
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "username": "%s",
                      "password": "%s"
                    }
                    """.formatted(username, password)))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        String token = root.path("data").path("token").asText();
        assertThat(token).isNotBlank();
        return token;
    }

    private Long findApplicationIdByNo(String token, String applicationNo) throws Exception {
        MvcResult listResult = mockMvc.perform(get("/api/v1/applications/my")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .param("pageNo", "1")
                .param("pageSize", "50"))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode records = objectMapper.readTree(listResult.getResponse().getContentAsString())
            .path("data")
            .path("records");
        for (JsonNode record : records) {
            if (applicationNo.equals(record.path("applicationNo").asText())) {
                return record.path("id").asLong();
            }
        }
        throw new IllegalStateException("created application not found in my list");
    }
}
