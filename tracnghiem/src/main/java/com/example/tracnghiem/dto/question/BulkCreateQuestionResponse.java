package com.example.tracnghiem.dto.question;

import java.util.List;

public record BulkCreateQuestionResponse(
        List<QuestionResponse> created,
        List<DuplicateQuestionInfo> duplicates,
        int totalProcessed,
        int totalCreated,
        int totalDuplicates) {
    
    public record DuplicateQuestionInfo(
            String content,
            Long chapterId,
            Long passageId,
            String reason // "TRONG_FILE" hoặc "DA_TON_TAI"
    ) {
    }
}

