# Contributing to TakaKoi 📱

Thank you for your interest in contributing to **TakaKoi**! We welcome contributions to help improve the personal finance and budget tracking experience across Android and iOS.

---

## 🌿 Branching Strategy & Workflow

TakaKoi uses a structured Git workflow to maintain stability while allowing active feature development:

```
feature/my-feature ──┐
                     ├──> develop (Preview / Active Work) ──> main (Stable / Tagged Release)
fix/bug-fix ─────────┘
```

### Branch Roles

- **`main`**: The reviewed stable baseline. Code here must be green in both Android and iOS CI. Use pull requests from `develop`; store-ready signing is intentionally not part of the current CI artifacts.
- **`develop`**: The active integration branch. Preview APKs and frameworks are built on pushes from here. Developers branch off `develop` and merge back into `develop`.
- **`feature/<name>`**: Short-lived feature branches created from `develop` for specific features (e.g. `feature/recurring-payments`).
- **`fix/<name>`**: Short-lived bug fix branches created from `develop` for resolving specific issues (e.g. `fix/payday-calculator`).

---

## 🚀 Local Development Setup

### 1. Prerequisites

- **JDK 21**: Install OpenJDK 21 (e.g. `brew install openjdk@21`).
- **Android SDK**: API 36 target, API 24 minimum.
- **Xcode 15+**: Required for building and running the iOS app container.

### 2. Environment Configuration

Ensure your `JAVA_HOME` environment variable points to JDK 21:

```bash
export JAVA_HOME="$(brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home"
```

> ⚠️ **Important**: macOS system defaults (Java 26+) break the Android SDK `jlink` module processing. Always ensure JDK 21 is set in your shell before running Gradle commands.

---

## 🛠️ Building & Testing Locally

Before opening a pull request, ensure both Android and iOS targets compile without errors.

### Android Debug Build

```bash
JAVA_HOME="$(brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home" \
  ./gradlew assembleDebug --no-configuration-cache
```

### iOS Framework Build

```bash
JAVA_HOME="$(brew --prefix openjdk@21)/libexec/openjdk.jdk/Contents/Home" \
  ./gradlew :shared:linkDebugFrameworkIosSimulatorArm64 --no-configuration-cache
```

### Finance and database regression tests

```bash
./gradlew :app:testDebugUnitTest \
  --tests 'com.example.CashflowCalculationsTest' \
  --tests 'com.example.CashflowUnitTest' \
  --tests 'com.example.MoneyTest' \
  --tests 'com.example.DatabaseMigrationTest' \
  --no-configuration-cache
```

These tests use synthetic data and disposable SQLite files. Database preservation tests run with generated Room DAOs and Robolectric's native SQLite through `AndroidSQLiteDriver` on API 24 and 36; they are not physical-device or Android bundled-driver verification. The production builder still defaults to `BundledSQLiteDriver`. Keep the generated Room schema in `shared/schemas/` under version control and add migration-preservation tests when changing entities. Never enable destructive migration fallback for finance data.

`domain/money/Money` uses checked integer minor units and explicit currency precision. It rejects overflow, mixed currencies, ambiguous grouping, and excess decimal places, and accepts ASCII/Bangla digits. Existing Room/UI amount fields still use `Double`; adding this primitive does not migrate or encrypt existing amounts.

On macOS, run `./gradlew :shared:iosSimulatorArm64Test --no-configuration-cache` for shared money invariants and native Room reopen/preservation tests. Set `TAKAKOI_TEST_SIMULATOR_ID` to an available simulator UUID to select an explicit test device. These tests use temporary synthetic databases, not app data; they do not validate UIKit, encryption, biometrics, or physical-device behavior.

---

## 📐 Code Style & Conventions

- **Kotlin**: Follow the official [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).
- **Compose Multiplatform**:
  - Keep UI components decoupled and stateful logic inside `ExpenseViewModel`.
  - Use Material Design 3 tokens and Space Grotesk typography defined in `shared/src/commonMain/kotlin/com/example/shared/ui/theme/`.
  - Prefer auto-mirrored icons for directional icons (`Icons.AutoMirrored.Filled.*`).
- **Swift / iOS**: Follow standard Swift conventions in `iosApp/`.

---

## 📥 Pull Request (PR) Guidelines

1. **Create a branch** off `develop`:
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/my-new-feature
   ```
2. **Commit your changes** with clear, semantic commit messages (e.g., `feat: add PDF report generator`, `fix: correct currency formatting in Ledger`).
3. **Run local verification** (`./gradlew assembleDebug` and `:shared:linkReleaseFrameworkIosSimulatorArm64`).
4. **Push to GitHub** and open a PR against the `develop` branch.
5. Ensure all **GitHub Actions CI checks** (`Build Android APK` and `iOS Framework Build`) pass cleanly.

---

## 📄 License

By contributing to TakaKoi, you agree that your contributions will be licensed under the project's [Apache License 2.0](LICENSE).

## 🔐 Public-repository rules

- Never commit `.env`, API keys, service-account files, keystores, provisioning profiles, certificates, or local IDE state.
- Keep sample/demo values fictional and avoid real names, addresses, account numbers, or financial records in source, screenshots, and tests.
- Do not add analytics, cloud sync, or network calls without updating [PRIVACY.md](PRIVACY.md) and the store disclosure plan.
- Report security issues privately using the process in [SECURITY.md](SECURITY.md); do not publish credentials in an issue or pull request.
