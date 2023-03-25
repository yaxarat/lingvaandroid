package dev.atajan.lingva_android.common.redux

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

typealias MiddleWare<S, I> = (S, I) -> Unit

@OptIn(ObsoleteCoroutinesApi::class)
abstract class MVIViewModel<State, Intention, SideEffect>(
    scope: CoroutineScope,
    initialState: State
) : ViewModel() {

    private val _states = MutableStateFlow(initialState)
    private val _sideEffects = MutableSharedFlow<SideEffect>(Channel.UNLIMITED)
    private val _middleWareList = mutableListOf<MiddleWare<State, Intention>>()

    val states: StateFlow<State> = _states.asStateFlow()
    val sideEffects: SharedFlow<SideEffect> = _sideEffects.asSharedFlow()
    val middleWareList: List<MiddleWare<State, Intention>> = _middleWareList.toList()

    private val actor = scope.actor(capacity = Channel.UNLIMITED) {
        channel.consumeEach { intention ->
            _states.value = reduce(
                currentState = _states.value,
                intention = intention,
                middleWares = _middleWareList
            )
        }
    }

    abstract fun reduce(
        currentState: State,
        intention: Intention,
        middleWares: List<MiddleWare<State, Intention>> = emptyList()
    ): State

    protected fun sideEffect(sideEffect: SideEffect) {
        _sideEffects.tryEmit(sideEffect)
    }

    fun send(intention: Intention) {
        actor.trySend(intention)
    }

    fun provideMiddleWares(vararg middleWares: MiddleWare<State, Intention>) {
        _middleWareList.addAll(middleWares)
    }
}