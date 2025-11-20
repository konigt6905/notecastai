package com.notecastai.gamenote.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.notecastai.gamenote.domain.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameQuestionDTO {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("type")
    private QuestionType type;

    @JsonProperty("questionText")
    private String questionText;

    @JsonProperty("options")
    private List<String> options;

    @JsonProperty("correctAnswer")
    private String correctAnswer;

    @JsonProperty("answer")
    private String answer;

    @JsonProperty("explanation")
    private String explanation;

    @JsonProperty("hint")
    private String hint;

}
