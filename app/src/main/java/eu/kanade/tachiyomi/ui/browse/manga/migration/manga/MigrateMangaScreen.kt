package eu.kanade.tachiyomi.ui.browse.manga.migration.manga

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.browse.manga.MigrateMangaScreen
import eu.kanade.presentation.util.Screen
import eu.kanade.tachiyomi.ui.browse.manga.migration.search.MigrateMangaSearchScreen
import eu.kanade.tachiyomi.ui.entries.manga.MangaScreen
import kotlinx.coroutines.flow.collectLatest
import tachiyomi.core.i18n.stringResource
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.screens.LoadingScreen

data class MigrateMangaScreen(
    private val sourceId: Long,
) : Screen() {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { MigrateMangaScreenModel(sourceId) }

        val state by screenModel.state.collectAsState()

        if (state.isLoading) {
            LoadingScreen()
            return
        }

        MigrateMangaScreen(
            navigateUp = navigator::pop,
            title = state.source!!.name,
            state = state,
            onClickItem = { navigator.push(MigrateMangaSearchScreen(it.id)) },
            onClickCover = { navigator.push(MangaScreen(it.id)) },
        )

        LaunchedEffect(Unit) {
            screenModel.events.collectLatest { event ->
                when (event) {
                    MigrationMangaEvent.FailedFetchingFavorites -> {
                        context.stringResource(MR.strings.internal_error)
                    }
                }
            }
        }
    }
}
