# llm-rewrite-inplace
## The Idea
Instrument a Ring handler to log its inputs and outputs, mark the sacred bits with (spare ...), and let an LLM rebuild the erased parts—right in the source file—when the handler’s called again. No manual REPL dance, no bloated automation loops—just a quiet, turn-based handshake between your intent and a language model’s imagination.

It’s about co-creation: you define the skeleton (input/output value logs and spared snippets), and the LLM sketches the flesh. It’s a dev-time toy that asks, "How much can we trust an AI to infer from what we’ve already proven works?" Think of it as a mirror—reflecting your handler’s behavior back into code—or a forge, reshaping it around what you’ve chosen to keep.

## Potential Extensions

  * Self-Refinement: Instrument the tool’s own functions, log their behavior, and let the LLM refine itself. A recursive twist—less a Quine, more a craftsman sharpening its own tools.
  * Multi-Handler Symphony: Scale it to an entire Ring app. Spare key logic across routes, let the LLM harmonize the rest. Could it infer a whole API from scattered logs?
  * Prompt Tuning: Feed it richer logs—timestamps, edge cases, HTTP quirks—and tweak the LLM prompts. Maybe fine-tune a model just for Clojure idioms.
  * Sandbox Safety: Wrap the rewrite in a sandbox. LLM hallucinations are fun until they rm -rf something. Safety nets could make this production-curious.
  * Versioned Rewrites: Keep a history of each rewrite. Diffs could reveal how the LLM "thinks" about your code over iterations.

# Pairing with an HTTP API Exerciser
The real dream—yours—is to marry this with an extensive external HTTP API exerciser. Imagine a tool that hammers your Ring app with every imaginable request: GETs, POSTs, edge-case payloads, 400s, 500s—the works. It’s TDD on steroids, defining behavior not with assertions but with a flood of real-world calls. Each hit logs an input-output pair, painting a vivid portrait of what your API should do.
Then, llm-rewrite-inplace steps in. The exerciser’s logs become the spec—comprehensive, messy, alive. The (spare ...) macro guards your must-keep logic (a DB call, a status code), and the LLM fills the gaps. Automatically? Maybe. Picture this: the exerciser runs, logs pile up, and on the next handler call, the LLM rewrites it to match all that behavior. No hand-holding—just a button press, a fetch, and a transformed file.
This flips traditional TDD. Instead of writing tests to drive code, you exercise the API to define it, then let the LLM infer the implementation. It’s behavior-driven development where the behavior comes from the wild, not your imagination. The gaps—control flow, edge handling—become the LLM’s playground. Could it work?
