package com.soundhive.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.soundhive.data.model.HomeShelf
import com.soundhive.data.model.LibraryPage
import com.soundhive.data.model.ShelfItem
import com.soundhive.data.model.UiState
import com.soundhive.data.settings.AppSettings
import com.soundhive.ui.components.MessageState
import com.soundhive.ui.components.PAGE_GUTTER
import com.soundhive.ui.components.PullToRefresh
import com.soundhive.ui.components.SHELF_CARD_WIDTH
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.soundhive.ui.components.librarySkeleton
import com.soundhive.ui.components.soundhiveSpecularBorder
import com.soundhive.ui.icons.SoundhiveIcons
import com.soundhive.ui.theme.LocalSoundhiveColors
import com.soundhive.ui.theme.Manrope
import com.soundhive.ui.theme.Sora

/**
 * The signed-in library: the saved collections, as shelves of cards.
 *
 * Deliberately only the collections. This page used to end with two runs of
 * track rows — "Liked Music" and "Songs" — which are two overlapping answers
 * to the same question and read as one list that couldn't make up its mind: a
 * track that stopped being liked didn't leave the page, it moved down it, into
 * a section most people had taken for more of the same. Liked Music is a
 * playlist, and it is reached the way every other playlist here is, by opening
 * its card.
 *
 * Every shelf has a "Lihat semua" action that opens the whole shelf as a grid.
 * There are no filter chips; the shelves themselves are the navigation.
 *
 * The liked list is still fetched — it is what the rest of the app reads a
 * track's rating off (see MainViewModel's `likeStatuses`); it just isn't a
 * second place to browse it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    signedIn: Boolean,
    state: UiState<LibraryPage>,
    listState: LazyListState,
    onShelfItemClick: (ShelfItem) -> Unit,
    onShelfItemLongPress: (ShelfItem) -> Unit,
    onNewPlaylist: () -> Unit,
    onSignIn: () -> Unit,
    onRetry: () -> Unit,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    pullState: PullToRefreshState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
) {
    val soundhiveColors = LocalSoundhiveColors.current
    val pinnedList by AppSettings.pinnedPlaylists.collectAsStateWithLifecycle()

    val pinnedSet = remember(pinnedList) {
        val set = HashSet<String>(pinnedList.size * 3)
        for (id in pinnedList) {
            set.add(id)
            set.add(id.removePrefix("VL"))
            set.add("VL$id")
        }
        set
    }
    val isPinned: (ShelfItem) -> Boolean = remember(pinnedSet) {
        { item -> item.browseId != null && item.browseId in pinnedSet }
    }

    // Judul rak yang sedang dibuka lewat "Lihat semua". Null berarti halaman utama pustaka.
    var openedShelf by rememberSaveable { mutableStateOf<String?>(null) }
    val openedData: HomeShelf? = when (val title = openedShelf) {
        null -> null
        ON_DEVICE -> onDeviceShelf()
        PLAYLISTS -> (state as? UiState.Success)?.data?.shelves?.firstOrNull { it.title == PLAYLISTS }
            ?: HomeShelf(PLAYLISTS, emptyList())
        else -> (state as? UiState.Success)?.data?.shelves?.firstOrNull { it.title == title }
    }
    BackHandler(enabled = openedData != null) { openedShelf = null }

    if (openedData != null) {
        LibraryGridPage(
            shelf = openedData,
            isItemPinned = isPinned,
            pinnedList = pinnedList,
            onBack = { openedShelf = null },
            onItemClick = onShelfItemClick,
            onItemLongPress = onShelfItemLongPress,
            onNewPlaylist = if (openedData.title == PLAYLISTS) onNewPlaylist else null,
            contentPadding = contentPadding,
            modifier = modifier,
        )
        return
    }

    PullToRefresh(
        refreshing = refreshing,
        onRefresh = onRefresh,
        state = pullState,
        modifier = modifier.background(soundhiveColors.surfaceOled),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
        ) {
            item(key = "library_header", contentType = "library_header") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 18.dp)
                        .padding(horizontal = PAGE_GUTTER),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column {
                            Text(
                                text = "Library",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontFamily = Sora,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 26.sp,
                                    letterSpacing = (-0.6).sp,
                                ),
                                color = soundhiveColors.textPrimary,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Your personal collections, playlists & downloads.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = Manrope,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                ),
                                color = soundhiveColors.textSecondary,
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(soundhiveColors.primary.copy(alpha = 0.14f))
                                .soundhiveSpecularBorder(RoundedCornerShape(12.dp), 0.75.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LibraryMusic,
                                contentDescription = null,
                                tint = soundhiveColors.primary,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }
                }
            }
            item(key = "shelf:$ON_DEVICE", contentType = "on_device_shelf") {
                Shelf(
                    shelf = onDeviceShelf(),
                    onItemClick = onShelfItemClick,
                    onSeeAll = { openedShelf = ON_DEVICE },
                )
            }
            if (!signedIn) {
                item(key = "library_signin", contentType = "message_state") {
                    MessageState(
                        message = "Sign in to your Google account to see your YouTube Music " +
                            "liked songs, playlists and history.",
                        actionLabel = "Sign in",
                        onAction = onSignIn,
                    )
                }
                return@LazyColumn
            }
            when (state) {
                is UiState.Loading -> librarySkeleton()
                is UiState.Error -> item(key = "library_error", contentType = "message_state") {
                    MessageState(state.message, actionLabel = "Retry", onAction = onRetry)
                }
                is UiState.Success -> {
                    // A fresh account has no Playlists shelf at all, and that
                    // is exactly the account most in need of the button that
                    // makes one — so the row is drawn either way, empty but
                    // for the tile that creates the first playlist.
                    val shelves = state.data.shelves
                    if (shelves.none { it.title == PLAYLISTS }) {
                        item(key = "shelf:$PLAYLISTS", contentType = "playlist_shelf") {
                            PlaylistShelf(
                                shelf = HomeShelf(PLAYLISTS, emptyList()),
                                onItemClick = onShelfItemClick,
                                onItemLongPress = onShelfItemLongPress,
                                onNewPlaylist = onNewPlaylist,
                                isItemPinned = isPinned,
                                pinnedList = pinnedList,
                                onSeeAll = { openedShelf = PLAYLISTS },
                            )
                        }
                    }
                    shelves.forEach { shelf ->
                        item(
                            key = "shelf:${shelf.title}",
                            contentType = if (shelf.title == PLAYLISTS) "playlist_shelf" else "regular_shelf",
                        ) {
                            if (shelf.title == PLAYLISTS) {
                                PlaylistShelf(
                                    shelf = shelf,
                                    onItemClick = onShelfItemClick,
                                    onItemLongPress = onShelfItemLongPress,
                                    onNewPlaylist = onNewPlaylist,
                                    isItemPinned = isPinned,
                                    pinnedList = pinnedList,
                                    onSeeAll = { openedShelf = PLAYLISTS },
                                )
                            } else {
                                Shelf(
                                    shelf = shelf,
                                    onItemClick = onShelfItemClick,
                                    onItemLongPress = onShelfItemLongPress,
                                    isItemPinned = isPinned,
                                    onSeeAll = { openedShelf = shelf.title },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Rak "On Device": Downloads, Local Music, History, dan Replay. */
private fun onDeviceShelf(): HomeShelf = HomeShelf(
    title = ON_DEVICE,
    items = listOf(
        ShelfItem(
            title = "Downloads",
            subtitle = "Downloaded songs",
            thumbnailUrl = null,
            videoId = null,
            browseId = "local:downloads",
        ),
        ShelfItem(
            title = "Local Music",
            subtitle = "Audio files on device",
            thumbnailUrl = null,
            videoId = null,
            browseId = "local:all",
        ),
        ShelfItem(
            title = "History",
            subtitle = "Recently played",
            thumbnailUrl = null,
            videoId = null,
            browseId = "soundhive:history",
        ),
        ShelfItem(
            title = "Replay",
            subtitle = "Your listening recap",
            thumbnailUrl = null,
            videoId = null,
            browseId = "soundhive:replay",
        ),
    ),
)

/** Urutan item dengan yang dipin di depan, sesuai urutan pin. */
private fun pinnedFirst(
    items: List<ShelfItem>,
    pinnedList: List<String>,
    isItemPinned: (ShelfItem) -> Boolean,
): List<ShelfItem> {
    val (pinned, unpinned) = items.partition { isItemPinned(it) }
    val sortedPinned = pinned.sortedBy { item ->
        val id = item.browseId.orEmpty()
        val index1 = pinnedList.indexOf(id)
        val index2 = pinnedList.indexOf(id.removePrefix("VL"))
        val index3 = pinnedList.indexOf("VL$id")
        listOf(index1, index2, index3).filter { it >= 0 }.minOrNull() ?: Int.MAX_VALUE
    }
    return sortedPinned + unpinned
}

/**
 * The one shelf on this page that can be written to: it leads with the tile
 * that creates a playlist, and holding a card opens the contextual actions menu.
 * Pinned playlists are reordered to the front.
 */
@Composable
private fun PlaylistShelf(
    shelf: HomeShelf,
    onItemClick: (ShelfItem) -> Unit,
    onItemLongPress: (ShelfItem) -> Unit,
    onNewPlaylist: () -> Unit,
    isItemPinned: (ShelfItem) -> Boolean,
    pinnedList: List<String>,
    onSeeAll: () -> Unit,
) {
    val reorderedShelf = remember(shelf, pinnedList) {
        shelf.copy(items = pinnedFirst(shelf.items, pinnedList, isItemPinned))
    }

    Shelf(
        shelf = reorderedShelf,
        onItemClick = onItemClick,
        onItemLongPress = onItemLongPress,
        isItemPinned = isItemPinned,
        onSeeAll = onSeeAll,
        leadingCard = {
            NewShelfCard(
                icon = SoundhiveIcons.Plus,
                label = "New playlist",
                subtitle = "Saved to YouTube Music",
                onClick = onNewPlaylist,
            )
        },
    )
}

/** Halaman "Lihat semua": semua item satu rak dalam bentuk grid, dengan tombol kembali. */
@Composable
private fun LibraryGridPage(
    shelf: HomeShelf,
    isItemPinned: (ShelfItem) -> Boolean,
    pinnedList: List<String>,
    onBack: () -> Unit,
    onItemClick: (ShelfItem) -> Unit,
    onItemLongPress: (ShelfItem) -> Unit,
    onNewPlaylist: (() -> Unit)?,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val soundhiveColors = LocalSoundhiveColors.current
    val gridItems = remember(shelf, pinnedList) {
        if (shelf.title == PLAYLISTS) pinnedFirst(shelf.items, pinnedList, isItemPinned) else shelf.items
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(soundhiveColors.surfaceOled)
            .padding(top = contentPadding.calculateTopPadding()),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PAGE_GUTTER, vertical = 8.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Kembali",
                    tint = soundhiveColors.textPrimary,
                )
            }
            Spacer(Modifier.size(6.dp))
            Text(
                text = shelf.title,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = Sora,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    letterSpacing = (-0.5).sp,
                ),
                color = soundhiveColors.textPrimary,
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(SEE_ALL_CARD_WIDTH),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                start = PAGE_GUTTER,
                end = PAGE_GUTTER,
                bottom = contentPadding.calculateBottomPadding() + 16.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            if (onNewPlaylist != null) {
                item(key = "new_playlist", contentType = "leading_card") {
                    NewShelfCard(
                        icon = SoundhiveIcons.Plus,
                        label = "New playlist",
                        subtitle = "Saved to YouTube Music",
                        onClick = onNewPlaylist,
                        cardWidth = SEE_ALL_CARD_WIDTH,
                    )
                }
            }
            items(
                items = gridItems,
                key = { it.videoId ?: it.browseId ?: "${shelf.title}_${it.title}" },
                contentType = { "shelf_card" },
            ) { item ->
                SoundhiveShelfCard(
                    item = item,
                    onClick = { onItemClick(item) },
                    onLongPress = { onItemLongPress(item) },
                    isPinned = isItemPinned(item),
                    cardWidth = SEE_ALL_CARD_WIDTH,
                    titleMaxLines = 3,
                    titleStyle = SeeAllTitleStyle,
                )
            }
        }
    }
}

/** Lebar kartu di halaman "Lihat semua": lebih besar dari kartu rak di Beranda. */
private val SEE_ALL_CARD_WIDTH = 168.dp

private val SeeAllTitleStyle = TextStyle(
    fontFamily = Manrope,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 20.sp,
    letterSpacing = (-0.1).sp,
)

/** Judul rak yang dipakai sebagai kunci dan untuk logika khusus. */
private const val PLAYLISTS = "Playlists"
private const val ON_DEVICE = "On Device"
