# Notecast AI

Voice notes processing platform with AI-powered transcription, knowledge management, and content generation.

## What It Does

- **Voice Notes**: Upload and automatically transcribe audio files
- **Smart Notes**: AI-enhanced knowledge base with formatting and Q&A
- **NoteCasts**: Generate AI content (summaries, articles, etc.) from your notes
- **Organization**: Tag-based system for managing your content

## Tech Stack

**Backend**
- Java 17
- Spring Boot 3.5.7
- PostgreSQL (with Flyway migrations)
- Spring Security + OAuth2 (Clerk JWT)

**AI/Storage**
- OpenAI API (voice-note transcription + text-to-speech)
- OpenRouter API (AI text generation)
- AWS S3 (file storage)

**Documentation**
- SpringDoc OpenAPI 3 / Swagger UI

## Quick Start

### Prerequisites
- Java 17+
- PostgreSQL 12+
- AWS S3 bucket
- Clerk account (authentication)
- OpenRouter API key (AI generation)
- OpenAI API key (transcription + text-to-speech)

### AI Configuration

Set the following environment variables (or Spring properties) before running the service:

| Property | Description |
| --- | --- |
| `AI_OPENROUTER_API_KEY` | Access token for OpenRouter chat models used for transcripts/notes |
| `AI_OPENAI_KEY` | OpenAI key used for voice-note transcription (Whisper) and text-to-speech synthesis |
| `AI_OPENAI_API_URL` *(optional)* | Override the default `https://api.openai.com/v1` endpoint |
| `AI_TTS_VOICE_PROVIDER` *(optional)* | Selects which TTS catalog to expose (`OPENAI` or `KOKORO`) |

All values map to the `ai.*` namespace inside `application.yaml`.
