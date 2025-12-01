package com.example.tracnghiem.dto.exam;

import java.util.List;

public record ExamAttemptDetailResponse(
        ExamAttemptResponse attempt,
        List<QuestionAnswerView> answers
) {
    public record QuestionAnswerView(
            Long questionId,
            String content,
            String questionType,
            Integer marks,
            Long selectedOptionId,
            String selectedOptionContent,
            String fillAnswer,
            boolean correct,
            List<String> correctAnswers
    ) {
    }
}

