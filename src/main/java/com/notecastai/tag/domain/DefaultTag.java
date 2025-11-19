package com.notecastai.tag.domain;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * Enumeration of default tags that are automatically created for every new user.
 * These tags provide a standard organizational structure to help users get started.
 */
@Getter
public enum DefaultTag {

    INBOX("Inbox"),
    WORK("Work"),
    PERSONAL("Personal"),
    IDEAS("Ideas"),
    LEARNING("Learning"),
    PROJECTS("Projects"),
    MEETINGS("Meetings"),
    RESEARCH("Research"),
    TODOS("Todos"),
    ARCHIVED("Archived");

    private final String tagName;

    DefaultTag(String tagName) {
        this.tagName = tagName;
    }

    public static List<String> getAllTagNames() {
        return Arrays.stream(values())
                .map(DefaultTag::getTagName)
                .toList();
    }
}
