package com.ise.platform.modules.kb.rag;

import com.ise.platform.modules.kb.KbDto;

import java.util.List;

public interface QaAnswerClient {
    String answer(String question,
                  List<KbDto.QaHistoryMessage> history,
                  List<RagChunk> evidence,
                  boolean allowGeneralReply);
}
