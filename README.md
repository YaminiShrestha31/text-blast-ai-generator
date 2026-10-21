# Text Blast – AI Content Generator

React (Vite) UI + **Spring Boot 3 (Java 17)** backend. The backend calls a configurable LLM (Gemini / Ollama / Claude) to generate 3 SMS variants
from campaign type, tone, length and emoji options. API keys stay on the server.

## Run
Pick ONE LLM provider (default is Gemini, which has a free tier).

```bash
# Option A – Google Gemini (free): get a key at https://aistudio.google.com/apikey
export GEMINI_API_KEY=your_key


```bash
# terminal 1 – backend (http://localhost:8080)
cd backend && mvn spring-boot:run

# terminal 2 – frontend (http://localhost:5173, proxies /api to 8080)
cd client && npm install && npm run dev
```
Tests: `cd backend && mvn test`

## Design
`ContentService` builds the prompt and post-processes results; it depends only on the `LlmClient` interface.
`GeminiLlmClient`, `OllamaLlmClient` and `AnthropicLlmClient` implement it, and `llm.provider` selects one.

## API
`POST /api/generate`
```json
{ "prompt": "Black Friday 25% off interior painting", "campaignType": "Promotional", "tone": "Friendly", "limit": true, "emoji": true }
```
Returns `{ "variants": ["...", "...", "..."] }`; errors return `{ "error": "..." }`.

## Notes
- Limit ON: model told to stay under 1600 chars, server hard-truncates as a safety net. OFF: UI shows an SMS cost disclaimer.
- Emoji OFF: model told not to use emoji, server strips any that slip through.
- Models are configurable via `GEMINI_MODEL`, `OLLAMA_MODEL`, `ANTHROPIC_MODEL`.
