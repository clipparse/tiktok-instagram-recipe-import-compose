# Import recipes from TikTok and Instagram links in a Jetpack Compose app

A small Kotlin and Jetpack Compose app that imports a recipe from a TikTok or Instagram link with the [ClipParse Android SDK](https://github.com/clipparse/clipparse-android-sdk). Paste a link, or share a post from the TikTok or Instagram app, and get back the recipe as structured data: the title, ingredients with quantities and units, ordered steps, timings and servings.

The code is meant to be copied: take what you need into your own app.

## Run it

1. Get a free ClipParse API key at [clipparse.com/get-free-key](https://clipparse.com/get-free-key). It takes seconds and doesn't require a credit card.
2. Copy `secrets.example.properties` to `secrets.properties` and paste your key. `secrets.properties` is gitignored, and the build needs it.
3. Open the project in Android Studio and run the `app` configuration, or run `./gradlew :app:installDebug` with a device or emulator connected.

The app runs on Android 6.0 (API level 23) or later.

## What's in it

| File | What it does |
| --- | --- |
| [`RecipeImportViewModel.kt`](app/src/main/kotlin/com/example/recipeimport/RecipeImportViewModel.kt) | Calls the SDK and exposes the import state to the UI. `importErrorMessage` turns each error into a message for the user, and `amountText` formats an ingredient's amount ("2–3 tbsp"). |
| [`ImportScreen.kt`](app/src/main/kotlin/com/example/recipeimport/ImportScreen.kt) | The screen: a link field, an Import button, and the recipe, with its thumbnail, servings and times, ingredients and steps grouped by section. |
| [`MainActivity.kt`](app/src/main/kotlin/com/example/recipeimport/MainActivity.kt) | Receives links shared from other apps and picks the URL out of the shared text. |
| [`AndroidManifest.xml`](app/src/main/AndroidManifest.xml) | The intent filter that puts the app in the share sheet for links. |

## Learn more

- [Guide: import recipes from TikTok and Instagram links in an Android app](https://docs.clipparse.com/guides/import-recipes-from-tiktok-instagram-android) walks through this code step by step.
- The [API reference](https://docs.clipparse.com/) lists every recipe field and outcome.
- [Pricing](https://clipparse.com/pricing) covers what each import costs and the plans past the free tier.

Building for iOS? See the [SwiftUI example](https://github.com/clipparse/tiktok-instagram-recipe-import-swiftui).

## License

The code in this repository is under the [MIT License](LICENSE). The ClipParse SDK it depends on is under [its own license](https://clipparse.com/sdk-license).
