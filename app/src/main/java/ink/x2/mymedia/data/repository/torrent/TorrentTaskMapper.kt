package ink.x2.mymedia.data.repository.torrent

import ink.x2.mymedia.data.local.db.entity.torrent.TorrentTaskEntity
import ink.x2.mymedia.domain.model.torrent.TorrentDownloadStatus
import ink.x2.mymedia.domain.model.torrent.TorrentDownloadTask

fun TorrentTaskEntity.toDomain(): TorrentDownloadTask {
    return TorrentDownloadTask(
        id = id,
        magnetUri = magnetUri,
        infoHash = infoHash,
        title = title,
        selectedFileIndexes = selectedFileIndexes.toIntList(),
        selectedFileNames = selectedFileNames.toStringList(),
        status = runCatching { TorrentDownloadStatus.valueOf(status) }
            .getOrDefault(TorrentDownloadStatus.FAILED),
        progress = progress,
        downloadedBytes = downloadedBytes,
        totalBytes = totalBytes,
        downloadSpeedBytes = downloadSpeedBytes,
        peersCount = peersCount,
        errorMessage = errorMessage,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun List<Int>.toPersistedIntList(): String = joinToString(separator = ",")

fun List<String>.toPersistedStringList(): String = joinToString(separator = "\n")

fun String.toIntList(): List<Int> {
    return split(",")
        .mapNotNull { it.trim().toIntOrNull() }
}

fun String.toStringList(): List<String> {
    return split("\n")
        .map { it.trim() }
        .filter { it.isNotBlank() }
}
