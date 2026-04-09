package com.notecastai.integration.ai.prompt;

import com.notecastai.notecast.domain.NoteCastStyle;
import com.notecastai.notecast.domain.TranscriptSize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TranscriptPromptBuilder {

    private String noteContent;
    private NoteCastStyle style;
    private TranscriptSize size;

    private static final String TTS_RULES = """
            CRITICAL TTS-FRIENDLY TEXT RULES:
            1. Write ONLY spoken text - no labels, no speaker names, no stage directions
            2. Use natural, conversational language suitable for speaking aloud
            3. Keep sentences clear and medium-length (10-20 words ideal)
            4. Avoid special characters: no asterisks, brackets, parentheses in speech
            5. Write out numbers and abbreviations: "three" not "3", "doctor" not "Dr."
            6. Use simple punctuation: periods and commas only (avoid semicolons, dashes)
            7. Include natural pauses by ending sentences, not with "..."
            8. Avoid acronyms unless commonly spoken (NASA is ok, WCAG is not)
            9. No markdown, no formatting codes, no URLs
            10. No meta-commentary like "[pause]" or "(speaks softly)"
            11. Make it sound like one person speaking naturally to an audience
            12. Flow smoothly from one idea to the next with natural transitions
            """;

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            You are an expert script writer specializing in creating natural-sounding text-to-speech scripts.
            
            YOUR TASK:
            Transform the provided note content into a polished, TTS-ready transcript.
            The transcript will be read by a single AI voice - make it sound natural and engaging.
            
            YOUR GOALS:
            1. Create a flowing, spoken narrative from the note content
            2. Apply the specified style to shape the delivery
            3. Create a %s transcript with precise length requirements
            4. Produce exactly %d to %d words in %d to %d sentences (strict requirement)
            5. Make every sentence TTS-friendly and natural to speak
            6. Maintain accuracy to the source material while making it conversational
            
            STYLE TO APPLY:
            %s
            %s
            
            LENGTH REQUIREMENTS (%s - approximately %s speaking time):
            %s
            - Word count: %d to %d words (MANDATORY)
            - Sentence count: %d to %d sentences (MANDATORY)
            - Count carefully before submitting
            - Aim for the middle of these ranges
            
            %s
            
            OUTPUT FORMAT:
            Return a JSON object with the transcript and metadata.
            The transcript must meet the exact length requirements specified above.
            """;

    private static final String USER_PROMPT_TEMPLATE = """
            Create a TTS-ready transcript from the following note content.
            
            **Style to Apply:** %s
            %s
            
            **Size Requirements:** %s (approximately %s speaking time)
            %s
            - Target: %d to %d words
            - Target: %d to %d sentences
            - These requirements are MANDATORY
            
            **Note Content:**
```
            %s
```
            
            **Critical Instructions:**
            
            1. **transcript field:**
               - Write ONLY the spoken text - no labels, headers, or meta-text
               - Make it sound like one person speaking naturally
               - Apply the style: %s
               - Apply the size guidance: %s
               - Word count MUST be between %d and %d words
               - Sentence count MUST be between %d and %d sentences
               - Use natural transitions between ideas
               - Keep sentences medium-length and clear (10-20 words each)
               - Write out all numbers, abbreviations, and symbols
               - No special characters except periods and commas
               - Flow smoothly from beginning to end
               - Start strong with a hook, end with a clear conclusion
            
            2. **estimatedDuration:**
               - Estimate speaking time in format "X min Y sec" (e.g., "5 min 30 sec")
               - Average speaking pace: 150 words per minute
               - Round to nearest 15 seconds
               - Should align with approximately %s
            
            3. **wordCount:**
               - Exact word count of the transcript
               - MUST be between %d and %d
               - Count carefully before submitting
            
            **TTS-Friendly Examples:**
            
            BAD: "Check the config.yaml file (located in /etc/) for settings."
            GOOD: "Look at the configuration file for the settings you need."
            
            BAD: "There are 3 main approaches: 1) async, 2) sync, 3) hybrid."
            GOOD: "There are three main approaches. The first is asynchronous. The second is synchronous. And the third is a hybrid of both."
            
            BAD: "The CEO said 'We're excited!' [applause]"
            GOOD: "The CEO said they were excited about the announcement."
            
            **Natural Flow Example (%s size):**
            %s
            
            **Remember:**
            - Size: %s
            - Words: %d to %d (MANDATORY)
            - Sentences: %d to %d (MANDATORY)
            - Duration: approximately %s
            - Style: %s
            
            Now create the transcript following ALL rules carefully.
            Count your words and sentences before submitting!
            """;

    public String getSystemPrompt() {
        TranscriptSize targetSize = size != null ? size : TranscriptSize.MEDIUM;
        NoteCastStyle targetStyle = style != null ? style : NoteCastStyle.DEFAULT;

        return String.format(
                SYSTEM_PROMPT_TEMPLATE,
                targetSize.getLabel(),
                targetSize.getMinWords(),
                targetSize.getMaxWords(),
                targetSize.getMinSentences(),
                targetSize.getMaxSentences(),
                targetStyle.getLabel(),
                targetStyle.getPromptText(),
                targetSize.getLabel(),
                targetSize.getApproximateDuration(),
                targetSize.getPromptGuidance(),
                targetSize.getMinWords(),
                targetSize.getMaxWords(),
                targetSize.getMinSentences(),
                targetSize.getMaxSentences(),
                TTS_RULES
        );
    }

    public String getUserPrompt() {
        TranscriptSize targetSize = size != null ? size : TranscriptSize.MEDIUM;
        NoteCastStyle targetStyle = style != null ? style : NoteCastStyle.DEFAULT;

        return String.format(
                USER_PROMPT_TEMPLATE,
                targetStyle.getLabel(),
                targetStyle.getPromptText(),
                targetSize.getLabel(),
                targetSize.getApproximateDuration(),
                targetSize.getPromptGuidance(),
                targetSize.getMinWords(),
                targetSize.getMaxWords(),
                targetSize.getMinSentences(),
                targetSize.getMaxSentences(),
                noteContent,
                targetStyle.getLabel(),
                targetSize.getPromptGuidance(),
                targetSize.getMinWords(),
                targetSize.getMaxWords(),
                targetSize.getMinSentences(),
                targetSize.getMaxSentences(),
                targetSize.getApproximateDuration(),
                targetSize.getMinWords(),
                targetSize.getMaxWords(),
                targetSize.getLabel(),
                getExampleForSize(targetSize),
                targetSize.getLabel(),
                targetSize.getMinWords(),
                targetSize.getMaxWords(),
                targetSize.getMinSentences(),
                targetSize.getMaxSentences(),
                targetSize.getApproximateDuration(),
                targetStyle.getLabel()
        );
    }

    private String getExampleForSize(TranscriptSize size) {
        return switch (size) {
            case EXTRA_SHORT -> """
                    "Cloud computing saves money. It eliminates expensive hardware. You pay only for what you use. It scales automatically. Your infrastructure grows with your business. It's reliable too. Data backs up across multiple locations. That's why companies are moving to the cloud."
                    """;
            case SHORT -> """
                    "Let's talk about cloud computing. It's transforming how businesses operate. First, it cuts costs dramatically. You don't need to buy and maintain expensive servers anymore. Instead, you pay only for what you actually use. Second, it scales effortlessly. As your business grows, your infrastructure expands automatically. Third, it improves reliability. Your data is backed up across multiple secure locations. These benefits make cloud computing essential for modern companies."
                    """;
            case MEDIUM -> """
                    "Today we're exploring cloud computing and why it matters for businesses. Cloud computing fundamentally changes how companies manage their technology infrastructure. Let's start with cost savings. Traditional IT requires massive upfront investments in servers and equipment. With cloud computing, those capital expenses disappear. You simply pay a monthly fee based on your actual usage. This pay-as-you-go model makes technology accessible to companies of all sizes. Next, consider scalability. Your business isn't static. Traffic spikes during holidays. New features attract users. Cloud infrastructure handles these changes automatically. Finally, there's reliability. Cloud providers maintain redundant data centers worldwide. If one location has issues, your services stay online. This level of reliability would cost millions to build yourself. That's the power of cloud computing."
                    """;
            case LARGE -> """
                    "Welcome to our deep dive into cloud computing. Over the next several minutes, we'll explore how this technology is reshaping the business landscape and why it's becoming essential for companies of all sizes. Let's begin with the fundamentals. Cloud computing means using remote servers hosted on the internet to store, manage, and process data, rather than relying on local servers or personal computers. This shift represents a fundamental change in how we think about IT infrastructure. The first major benefit is cost reduction. Traditional data centers require substantial capital investment. You need to purchase servers, networking equipment, and cooling systems. You need physical space to house everything. You need staff to maintain it all. These costs add up quickly, often reaching millions of dollars before you process a single transaction. Cloud computing eliminates most of these expenses. Instead of buying equipment, you rent computing resources from providers like Amazon, Microsoft, or Google. You pay only for what you actually use, typically on a monthly basis. This transforms IT from a capital expense into an operating expense, making it much easier to manage financially. The second advantage is scalability. Businesses are dynamic. You might have steady traffic most of the year, then experience massive spikes during holidays or special events. Traditional infrastructure requires you to build capacity for these peak times, leaving resources idle the rest of the year. Cloud services scale automatically. When demand increases, the system allocates more resources. When demand drops, it scales back down. You're always paying for exactly what you need, nothing more, nothing less."
                    """;
            case EXTRA_LARGE -> """
                    "Welcome to our comprehensive exploration of cloud computing. Over the next fifteen minutes, we're going to take an in-depth look at this transformative technology, examining not just what it is, but how it works, why it matters, and how it's changing the way businesses operate in the digital age. So let's dive in. First, let's establish what we mean by cloud computing. At its core, cloud computing is the delivery of computing services over the internet. These services include servers, storage, databases, networking, software, analytics, and intelligence. Instead of owning and maintaining physical data centers and servers, companies access these resources from cloud providers on an as-needed basis. Think of it like electricity. You don't generate your own power. You don't maintain a power plant. You simply plug into the grid and pay for what you use. Cloud computing works the same way. Now, let's explore the economic transformation this enables. Traditional IT infrastructure requires massive capital investment. Imagine you're starting a new online business. Before cloud computing, you would need to estimate your maximum capacity, then purchase enough servers and equipment to handle that peak load. This meant spending hundreds of thousands or even millions of dollars upfront, before you had a single customer. You needed physical space to house the equipment. You needed specialized cooling systems to prevent overheating. You needed backup power systems. You needed security measures. And you needed skilled staff to manage everything around the clock. This high barrier to entry meant only well-funded companies could build robust online services. Cloud computing changes everything. Now you can start with minimal resources and pay only for what you use. Your first month might cost fifty dollars. As you grow, your costs increase proportionally, but so does your revenue. This pay-as-you-go model democratizes technology, allowing startups to compete with established players..."
                    """;
        };
    }
}