package com.ise.platform.modules.kb;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KbServiceTest {

    private final KbService kbService = new KbService();

    @Test
    void qaShouldFallbackWhenNoReliableSource() {
        KbDto.QaRequest request = new KbDto.QaRequest();
        request.setQuestion("一个完全不相关的问题");
        KbDto.QaResponse response = kbService.qa(request);
        assertThat(response.getAnswer()).isEqualTo("未检索到可靠依据");
        assertThat(response.getSources()).isEmpty();
        assertThat(response.getConfidence()).isEqualTo(0.0d);
    }
}
