package me.shikhov.setupwizard

import androidx.annotation.IntRange


class Stage(val id: String,
            val readableName: String,
            private val observer: (Stage, State) -> Unit,
            private val setup: () -> Unit,
            private val run: Stage.() -> Unit,
            private val teardown: () -> Unit,
            private val onError:(Stage, Throwable) -> Unit,
            private val onProgressChanged: (Stage, progress: Int) -> Unit) {

    var progressLevel: Int = 0
        private set(value) {
            if(field != value) {
                check(value in 0..10000) { value }
                field = value
                onProgressChanged(this, field)
            }
        }


    fun done() {
        check(state == State.RUNNING) { this }
        reportProgress(10000)
        state = State.DONE
    }

    fun cancel() {
        check(state == State.RUNNING) { this }
        state = State.CANCELED
    }

    fun error(throwable: Throwable) {
        check(state == State.RUNNING)
        state = State.FAILED
        onError(this, throwable)
    }

    fun reportProgress(@IntRange(from = 0, to = 10_000) level: Int) {
        check(state == State.RUNNING) { this }
        progressLevel = level
    }

    /**
     * CREATED -> RUNNING
     * RUNNING -> DONE | CANCELED | FAILED
     */
    enum class State {
        CREATED,
        RUNNING,
        CANCELED,
        FAILED,
        DONE;

        internal val isFinished get() = ordinal > RUNNING.ordinal
    }

    var state: State = State.CREATED
        private set(value) {
            field = value
            observer(this, field)
        }

    internal fun start() {
        state = State.RUNNING
        reportProgress(0)

        setup()

        runCatching(run).fold({
            // task actually can be running in background, so this callback is unused.
        }, ::error)
    }

    internal fun tearDown() = teardown()

    override fun toString(): String {
        return "Stage#$id($state)"
    }
}