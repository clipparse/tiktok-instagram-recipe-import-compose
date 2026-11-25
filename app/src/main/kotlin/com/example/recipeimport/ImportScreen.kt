package com.example.recipeimport

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.clipparse.sdk.model.Recipe

/** Paste a TikTok or Instagram link, import it, and show the recipe. */
@Composable
fun ImportScreen(viewModel: RecipeImportViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val importing = state == ImportState.Loading

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.imePadding(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 16.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("Recipe import", style = MaterialTheme.typography.headlineMedium)
            }
            item {
                OutlinedTextField(
                    value = viewModel.link,
                    onValueChange = { viewModel.link = it },
                    label = { Text("TikTok or Instagram link") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { viewModel.importRecipe() }),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { viewModel.importRecipe() },
                        enabled = viewModel.link.isNotBlank() && !importing,
                    ) {
                        Text("Import recipe")
                    }
                    if (importing) {
                        Spacer(Modifier.width(16.dp))
                        CircularProgressIndicator(Modifier.size(24.dp))
                    }
                }
            }
            item {
                Text(
                    "You can also share a post to this app from TikTok or Instagram.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            when (val current = state) {
                is ImportState.Failed -> item {
                    Text(current.message, color = MaterialTheme.colorScheme.error)
                }
                is ImportState.Imported -> recipe(current.recipe)
                ImportState.Idle, ImportState.Loading -> Unit
            }
        }
    }
}

/** The imported recipe: thumbnail, servings and times, ingredients and steps. */
private fun LazyListScope.recipe(recipe: Recipe) {
    item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            recipe.thumbnailUrl?.let { thumbnail ->
                AsyncImage(
                    model = thumbnail,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )
            }
            Text(recipe.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            summaryLine(recipe)?.let {
                Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val context = LocalContext.current
            TextButton(
                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(recipe.sourceUrl))) },
                contentPadding = PaddingValues(0.dp),
            ) {
                Text("Open the original post")
            }
        }
    }

    // Ingredients and steps come in the post's order; a section ("For the
    // sauce") starts a new group.
    recipe.ingredients.groupedBySection { it.section }.forEach { (section, ingredients) ->
        item { SectionTitle(section ?: "Ingredients") }
        itemsIndexed(ingredients) { index, ingredient ->
            if (index > 0) HorizontalDivider()
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(ingredient.name)
                    ingredient.prep?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                amountText(ingredient)?.let {
                    Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    recipe.steps.groupedBySection { it.section }.forEach { (section, steps) ->
        item { SectionTitle(section ?: "Steps") }
        itemsIndexed(steps) { index, step ->
            Row {
                Text("${index + 1}.", fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
                Column {
                    Text(step.text)
                    step.durationMinutes?.let {
                        Text("$it min", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp),
    )
}

/** Servings and times, such as "4 servings · 10 min prep · 25 min cook". */
private fun summaryLine(recipe: Recipe): String? = listOfNotNull(
    recipe.servings?.let { "${java.text.DecimalFormat("0.##").format(it)} servings" },
    recipe.prepTimeMinutes?.let { "$it min prep" },
    recipe.cookTimeMinutes?.let { "$it min cook" },
).takeIf { it.isNotEmpty() }?.joinToString(" · ")

/** Splits items into runs that share the same section, keeping their order. */
private fun <T> List<T>.groupedBySection(section: (T) -> String?): List<Pair<String?, List<T>>> {
    val groups = mutableListOf<Pair<String?, MutableList<T>>>()
    for (item in this) {
        val title = section(item)
        if (groups.isNotEmpty() && groups.last().first == title) {
            groups.last().second.add(item)
        } else {
            groups.add(title to mutableListOf(item))
        }
    }
    return groups
}
