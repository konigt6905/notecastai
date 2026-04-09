package com.notecastai.gamenote.api.mapper;

import com.notecastai.gamenote.api.dto.GameNoteCreateRequest;
import com.notecastai.gamenote.api.dto.SubmitStatisticsRequest;
import com.notecastai.gamenote.service.command.CreateGameNoteCommand;
import com.notecastai.gamenote.service.command.SubmitStatisticsCommand;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GameNoteCommandMapper {

    CreateGameNoteCommand toCommand(GameNoteCreateRequest request);

    SubmitStatisticsCommand toCommand(SubmitStatisticsRequest request);

    SubmitStatisticsCommand.QuestionAnswer toCommand(SubmitStatisticsRequest.QuestionAnswerDTO dto);
}
