package com.notecastai.voicenote.api.Mapper;

import com.notecastai.common.EntityMapper;
import com.notecastai.note.api.mapper.NoteMapper;
import com.notecastai.tag.api.mapper.TagMapper;
import com.notecastai.voicenote.api.dto.VoiceNoteDTO;
import com.notecastai.voicenote.domain.VoiceNoteEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {TagMapper.class, NoteMapper.class}
)
public interface VoiceNoteMapper extends EntityMapper<VoiceNoteEntity, VoiceNoteDTO> {

    @Override
    @Mapping(source = "user.id", target = "userId")
    VoiceNoteDTO toDto(VoiceNoteEntity entity);
}
