package com.notecastai.tag.service;

import com.notecastai.tag.api.dto.TagCreateRequest;
import com.notecastai.tag.api.dto.TagDTO;
import com.notecastai.tag.api.dto.TagUpdateRequest;

import java.util.List;

public interface TagService {

    TagDTO create(TagCreateRequest request);

    TagDTO update(Long id, TagUpdateRequest request);

    void deleteForUser(Long id, Long userId);

    TagDTO getForUser(Long id, Long userId);

    List<TagDTO> findAllByUser(Long userId);

    List<TagDTO> createDefaultTagsForUser(Long userId);
}