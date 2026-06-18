package ink.x2.mymedia.domain.model
enum class ImportStatus {
    SUCCESS,
    FAILURE,
    IN_PROGRESS
}
sealed interface ImportProgress {
    data class Loading(
        val current: Int,
        val total: Int,
        val currentItem: LocalMediaItem,
        val status: ImportStatus
    ) : ImportProgress
    data class Success(
        val successCount: Int,
        val failedItems: List<LocalMediaItem>
    ) : ImportProgress
    data object Failure: ImportProgress
}