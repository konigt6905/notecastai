package com.notecastai.integration.ai.prompt;

import com.notecastai.note.domain.FormateType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class NewNotePromptBuilder {

    private String title;
    private String knowledgeBase;
    private FormateType formateType;
    private List<String> availableTags;
    private String userInstructions;

    private static final String MARKDOWN_RULES = """
            MARKDOWN FORMATTING RULES:
            1. Use proper headings: # H1, ## H2, ### H3
            2. Use **bold** for emphasis and *italic* for subtle emphasis
            3. Use code blocks with language identifiers:
```language
               code here
```
            4. Use `inline code` for small code snippets or technical terms
            5. Use lists: - for unordered, 1. for ordered
            6. Use > for blockquotes
            7. Use tables when appropriate
            8. Keep formatting clean and readable
            """;

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            You are an expert note formatter and organizer with 10+ years of experience in knowledge management.
            
            YOUR TASK:
            Transform raw knowledge base content into a well-formatted, organized note using Markdown.
            
            YOUR GOALS:
            1. Improve the title if needed (or create one if missing)
            2. Format the content according to the specified format type using Markdown
            3. Select relevant tags from the user's available tags
            4. Propose 6 intelligent actions the user could perform with this note
            
            FORMAT INSTRUCTIONS:
            %s
            
            %s
            """;

    private static final String USER_PROMPT_TEMPLATE = """
            Transform the following raw knowledge base content into a well-organized note.
            
            **Input Details:**
            - Original Title: %s
            - Format Type: %s
            - Available User Tags: %s
            %s
            
            **Raw Knowledge Base Content:**
```
            %s
```
            
            **Output Instructions:**
            
            1. **adjustedTitle**: 
               - If original title exists and is good, keep it or slightly improve it
               - If no title or poor title, create a descriptive one (3-6 words)
               - Should accurately reflect the note's content
            
            2. **formattedNote**:
               - Apply the format type: %s
               - Use proper Markdown formatting (headings, lists, bold, code blocks, etc.)
               - Ensure content is well-organized and easy to read
               - Preserve important information from the knowledge base
               - Keep the same language as the input content
            
            3. **proposedTags**:
               - Select 2-5 most relevant tags from available tags: %s
               - Tags must exist in the available tags list
               - Only include tags that truly match the content
               - If no good matches, provide empty array []
            
            4. **proposedAiActions**:
               - Propose exactly 6 intelligent actions
               - Each action should be something useful the user could do with this note
               - Action names should be short and clear (2-4 words)
               - Prompts should be specific instructions
               
            **Examples of Good AI Actions:**
            - Extract action items: "Analyze this note and extract all actionable tasks"
            - Create summary: "Create a 3-sentence executive summary"
            - Find key concepts: "Identify the 5 most important concepts"
            - Generate questions: "Create 5 thought-provoking questions"
            - Expand on topic: "Expand the main topic with additional context"
            - Create flashcards: "Generate 10 question-answer flashcards"
            """;

    public String getSystemPrompt() {
        String formatInstructions = formateType != null
                ? "FORMAT TYPE: " + formateType.getLabel() + "\n" + formateType.getPromptText()
                : "FORMAT TYPE: Default\nFormat the content in a clear, well-structured way using appropriate Markdown.";

        return String.format(
                SYSTEM_PROMPT_TEMPLATE,
                formatInstructions,
                MARKDOWN_RULES
        );
    }

    public String getUserPrompt() {
        String titleDisplay = (title != null && !title.isBlank())
                ? "\"" + title + "\""
                : "(no title provided - please create one)";

        String formatTypeDisplay = formateType != null
                ? formateType.getLabel() + " - " + formateType.getPromptText()
                : "Default - Format in an appropriate way";

        String tagsDisplay = (availableTags != null && !availableTags.isEmpty())
                ? availableTags.stream().collect(Collectors.joining("\", \"", "[\"", "\"]"))
                : "(no tags available)";

        String instructionsSection = (userInstructions != null && !userInstructions.isBlank())
                ? "- **SUPER IMPORTANT** Additional User Instructions: " + userInstructions
                : "";

        String formatInstructionsRepeat = formateType != null
                ? formateType.getPromptText()
                : "Format the content in a clear, well-structured way.";

        return String.format(
                USER_PROMPT_TEMPLATE,
                titleDisplay,
                formatTypeDisplay,
                tagsDisplay,
                instructionsSection,
                knowledgeBase,
                formatInstructionsRepeat,
                tagsDisplay
        );
    }
}
