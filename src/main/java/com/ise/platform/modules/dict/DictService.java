package com.ise.platform.modules.dict;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DictService {

    private final Map<String, List<Map<String, String>>> dict = new LinkedHashMap<>();

    public DictService() {
        dict.put("student_status", List.of(
            entry("active", "在读"),
            entry("graduated", "毕业"),
            entry("suspended", "休学")
        ));
        dict.put("application_status", List.of(
            entry("submitted", "已提交"),
            entry("reviewing", "审核中"),
            entry("approved", "已通过"),
            entry("rejected", "已驳回"),
            entry("revoked", "已撤回")
        ));
        dict.put("notice_status", List.of(
            entry("draft", "草稿"),
            entry("published", "已发布"),
            entry("archived", "已归档")
        ));
        dict.put("political_status", List.of(
            entry("league_member", "共青团员"),
            entry("development_candidate", "发展对象"),
            entry("probationary_party_member", "预备党员"),
            entry("party_member", "中共党员")
        ));
    }

    public Map<String, List<Map<String, String>>> getDicts(String types) {
        if (!StringUtils.hasText(types)) {
            return dict;
        }
        List<String> requested = Arrays.stream(types.split(","))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .collect(Collectors.toList());
        Map<String, List<Map<String, String>>> result = new LinkedHashMap<>();
        for (String type : requested) {
            if (dict.containsKey(type)) {
                result.put(type, dict.get(type));
            }
        }
        return result;
    }

    private Map<String, String> entry(String value, String label) {
        Map<String, String> item = new LinkedHashMap<>();
        item.put("value", value);
        item.put("label", label);
        return item;
    }
}
