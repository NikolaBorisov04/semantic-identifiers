# Semantic Identifiers

An IntelliJ IDEA plugin that uses the Google Gemini AI API to suggest meaningful, semantic names for variables and functions based on the surrounding code context.

<!-- Plugin description -->
**Semantic Identifiers** helps you write cleaner code by suggesting descriptive names for your variables and functions. Place your cursor on any identifier, invoke the action, and receive three AI-generated name suggestions tailored to the context of your code.
<!-- Plugin description end -->

## Features

- Suggests 3 context-aware names for any variable, parameter, or function
- Accessible via right-click context menu, **Alt+Shift+S**, or the **Alt+Enter** intention menu
- API key stored securely in the OS credential store (Windows Credential Manager / macOS Keychain)

## Setup

1. Get a free API key from [Google AI Studio](https://aistudio.google.com).
2. Open **Settings → Tools → Semantic Identifiers** and paste your key.

## Usage

1. Place your cursor on a variable or function name.
2. Trigger via **right-click → Suggest Semantic Name**, **Alt+Shift+S**, or **Alt+Enter**.
3. A dialog shows three AI-suggested names based on the surrounding code.

## Building from Source

```bash
./gradlew buildPlugin
```

Output: `build/distributions/`. Install via **Settings → Plugins → Install Plugin from Disk**.
