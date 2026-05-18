package com.ise.platform.modules.application;

import com.ise.platform.common.api.PagedData;
import com.ise.platform.common.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ApplicationService {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final AtomicLong idGenerator = new AtomicLong(3000);
    private final List<ApplicationEntity> applications = new CopyOnWriteArrayList<>();

    public ApplicationService() {
        applications.add(new ApplicationEntity(
            1L,
            "APP20260418001",
            1L,
            1L,
            "certificate",
            "在读证明申请",
            "实习单位提交材料",
            "reviewing",
            "辅导员 李老师",
            "2026-04-18 14:30:00"
        ));
    }

    public PagedData<ApplicationDto.ApplicationView> myApplications(CurrentUser user,
                                                                    int pageNo,
                                                                    int pageSize,
                                                                    String applicationType,
                                                                    String status) {
        List<ApplicationDto.ApplicationView> filtered = applications.stream()
            .filter(item -> item.applicantUserId().equals(user.getId()))
            .filter(item -> !StringUtils.hasText(applicationType) || "all".equalsIgnoreCase(applicationType) || item.applicationType().equalsIgnoreCase(applicationType))
            .filter(item -> !StringUtils.hasText(status) || "all".equalsIgnoreCase(status) || item.status().equalsIgnoreCase(status))
            .sorted(Comparator.comparing(ApplicationEntity::submittedAt).reversed())
            .map(item -> new ApplicationDto.ApplicationView(
                item.id(),
                item.applicationNo(),
                item.applicationType(),
                item.title(),
                item.purpose(),
                item.status(),
                item.currentApprover(),
                item.submittedAt()
            ))
            .toList();
        return paginate(filtered, pageNo, pageSize);
    }

    public ApplicationDto.CreateResponse create(CurrentUser user, ApplicationDto.CreateRequest request) {
        String applicationNo = "APP" + NO_FORMATTER.format(LocalDateTime.now());
        ApplicationEntity entity = new ApplicationEntity(
            idGenerator.incrementAndGet(),
            applicationNo,
            request.getTemplateId(),
            user.getId(),
            request.getApplicationType().toLowerCase(Locale.ROOT),
            request.getTitle(),
            request.getPurpose(),
            "submitted",
            "辅导员 李老师",
            DATETIME_FORMATTER.format(LocalDateTime.now())
        );
        applications.add(entity);
        return new ApplicationDto.CreateResponse(applicationNo, "submitted", "辅导员 李老师", null);
    }

    private PagedData<ApplicationDto.ApplicationView> paginate(List<ApplicationDto.ApplicationView> source, int pageNo, int pageSize) {
        int safePageNo = Math.max(pageNo, 1);
        int safePageSize = Math.max(1, Math.min(pageSize, 100));
        int from = (safePageNo - 1) * safePageSize;
        if (from >= source.size()) {
            return new PagedData<>(List.of(), safePageNo, safePageSize, source.size());
        }
        int to = Math.min(from + safePageSize, source.size());
        return new PagedData<>(new ArrayList<>(source.subList(from, to)), safePageNo, safePageSize, source.size());
    }

    private record ApplicationEntity(Long id,
                                     String applicationNo,
                                     Long templateId,
                                     Long applicantUserId,
                                     String applicationType,
                                     String title,
                                     String purpose,
                                     String status,
                                     String currentApprover,
                                     String submittedAt) {
    }
}
