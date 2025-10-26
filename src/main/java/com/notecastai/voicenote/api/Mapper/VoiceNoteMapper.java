package com.notecastai.voicenote.api.Mapper;

import com.notecastai.common.EntityMapper;
import com.notecastai.voicenote.api.dto.VoiceNoteDTO;
import com.notecastai.voicenote.domain.VoiceNoteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VoiceNoteMapper extends EntityMapper<VoiceNoteEntity, VoiceNoteDTO> {

    @Override
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "note.id", target = "noteId")
    VoiceNoteDTO toDto(VoiceNoteEntity entity);
}
