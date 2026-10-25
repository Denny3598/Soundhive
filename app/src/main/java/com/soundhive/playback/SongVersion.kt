package com.soundhive.playback

import com.soundhive.data.YtMusicRepository
import com.soundhive.data.model.SearchFilter
import com.soundhive.data.model.SearchResult
import com.soundhive.data.sources.TrackMatcher
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.ConcurrentHashMap

/**
 * Album tracks often point at a music video. Its audio is a different mix from
 * the song, which is why it sounds wrong. This finds the song's own audio-only
 * upload: same title and artist, and the same runtime to within a few seconds.
 *
 * When nothing matches, the original id is kept. Results are remembered for the
 * session, so each track is searched at most once.
 */
object SongVersion {

    private const val LOOKUP_TIMEOUT_MS = 6_000L
    private const val DURATION_TOLERANCE_SEC = 3

    private val chosen = ConcurrentHashMap<String, String>()

    /** The id to play for [videoId]: its audio-only counterpart, or [videoId] itself. */
    suspend fun audioVersionOf(videoId: String, target: TrackMatcher.Target): String {
        chosen[videoId]?.let { return it }
        // Without a runtime there is nothing to check a counterpart against.
        if (target.durationSec == null) return videoId
        // A timeout keeps the original and is not remembered, so a later play can try again.
        val found = withTimeoutOrNull(LOOKUP_TIMEOUT_MS) { lookUp(target) } ?: return videoId
        val id = found ?: videoId
        chosen[videoId] = id
        return id
    }

    private suspend fun lookUp(target: TrackMatcher.Target): String? {
        val songs = TrackMatcher.queries(target)
            .flatMap { query ->
                YtMusicRepository.search(query, SearchFilter.SONGS).getOrNull()
                    .orEmpty()
                    .filterIsInstance<SearchResult.Track>()
                    .map { it.song }
            }
            .distinctBy { it.videoId }
        val sameRuntime = songs.filter { TrackMatcher.withinSeconds(it, target, DURATION_TOLERANCE_SEC) }
        return TrackMatcher.best(sameRuntime, target)?.videoId
    }
}
