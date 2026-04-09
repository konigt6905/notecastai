package com.notecastai.note.api.mapper;

import com.notecastai.note.api.dto.*;
import com.notecastai.note.service.command.*;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NoteCommandMapper {

    CreateNoteCommand toCommand(CreateNoteRequest request);

    CombineNoteCommand toCommand(NoteCombineRequest request);

    UpdateNoteManualCommand toCommand(NoteAdjustManualRequest request);

    FormatNoteKnowledgeCommand toCommand(NoteKnowledgeFormatRequest request);

    FormatNoteCommand toCommand(FormatNoteRequest request);

    AskNoteQuestionCommand toCommand(NoteQuestionRequest request);

    AskNoteQuestionCommand.ChatMessageCommand toCommand(ChatMessage message);
}
