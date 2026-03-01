# SmolChat Android Library Conversion (POC Scope)

## Goal
Convert the project into a set of pure, headless library modules. The primary POC feature is a **single-function API** to manage LLM models: download them in the background (with notifications) and load them for inference, all without UI.

## Module Roles

### 1. `:smollm`
- **Role**: Core JNI bindings for `llama.cpp`. Handles raw inference.
- **Namespace**: `io.shubham0204.smollm`

### 2. `:hf-model-hub-api`
- **Role**: JVM library for Hugging Face metadata.
- **Namespace**: `io.shubham0204.smollm.hf_api`

### 3. `:smollm-core` (Currently :smolchat-core)
- **Role**: The "One Function" entry point for consumers.
- **Key API**: `SmolLMClient.loadModel(modelId: String): ModelStatus`
- **Persistence**: `SharedPreferences`.
- **Namespace**: `io.shubham0204.smolchat.core` (to be consolidated to `io.shubham0204.smollm.core`)

### 4. `:demo` (Application Module)
- **Role**: Verification app for the library.

---

## Milestones

### Milestone 1: Seamless Download & Persistence
- [x] Press a button in the `:demo` app.
- [x] Fake implementation of state transitions with coroutines.
- [x] State polling logic from Repository to UI.
- [x] Koin dependency injection in Demo app.
- [ ] **UNDONE**: Implement actual `DownloadManager` logic in `:smollm-core`.
- [ ] **UNDONE**: Implement background notification during download.
- [ ] **UNDONE**: Persist real download IDs in `SharedPreferences`.

### Milestone 2: Headless Inference
- [ ] Once model is `ON_DISK`, load it into memory.
- [ ] Automatically submit the prompt "hello".
- [ ] Display the LLM's response in the `:demo` UI.
- [ ] **UNDONE**: Connect `:smollm` JNI to the core for real verification.

### Milestone 3: Clean up and Tidy Code
- [ ] **Rename libraries**: Remove all references of "chat" from library modules and package names.
- [ ] **Namespace Consolidation**: Standardize all libraries under `io.shubham0204.smollm`.
- [ ] **Module Renaming**: Rename `:smolchat-core` to `:smollm-core`.
- [ ] update readme and delete modules that are no longer needed.

### Milestone 4: Local Maven Testing (RC)
- [ ] Configure `maven-publish` plugin for all modules.
- [ ] Publish to `mavenLocal()`.
- [ ] Import and verify in a local build of `RaccoonForLemmy`.
- [ ] develop simple feature in `RaccoonForLemmy` to use the new library and open PR in RaccoonForLemmy repo.

### Milestone 5: Maven Central Publication
- [ ] Setup Sonatype credentials and GPG signing.
- [ ] Configure POM metadata for all artifacts.
- [ ] First release to Maven Central.

---

## Operational Rules
- **No File Edits**: The AI will not create, edit, or remove any files unless explicitly and clearly instructed by the user for a specific file.

## Decisions
- **Persistence**: `SharedPreferences`.
- **UI**: Only in the `:demo` module.
- **DI**: Koin for the `:demo` app.
