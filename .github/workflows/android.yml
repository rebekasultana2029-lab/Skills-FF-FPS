name: Build Skills FPS

on:
  push:
    branches: [ "main" ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Build with Gradle
        uses: gradle/gradle-build-action@v2
        with:
          arguments: assembleDebug

      - name: Upload APK
        uses: actions/upload-artifact@v3
        with:
          name: SkillsFPS-debug-apk
          path: app/build/outputs/apk/debug/app-debug.apk
