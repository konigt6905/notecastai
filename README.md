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
- Groq API (audio transcription)
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
- Groq API key (transcription)
- OpenRouter API key (AI generation)

