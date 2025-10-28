package com.notecastai.integration.ai.prompt;

import com.notecastai.note.domain.NoteEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.stream.Collectors;

@Getter
@Builder
public class NoteChatPromptBuilder {

    private NoteEntity note;
    private String userQuestion;

    private static final String MARKDOWN_RESPONSE_RULES = """
            MARKDOWN RESPONSE FORMATTING:
            1. Use proper markdown formatting for all responses
            2. Use **bold** for key terms and important points
            3. Use *italic* for emphasis
            4. Use headings (##, ###) to structure longer responses
            5. Use bullet points (-) or numbered lists (1.) for multiple items
            6. Use `code` for technical terms, commands, or code snippets
            7. Use code blocks with language identifier for longer code examples:
```language
               code here
```
            8. Use > for blockquotes when citing from the note
            9. Use tables when comparing multiple items
            10. Keep responses well-structured and easy to read
            11. Add line breaks between sections for readability
            """;

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            You are an expert AI assistant specialized in helping users understand and work with their notes.
            
            YOUR ROLE:
            - Answer questions about the user's note accurately and helpfully
            - Provide clear, well-structured explanations using markdown formatting
            - Reference specific parts of the note when relevant
            - Maintain context from previous messages in the conversation
            - Be concise but thorough - aim for clarity over brevity
            
            YOUR KNOWLEDGE BASE:
            You have access to the user's note with the following information:
            
            **Note Title:** %s
            
            **Note Tags:** %s
            
            **Current Format:** %s
            
            **Knowledge Base (Raw Content):**
```
            %s
```
            
            **Formatted Note (User's Display Version):**
```markdown
            %s
```
            
            %s
            
            RESPONSE GUIDELINES:
            1. Base your answers on the note content above
            2. If the note doesn't contain relevant information, clearly state that
            3. Reference specific sections or points from the note when answering
            4. Use markdown formatting to make responses clear and readable
            5. If asked for clarification, provide examples from the note
            6. If asked about topics not in the note, acknowledge the limitation but offer related insights if helpful
            7. Maintain conversation context - reference previous exchanges when relevant
            8. Be helpful, accurate, and educational
            
            FORMAT:
            Always respond in well-formatted markdown. Make your responses easy to scan and understand.
            """;

    public String getSystemPrompt() {
        String title = note.getTitle() != null ? note.getTitle() : "(No title)";

        String tags = note.getTags() != null && !note.getTags().isEmpty()
                ? note.getTags().stream()
                .map(tag -> tag.getName())
                .collect(Collectors.joining(", "))
                : "(No tags)";

        String format = note.getCurrentFormate() != null
                ? note.getCurrentFormate().getLabel()
                : "Default";

        String knowledgeBase = note.getKnowledgeBase() != null
                ? note.getKnowledgeBase()
                : "(No knowledge base content)";

        String formattedNote = note.getFormattedNote() != null
                ? note.getFormattedNote()
                : "(No formatted version available)";

        return String.format(
                SYSTEM_PROMPT_TEMPLATE,
                title,
                tags,
                format,
                knowledgeBase,
                formattedNote,
                MARKDOWN_RESPONSE_RULES
        );
    }

    public String getUserPrompt() {
        return userQuestion;
    }

}