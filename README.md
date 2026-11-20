# GoNix EduPlus

## Hybrid AI for Learning — Local Intelligence. Cloud Power. One AI Experience.

**Learn More. Become More.**

GoNix EduPlus is an AI-powered learning and productivity platform built around a hybrid inference architecture. It combines supported on-device AI with Firebase AI Logic cloud inference, allowing the application to adapt to device capabilities and user preferences.

Our goal is to make AI-assisted learning more accessible while treating privacy, user choice and responsible inference as core engineering requirements.

---

## 1. The Challenge

Learners increasingly rely on AI to understand difficult topics, study, summarise information and improve productivity. However, cloud-only AI experiences can create barriers:

- Continuous connectivity may be unavailable, unreliable or expensive.
- Remote inference can introduce additional latency.
- Devices have different AI capabilities.
- Users need meaningful control over whether their prompts are sent to cloud services.

GoNix EduPlus addresses these challenges with a hybrid architecture that supports local inference where available and consent-controlled cloud inference when appropriate.

## 2. Our Solution

GoNix EduPlus brings together three AI execution capabilities.

### Chrome On-Device AI

On supported Chrome environments, the web application detects the browser's on-device Language Model API and can use Firebase AI Logic's on-device inference path.

Availability depends on browser version, device capability, secure-context requirements and model availability. Detecting the API does not by itself guarantee that a model is ready to run.

### Native Android On-Device AI

The Android application includes a native Capacitor plugin that integrates Firebase AI Logic's on-device model capabilities.

The native integration exposes these model choices:

| Model choice | Firebase AI Logic option |
|---|---|
| Gemini Nano 4 Full Preview | `OnDeviceModelOption.PREVIEW` |
| Gemini Nano 4 Fast Preview | `OnDeviceModelOption.PREVIEW_FAST` |
| Stable option | `OnDeviceModelOption.STABLE` |

The default selection is Gemini Nano 4 Full Preview. The plugin supports model status checks, model download handling with progress events, and on-device generation. It verifies that the returned inference source is `ON_DEVICE`.

Model availability is device- and software-dependent. The presence of the native integration does not mean Gemini Nano is available on every Android device.

### Firebase AI Logic Cloud Inference

For cloud inference, the application is configured to use **`gemini-3.5-flash-lite`** through Firebase AI Logic.

This is the cloud model, distinct from the Gemini Nano 4 on-device model options. Cloud execution requires network connectivity, valid Firebase configuration, App Check configuration and the user's explicit cloud permission.

## 3. Hybrid Inference Modes

The shared inference engine supports four strategies:

| Mode | Behaviour |
|---|---|
| `PREFER_ON_DEVICE` | Prioritises on-device inference. Cloud use is subject to explicit consent and platform safeguards. |
| `PREFER_IN_CLOUD` | Prioritises cloud inference and requires cloud consent. |
| `ONLY_ON_DEVICE` | Uses local inference only; cloud fallback is prohibited. |
| `ONLY_IN_CLOUD` | Uses cloud inference only and requires cloud consent. |

The engine checks the execution environment, local model capability, selected mode and consent state. It validates the reported inference source before accepting a response.

On Android, cloud inference is deliberately blocked in the current implementation until native App Check integration is fully verified. If Gemini Nano is unavailable, the application reports the limitation rather than silently sending the prompt to the cloud.

## 4. Architecture

```text
                     GoNix EduPlus
                           |
                       AI Request
                           |
                  Shared Hybrid Engine
                           |
                  Read AI Preferences
                           |
                Detect Execution Platform
                           |
                +----------+----------+
                |                     |
              Chrome                Android
                |                     |
       Browser LanguageModel   Native Firebase AI
            API / AI Logic       Logic integration
                |                     |
          On-device AI           Gemini Nano 4
                |                     |
                +----------+----------+
                           |
                  Verify inference source
                           |
                    Return response

       Consent-controlled cloud path (where enabled):
                           |
                   Firebase AI Logic
                           |
                 gemini-3.5-flash-lite
```

## 5. Educational Experience

GoNix EduPlus is designed for students, teachers, lecturers and lifelong learners. The application brings AI assistance into an education-focused environment with learning and productivity areas such as:

- Learning dashboard, courses and curriculum.
- Student Studio and Lecturer Studio.
- Assessments, certificates and learning progress.
- AI-assisted learning and platform assistance.
- Community, messaging, notifications and user settings.

The platform's wider ambition is to make practical AI tools more accessible to learners, including people working with limited connectivity or a broad range of device capabilities.

Some wider platform areas may use demonstration or session-local data. Their presence in the interface should not be interpreted as proof that every feature has a production backend.

## 6. Privacy and Security

Privacy-aware execution is built into the inference flow:

- Cloud consent is explicit and defaults to disabled.
- Choosing a cloud-oriented mode does not silently grant consent.
- Local-only mode cannot fall back to cloud inference.
- Cloud requests are gated by Firebase App Check configuration.
- The application checks the inference source reported by Firebase AI Logic.
- Prompt length is validated.
- Transient cloud failures use bounded retry handling.
- Firebase configuration is supplied through environment variables.

**Verification status:** App Check initialization is integrated into the web and Android configurations. Runtime token acquisition and enforcement must be verified before App Check enforcement is enabled. Android cloud inference remains blocked pending native App Check verification.

On-device inference depends on supported hardware, operating-system and browser capabilities, and model availability. Cloud inference requires connectivity and correctly configured Firebase services.

## 7. Technology Stack

| Technology | Purpose |
|---|---|
| React 19 | User interface |
| TypeScript 5.9 | Application logic and type safety |
| Vite 8 | Development and production builds |
| Capacitor 8 | Native Android integration |
| Kotlin and Java | Native Android implementation |
| Firebase AI Logic | On-device and cloud AI integration |
| Gemini Nano 4 | Supported on-device AI model options |
| Gemini 3.5 Flash Lite | Cloud inference model |
| Firebase App Check | Application attestation |
| Firebase Hosting | Web application hosting |
| jsPDF and DOCX tooling | Document export capabilities |
| Recharts | Data visualisation |

## 8. Current Implementation Status

The following table distinguishes code present in the repository from capabilities that still require runtime verification.

| Capability | Implementation status |
|---|---|
| React and TypeScript application | Implemented |
| Shared hybrid inference engine | Implemented |
| Four inference modes | Implemented |
| Chrome on-device API detection | Implemented; model execution requires compatible-browser testing |
| Firebase AI Logic cloud integration | Implemented; live inference requires runtime verification |
| Explicit cloud-consent controls | Implemented |
| Streaming cloud responses | Implemented |
| Inference-source validation in web inference path | Implemented |
| Native Android Gemini Nano plugin | Implemented |
| Android model status and download handling | Implemented |
| Android on-device generation | Implemented; compatible-device testing required |
| Web and Android App Check configuration | Integrated; runtime verification required |
| Android cloud fallback | Blocked pending native App Check verification |
| Production authentication and persistent platform services | Further implementation and verification required |

A device may report Gemini Nano as unavailable even when the native integration is installed correctly. The application must communicate that state honestly.

## 9. Live Application and Repository

**Web application:** https://gonix-eduplus.web.app

**Source code:** https://github.com/MelcommGlobal-TechHub/GoNix-EduPlus-CrossPlatform

**Firebase project:** `gonix-hybrid-ai`

The EduPlus web application uses its dedicated Firebase Hosting site. The separate GoNix Hybrid AI Hosting deployment is maintained independently.

## 10. Run Locally

### Requirements

- Node.js 24 recommended.
- npm.
- A Firebase project and registered web application.
- A supported browser for on-device inference testing.
- Android SDK and Java/Kotlin toolchain for native Android development.

### Install Dependencies

```bash
npm ci
```

### Configure Firebase

Create a `.env.local` file in the project root:

```env
VITE_FIREBASE_API_KEY=your_firebase_web_api_key
VITE_FIREBASE_AUTH_DOMAIN=your_project.firebaseapp.com
VITE_FIREBASE_PROJECT_ID=your_project_id
VITE_FIREBASE_APP_ID=your_web_app_id
VITE_RECAPTCHA_ENTERPRISE_SITE_KEY=your_web_recaptcha_site_key
```

Use the configuration belonging to the intended Firebase web app. Do not commit `.env.local`, service-account credentials or private secrets.

### Start the Development Server

```bash
npm run dev
```

### Build the Web Application

```bash
npm run build
```

### Preview the Production Build

```bash
npm run preview
```

### Build Android

The Android build requires the registered Firebase Android configuration and the Android App Check build configuration.

```bash
npx cap sync android
cd android
./gradlew assembleDebug
```

## 11. Project Structure

```text
.
├── .github/workflows/       # Android build workflow
├── android/                 # Capacitor Android project and native Kotlin code
├── public/                  # Static assets
├── src/
│   ├── components/          # Application components
│   ├── context/             # Shared AI and application settings
│   ├── layouts/              # Application layouts
│   ├── lib/
│   │   ├── firebase.ts      # Firebase initialization
│   │   ├── hybrid-ai.ts     # Shared hybrid inference engine
│   │   └── native-gemini-nano.ts
│   ├── pages/               # Application pages
│   ├── App.tsx
│   └── main.tsx
├── .env.example
├── package.json
├── firebase.json
└── vite.config.ts
```

## 12. Development Roadmap

### Hybrid AI Validation

- Verify Chrome on-device inference on supported devices.
- Verify Gemini Nano 4 Full Preview and Fast Preview on compatible Android hardware.
- Validate Firebase AI Logic cloud inference.
- Complete native Android App Check verification.
- Test consent, source verification and failure handling.

### Learning and Productivity

- Continue improving AI-assisted learning workflows.
- Expand educational productivity tools.
- Improve learner progress and persistent platform services.
- Continue device compatibility, accessibility and performance testing.

## 13. Why GoNix EduPlus?

GoNix EduPlus explores a practical direction for educational AI: **the future should not be cloud-only.**

By combining on-device Gemini Nano capabilities with Firebase AI Logic cloud inference, the platform is designed to adapt to device capability, connectivity and user consent.

It brings this architecture into an education-focused product with a clear ambition: help more learners access intelligent learning and productivity tools while making responsible AI behaviour part of the engineering design.

**GoNix EduPlus — Learn More. Become More.**

**Local Intelligence. Cloud Power. One AI Experience.**

---

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE).
