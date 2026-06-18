package ink.x2.mymedia.core.common

sealed interface AppError {
    data object SecurityException : AppError
    data class Unknown(val throwable: Throwable?) : AppError
}