package com.example.recipeimport

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme

class MainActivity : ComponentActivity() {
    private val viewModel: RecipeImportViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // On a configuration change the ViewModel already holds the import.
        if (savedInstanceState == null) importSharedLink(intent)
        setContent {
            MaterialTheme {
                ImportScreen(viewModel)
            }
        }
    }

    // A share while the app is open arrives here (the activity is singleTop).
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        importSharedLink(intent)
    }

    private fun importSharedLink(intent: Intent) {
        sharedLink(intent)?.let { viewModel.importRecipe(it) }
    }
}

/** The link in a share from another app. Apps share it as text, sometimes with other words around it. */
fun sharedLink(intent: Intent): String? {
    val sharedText = intent.takeIf { it.action == Intent.ACTION_SEND }
        ?.getStringExtra(Intent.EXTRA_TEXT)
        ?: return null
    val matcher = Patterns.WEB_URL.matcher(sharedText)
    return if (matcher.find()) matcher.group() else null
}
