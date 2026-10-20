---
name: yt-notes
description: Create structured study notes from a YouTube video. Use when the user gives a YouTube URL/ID and wants notes, a summary, or learning material from it.
argument-hint: <youtube-url-or-id> [focus or extra instructions]
---

Create learning notes for the YouTube video: $ARGUMENTS

1. Fetch the transcript with the `youtube` MCP server's transcript tool (`getTranscripts`). If video details are available (title, channel, duration), fetch them too; skip them if they fail (no API key is configured).
2. Write the notes to `notes/<kebab-case-video-title>.md` using this structure:

   ```markdown
   # <Video title>
   > Source: <url> · Channel: <channel> · Notes created: <date>

   ## TL;DR
   3–5 bullets with the core takeaways.

   ## Key Concepts
   For each concept: a short explanation in plain words, plus why it matters.

   ## Detailed Notes
   Follow the video's flow under section headings. Add approximate timestamps like [12:34] so the user can jump back to that part.

   ## Code Examples
   Rewrite any code shown or described as clean, runnable snippets (Java unless the video uses another language). Leave this section out if the video has no code.

   ## Common Pitfalls / Tips

   ## Review Questions
   5–8 questions to test understanding. Put the answers in a collapsed <details> block.

   ## Further Study
   Related topics to look up next.
   ```

3. Write the notes yourself; don't paste the transcript. Fix transcription errors in technical terms (e.g. "java stream" → `Stream`). If the user gave a focus, put more depth there.
4. Reply with the file path and the TL;DR.
