# Repository Guidelines

## Project Structure & Module Organization

This is a multi-module Android Gradle project. The runnable app lives in `app/`, while feature and rendering code is split across `biz/opengl`, `biz/filament`, `biz/rajawali`, `biz/flow-test`, `biz/ui-test`, and `biz-center/render-engine`. Unity-exported code and native assets are under `biz/unityLibrary`; avoid editing generated Unity or `build/` outputs unless regenerating that module deliberately. Shared Gradle setup is in `scripts/`, with module membership declared in `settings.gradle`.

Source code is under each module's `src/main/java`, Android resources under `src/main/res`, assets under `src/main/assets`, and shaders/materials under paths such as `biz/opengl/src/main/res/raw` and `biz-center/render-engine/src/main/materials`.

## Build, Test, and Development Commands

- `./gradlew :app:assembleDebug` builds the debug APK.
- `./gradlew :app:installDebug` installs the debug APK on a connected device or emulator.
- `./gradlew :biz:opengl:assembleDebug` builds a library module in isolation; replace the module path as needed.
- `./gradlew testDebugUnitTest` runs local JVM unit tests when test sources exist.
- `./gradlew connectedDebugAndroidTest` runs instrumented Android tests on a connected device.
- `./gradlew clean` removes Gradle build outputs.

## Coding Style & Naming Conventions

Use Kotlin for new Android code unless extending existing Java sources. Follow the existing style: 4-space indentation, `UpperCamelCase` classes, `lowerCamelCase` methods and properties, and package names under `com.mashiro.*`. Keep Android resource names lowercase with underscores, for example `layout_flow_test.xml` or `simple_vertex_shader.glsl`. Prefer small Activity/ViewModel/helper classes aligned with the owning module rather than adding cross-module utility code.

## Testing Guidelines

No `src/test` or `src/androidTest` directories are currently committed. Add local tests under `<module>/src/test/java` and instrumented tests under `<module>/src/androidTest/java`. Name tests after the behavior under test, such as `ShaderHelperTest` or `FlowViewModelTest`, and run the relevant Gradle test task before submitting changes.

## Commit & Pull Request Guidelines

Recent commits mostly use a bracketed type prefix, for example `[feature]改善扫光动画`. Keep messages concise and descriptive; avoid placeholder commits such as `g`. For PRs, include a short summary, affected modules, manual test notes or Gradle output, linked issues if applicable, and screenshots or screen recordings for UI/rendering changes.

## Security & Configuration Tips

Do not commit new credentials, keystores, device paths, or generated build artifacts. Keep machine-specific configuration in `local.properties`, which is already ignored.
