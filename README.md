<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/c590683a-3bd1-4e13-89a2-db0a1cdfb22f

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device
7. If you have already published your app in AI Studio, please [request upload key reset](https://support.google.com/googleplay/android-developer/answer/9842756#zippy=%2Crequest-an-upload-key-reset) in Google Play Console.


## APK builds

GitHub Actions builds the Android APK automatically on pushes to `main`, pull requests, and manual runs.

- **Debug APK:** `.github/workflows/build-apk.yml`
- **Tagged release:** push a tag such as `v1.0.0` to build and attach the APK to a GitHub Release.
- The debug APK is uploaded as the `focusforge-debug-apk` Actions artifact.

The current Study Mode is whitelist-first: ordinary YouTube navigation remains usable, but a detected YouTube watch/video page is blocked unless the visible channel identity matches a saved whitelist entry. Generic words such as `math` or `physics` are not sufficient to bypass the block.
