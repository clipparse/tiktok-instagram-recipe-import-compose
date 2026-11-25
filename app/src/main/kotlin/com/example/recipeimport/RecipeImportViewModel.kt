package com.example.recipeimport

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.clipparse.sdk.ClipParseClient
import com.clipparse.sdk.ClipParseErrorCode
import com.clipparse.sdk.ClipParseException
import com.clipparse.sdk.model.ExtractionResult
import com.clipparse.sdk.model.Ingredient
import com.clipparse.sdk.model.IngredientUnit
import com.clipparse.sdk.model.NoRecipeReason
import com.clipparse.sdk.model.Recipe
import java.text.DecimalFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ImportState {
    data object Idle : ImportState
    data object Loading : ImportState
    data class Imported(val recipe: Recipe) : ImportState
    data class Failed(val message: String) : ImportState
}

/** Imports a recipe from a TikTok or Instagram link and exposes the state to the UI. */
class RecipeImportViewModel(application: Application) : AndroidViewModel(application) {
    private val client = ClipParseClient(application, apiKey = BuildConfig.CLIPPARSE_API_KEY)

    private val _state = MutableStateFlow<ImportState>(ImportState.Idle)
    val state: StateFlow<ImportState> = _state.asStateFlow()

    /** The link in the text field. A shared link fills it too. */
    var link by mutableStateOf("")

    fun importRecipe(link: String = this.link) {
        this.link = link
        _state.value = ImportState.Loading
        viewModelScope.launch {
            _state.value = try {
                when (val result = client.extract(link.trim())) {
                    is ExtractionResult.Success -> ImportState.Imported(result.recipe)
                    is ExtractionResult.NoRecipe ->
                        if (result.reason == NoRecipeReason.MultipleRecipesDetected) {
                            ImportState.Failed("This post has more than one recipe.")
                        } else {
                            ImportState.Failed("We couldn't find a recipe in this post.")
                        }
                }
            } catch (e: ClipParseException) {
                ImportState.Failed(importErrorMessage(e))
            }
        }
    }
}

/** A message for the user, for each way an import can fail. */
fun importErrorMessage(e: ClipParseException): String = when (e.code) {
    ClipParseErrorCode.PlatformUnsupported -> "Only TikTok and Instagram links are supported."
    ClipParseErrorCode.InvalidRequest -> "That doesn't look like a link."
    ClipParseErrorCode.PostUnavailable -> "This post was deleted, is private, or doesn't exist."
    ClipParseErrorCode.Unauthorized,
    ClipParseErrorCode.InvalidApiKey,
    ClipParseErrorCode.ApiKeyRevoked -> {
        // A problem with the API key: yours to fix, not the user's.
        Log.w("RecipeImport", "ClipParse API key error", e)
        "Recipe import isn't available right now."
    }
    ClipParseErrorCode.RateLimited,
    ClipParseErrorCode.QuotaExceeded -> "Too many imports right now. Try again later."
    ClipParseErrorCode.Network -> "Check your connection and try again."
    // New error codes can be added: the SDK passes on a code it doesn't know yet as is.
    else -> "Something went wrong. Please try again."
}

private val number = DecimalFormat("0.##")

/** The amount to show for an ingredient, such as "2–3 tbsp" or "3". */
fun amountText(ingredient: Ingredient): String? {
    val quantity = ingredient.quantity ?: return null
    val low = ingredient.quantityMin
    val high = ingredient.quantityMax
    val amount = if (low != null && high != null) {
        "${number.format(low)}–${number.format(high)}"
    } else {
        number.format(quantity)
    }
    val unit = ingredient.unit
    return if (unit == null || unit == IngredientUnit.Each) amount else "$amount ${unit.raw}"
}
