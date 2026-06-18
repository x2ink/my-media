package ink.x2.mymedia.core.common

sealed interface AppResult<out T>{
    data class Success<out T>(val data:T): AppResult<T>
    data class Error(val error: AppError): AppResult<Nothing>
}
inline fun <T> AppResult<T>.onSuccess(action: (T) -> Unit): AppResult<T> {
    if (this is AppResult.Success) {
        action(data)
    }
    return this
}
inline fun <T> AppResult<T>.onError(action: (AppError) -> Unit): AppResult<T> {
    if (this is AppResult.Error) {
        action(error)
    }
    return this
}