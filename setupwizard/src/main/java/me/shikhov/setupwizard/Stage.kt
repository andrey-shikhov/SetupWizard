package me.shikhov.setupwizard


class Stage(val id: String,
            private val observer: (Stage, State) -> Unit,
            private val setup: () -> Unit,
            private val run: Stage.() -> Unit,
            private val teardown: () -> Unit,
            private val onError:(Stage, Throwable) -> Unit) {

    fun done() {
        check(state == State.RUNNING)
        state = State.DONE
    }

    fun cancel() {
        check(state == State.RUNNING)
        state = State.CANCELED
    }

    fun error(throwable: Throwable) {
        check(state == State.RUNNING)
        state = State.FAILED
        onError(this, throwable)
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