package com.notecastai.tag.service;

import com.notecastai.tag.api.dto.TagDTO;
import com.notecastai.tag.service.command.CreateTagCommand;
import com.notecastai.tag.service.command.UpdateTagCommand;

import java.util.List;

public interface TagService {

    TagDTO create(CreateTagCommand command);

    TagDTO update(Long id, UpdateTagCommand command);

    void deleteForCurrentUser(Long id);

    TagDTO getForCurrentUser(Long id);

    List<TagDTO> findAllByCurrentUser();
}
