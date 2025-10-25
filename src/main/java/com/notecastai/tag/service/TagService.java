package com.notecastai.tag.service;

import com.notecastai.tag.api.dto.TagCreateRequest;
import com.notecastai.tag.api.dto.TagDTO;

import java.util.List;

public interface TagService {

    TagDTO create(TagCreateRequest request);

    void deactivateForUser(Long id, Long userId);

    TagDTO getForUser(Long id, Long userId);

    List<TagDTO> findAllByUser(Long userId);

    List<TagDTO> createDefaultTagsForUser(Long userId);
}