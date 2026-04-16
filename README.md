# Semantic Identifiers

An IntelliJ IDEA plugin that uses the Google Gemini AI API to suggest meaningful, context-aware names for any identifier in your code.

> **Note:** This plugin is currently in development and not yet production-ready. It will be published on the [JetBrains Marketplace](https://plugins.jetbrains.com/) once it is fully ready. For now, the only way to install it is by [building from source](#installation).

<!-- Plugin description -->
**Semantic Identifiers** helps you write cleaner, more readable code by suggesting descriptive names for any identifier — variables, parameters, functions, classes, constants, and more. Place your cursor on any named element, invoke the action, and receive three AI-generated name suggestions tailored to the context of your code.
<!-- Plugin description end -->

## Features

- Suggests three context-aware names for **any semantic identifier** (variables, parameters, functions, classes, fields, constants, etc.)
- Works with **every programming language** supported by IntelliJ IDEA — Java, Kotlin, Python, JavaScript, TypeScript, Go, Rust, PHP, and more
- Analyzes surrounding code (30 lines of context) to produce meaningful suggestions
- Accessible via right-click context menu, **Alt+Shift+S**, or the **Alt+Enter** intention menu
- API key stored securely in the OS credential store (Windows Credential Manager / macOS Keychain / KDE Wallet)
- Lightweight — no bundled AI models, all processing happens via the Gemini API
- Runs API calls in the background without blocking the IDE

## Requirements

- IntelliJ IDEA **2024.3** or later (build 243+)
- A free Google Gemini API key

## Setup

1. Get a free API key from [Google AI Studio](https://aistudio.google.com).
2. Open **Settings → Tools → Semantic Identifiers** and paste your key.

## Usage

1. Place your cursor on any identifier (variable, function, class, parameter, etc.).
2. Trigger via **right-click → Suggest Semantic Name**, **Alt+Shift+S**, or **Alt+Enter**.
3. A dialog shows three AI-suggested names based on the surrounding code.

## Installation

Since the plugin is not yet published on the JetBrains Marketplace, you need to build and install it manually:

```bash
./gradlew buildPlugin
```

The built plugin ZIP will be in `build/distributions/`. Install it via **Settings → Plugins → Install Plugin from Disk...** and select the ZIP file.

## Tech Stack

- **Language:** Kotlin
- **Platform:** IntelliJ Platform SDK (2025.1)
- **AI Provider:** Google Gemini API (`gemini-2.5-flash`)
- **Build System:** Gradle with IntelliJ Platform Gradle Plugin