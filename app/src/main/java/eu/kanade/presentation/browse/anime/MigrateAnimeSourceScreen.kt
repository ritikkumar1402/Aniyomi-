package eu.kanade.presentation.browse.anime

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import eu.kanade.domain.source.service.SetMigrateSorting
import eu.kanade.presentation.browse.anime.components.AnimeSourceIcon
import eu.kanade.presentation.browse.anime.components.BaseAnimeSourceItem
import eu.kanade.presentation.components.Badge
import eu.kanade.presentation.components.BadgeGroup
import eu.kanade.presentation.components.EmptyScreen
import eu.kanade.presentation.components.LoadingScreen
import eu.kanade.presentation.components.ScrollbarLazyColumn
import eu.kanade.presentation.components.Scroller.STICKY_HEADER_KEY_PREFIX
import eu.kanade.presentation.theme.header
import eu.kanade.presentation.util.padding
import eu.kanade.presentation.util.plus
import eu.kanade.presentation.util.secondaryItemAlpha
import eu.kanade.presentation.util.topSmallPaddingValues
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.browse.anime.migration.sources.MigrateAnimeSourceState
import eu.kanade.tachiyomi.util.system.copyToClipboard
import tachiyomi.domain.source.anime.model.AnimeSource

@Composable
fun MigrateAnimeSourceScreen(
    state: MigrateAnimeSourceState,
    contentPadding: PaddingValues,
    onClickItem: (AnimeSource) -> Unit,
    onToggleSortingDirection: () -> Unit,
    onToggleSortingMode: () -> Unit,
) {
    val context = LocalContext.current
    when {
        state.isLoading -> LoadingScreen(modifier = Modifier.padding(contentPadding))
        state.isEmpty -> EmptyScreen(
            textResource = R.string.information_empty_library,
            modifier = Modifier.padding(contentPadding),
        )
        else ->
            MigrateAnimeSourceList(
                list = state.items,
                contentPadding = contentPadding,
                onClickItem = onClickItem,
                onLongClickItem = { source ->
                    val sourceId = source.id.toString()
                    context.copyToClipboard(sourceId, sourceId)
                },
                sortingMode = state.sortingMode,
                onToggleSortingMode = onToggleSortingMode,
                sortingDirection = state.sortingDirection,
                onToggleSortingDirection = onToggleSortingDirection,
            )
    }
}

@Composable
private fun MigrateAnimeSourceList(
    list: List<Pair<AnimeSource, Long>>,
    contentPadding: PaddingValues,
    onClickItem: (AnimeSource) -> Unit,
    onLongClickItem: (AnimeSource) -> Unit,
    sortingMode: SetMigrateSorting.Mode,
    onToggleSortingMode: () -> Unit,
    sortingDirection: SetMigrateSorting.Direction,
    onToggleSortingDirection: () -> Unit,
) {
    ScrollbarLazyColumn(
        contentPadding = contentPadding + topSmallPaddingValues,
    ) {
        stickyHeader(key = STICKY_HEADER_KEY_PREFIX) {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(start = MaterialTheme.padding.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.migration_selection_prompt),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.header,
                )

                IconButton(onClick = onToggleSortingMode) {
                    when (sortingMode) {
                        SetMigrateSorting.Mode.ALPHABETICAL -> Icon(Icons.Outlined.SortByAlpha, contentDescription = stringResource(R.string.action_sort_alpha))
                        SetMigrateSorting.Mode.TOTAL -> Icon(Icons.Outlined.Numbers, contentDescription = stringResource(R.string.action_sort_count))
                    }
                }
                IconButton(onClick = onToggleSortingDirection) {
                    when (sortingDirection) {
                        SetMigrateSorting.Direction.ASCENDING -> Icon(Icons.Outlined.ArrowUpward, contentDescription = stringResource(R.string.action_asc))
                        SetMigrateSorting.Direction.DESCENDING -> Icon(Icons.Outlined.ArrowDownward, contentDescription = stringResource(R.string.action_desc))
                    }
                }
            }
        }

        items(
            items = list,
            key = { (source, _) -> "migrate-${source.id}" },
        ) { (source, count) ->
            MigrateAnimeSourceItem(
                modifier = Modifier.animateItemPlacement(),
                source = source,
                count = count,
                onClickItem = { onClickItem(source) },
                onLongClickItem = { onLongClickItem(source) },
            )
        }
    }
}

@Composable
private fun MigrateAnimeSourceItem(
    modifier: Modifier = Modifier,
    source: AnimeSource,
    count: Long,
    onClickItem: () -> Unit,
    onLongClickItem: () -> Unit,
) {
    BaseAnimeSourceItem(
        modifier = modifier,
        source = source,
        showLanguageInContent = source.lang != "",
        onClickItem = onClickItem,
        onLongClickItem = onLongClickItem,
        icon = { AnimeSourceIcon(source = source) },
        action = {
            BadgeGroup {
                Badge(text = "$count")
            }
        },
        content = { _, sourceLangString ->
            Column(
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.padding.medium)
                    .weight(1f),
            ) {
                Text(
                    text = source.name.ifBlank { source.id.toString() },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (sourceLangString != null) {
                        Text(
                            modifier = Modifier.secondaryItemAlpha(),
                            text = sourceLangString,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    if (source.isStub) {
                        Text(
                            modifier = Modifier.secondaryItemAlpha(),
                            text = stringResource(R.string.not_installed),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        },
    )
}
