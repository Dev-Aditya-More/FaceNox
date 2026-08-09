# Implement Camera Feature for Android

This plan details the steps to implement the camera functionality in the Android application, replacing the current "Coming soon" placeholder.

## User Review Required

> [!NOTE]
> This implementation uses the system camera app via an Intent (`ActivityResultContracts.TakePicture`). This is the most compatible way to add camera support quickly while ensuring a smooth flow.

## Proposed Changes

### [sharedUI]

#### [MODIFY] [ImagePicker.kt](file:///C:/Users/adity/Cross_Platform/FaceNox/sharedUI/src/commonMain/kotlin/org/aditya1875/facenox/platform/ImagePicker.kt)
- Add `takePhoto(onResult: (String?) -> Unit)` to the `ImagePicker` interface.

#### [MODIFY] [ImagePicker.jvm.kt](file:///C:/Users/adity/Cross_Platform/FaceNox/sharedUI/src/jvmMain/kotlin/org/aditya1875/facenox/platform/ImagePicker.jvm.kt)
- Add a stub implementation for `takePhoto`.

#### [MODIFY] [ImagePicker.android.kt](file:///C:/Users/adity/Cross_Platform/FaceNox/sharedUI/src/androidMain/kotlin/org/aditya1875/facenox/platform/ImagePicker.android.kt)
- Implement `takePhoto` using `ActivityResultContracts.TakePicture()`.
- Handle temporary file creation and URI generation using `FileProvider`.

#### [MODIFY] [ImageSelectionScreen.kt](file:///C:/Users/adity/Cross_Platform/FaceNox/sharedUI/src/commonMain/kotlin/org/aditya1875/facenox/feature/screens/imageselection/ImageSelectionScreen.kt)
- Enable the Camera button.
- Call `picker.takePhoto` when the Camera button is clicked.

---

### [androidApp]

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/adity/Cross_Platform/FaceNox/androidApp/src/main/AndroidManifest.xml)
- Add `android.permission.CAMERA` permission (optional but recommended for some devices).
- Declare a `FileProvider` to share temporary image files with the camera app.

#### [NEW] [file_paths.xml](file:///C:/Users/adity/Cross_Platform/FaceNox/androidApp/src/main/res/xml/file_paths.xml)
- Define the paths for the `FileProvider`.

## Verification Plan

### Manual Verification
1. Run the `:androidApp` on an Android device or emulator.
2. Navigate to the Image Selection screen.
3. Verify that the "Camera" button is now enabled and no longer says "Coming soon".
4. Tap the "Camera" button.
5. Verify that the system camera app opens.
6. Take a photo and confirm.
7. Verify that the app returns to the selection screen and then proceeds to the next screen with the captured image.
