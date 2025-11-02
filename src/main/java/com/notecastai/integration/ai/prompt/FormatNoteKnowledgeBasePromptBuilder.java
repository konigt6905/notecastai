package com.notecastai.integration.ai.prompt;

import com.notecastai.note.domain.FormateType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FormatNoteKnowledgeBasePromptBuilder {

    private String currentTitle;
    private String currentKnowledgeBase;
    private FormateType formateType;
    private List<String> availableTags;
    private String userInstructions;

    private static final String PLAIN_TEXT_RULES = """
            PLAIN TEXT FORMATTING RULES:
            1. DO NOT use any Markdown syntax (no #, **, *, `, etc.)
            2. DO NOT use code blocks or inline code formatting
            3. Use plain text only with simple structure:
               - Use blank lines to separate sections
               - Use simple indentation (spaces) for hierarchy
               - Use dash (-) for lists if needed
               - Use UPPERCASE for emphasis sparingly
               - Use line breaks for readability
            4. Keep the text clean and readable in plain format
            5. Think of it as a plain .txt file content
            """;

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            You are an expert content editor specializing in knowledge base organization.
            
            YOUR TASK:
            Reformat and improve the existing knowledge base content according to the specified format type.
            
            YOUR GOALS:
            1. Improve the title if needed based on the updated content
            2. Reformat the knowledge base content as PLAIN TEXT (no markdown)
            3. Apply the format type instructions
            4. Apply any user-provided instructions
            5. Select relevant tags from available tags
            6. Propose 6 intelligent actions for this note
            
            FORMAT TYPE INSTRUCTIONS:
            %s
            
            %s
            """;

    private static final String USER_PROMPT_TEMPLATE = """
            Reformat the following knowledge base content according to the specified requirements.
            
            **Current Note Details:**
            - Current Title: "%s"
            - Format Type: %s
            - Available User Tags: %s
            %s
            
            **Current Knowledge Base Content:**
```
            %s
```
            
            **Output Instructions:**
            
            1. **adjustedTitle**:
               - Keep the current title: "%s"
               - Only change if content was significantly restructured
               - Should be 3-8 words
            
            2. **knowledgeBase**:
               - Output PLAIN TEXT ONLY - no markdown, no special syntax
               - Apply format type: %s
               - Format instructions: %s
               %s
               - Use simple structure with blank lines and indentation
               - Keep it clean and readable as plain text
               - Preserve all important information
               - Keep the same language as input
            
            3. **proposedTags**:
               - Select 2-5 most relevant tags from: %s
               - Only use tags that exist in the available tags list
               - If no good matches, return empty array []
            
            4. **proposedAiActions**:
               - Propose exactly 6 useful actions
               - Action names: 2-4 words
               - Prompts: Clear, specific instructions
            
            **Plain Text Example:**
```
            Project Kickoff Meeting
            Date: 2024-01-15
            
            Attendees:
            - John Smith (PM)
            - Sarah Johnson (Dev Lead)
            
            Key Decisions:
            - Launch date set for March 1st
            - Weekly sprint meetings every Monday
            
            Action Items:
            - John: Create timeline (Due: Jan 20)
            - Sarah: Setup environment (Due: Jan 18)
```
            """;

    public String getSystemPrompt() {
        FormateType format = formateType != null ? formateType : FormateType.DEFAULT;

        String formatInstructions = "FORMAT TYPE: " + format.getLabel() + "\n" + format.getPromptText();

        return String.format(
                SYSTEM_PROMPT_TEMPLATE,
                formatInstructions,
                PLAIN_TEXT_RULES
        );
    }

    public String getUserPrompt() {
        FormateType format = formateType != null ? formateType : FormateType.DEFAULT;

        String tagsDisplay = (availableTags != null && !availableTags.isEmpty())
                ? availableTags.stream().collect(Collectors.joining("\", \"", "[\"", "\"]"))
                : "(no tags available)";

        String instructionsSection = (userInstructions != null && !userInstructions.isBlank())
                ? "- Additional Instructions: " + userInstructions
                : "";

        String userInstructionsInBody = (userInstructions != null && !userInstructions.isBlank())
                ? "   - User Instructions: " + userInstructions
                : "";

        return String.format(
                USER_PROMPT_TEMPLATE,
                currentTitle,
                format.getLabel(),
                tagsDisplay,
                instructionsSection,
                currentKnowledgeBase,
                currentTitle,
                format.getLabel(),
                format.getPromptText(),
                userInstructionsInBody,
                tagsDisplay
        );
    }
}