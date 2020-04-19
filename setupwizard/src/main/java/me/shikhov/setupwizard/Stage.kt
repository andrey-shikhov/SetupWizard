package me.shikhov.setupwizard


class Stage(val id: String,
            private val observer: (Stage, State) -> Unit,
            private val setup: () -> Unit,
            private val run: Stage.() -> Unit,
            private val teardown: () -> Unit,
            private val onError:(Stage, Throwable) -> Unit) {

    fun done() {
        check(state == State.STARTED)
        state = State.DONE
    }

    fun cancel() {
        check(state == State.STARTED)
        state = State.CANCELED
    }

    fun error(throwable: Throwable) {
        check(state == State.STARTED)
        state = State.FAILED
        onError(this, throwable)
    }

    /**
     * CREATED -> STARTED
     * STARTED -> DONE | CANCELED | FAILED
     *
     */
    enum class State {
        CREATED,
        STARTED,
        CANCELED,
        FAILED,
        DONE;

        val isFinished get() = ordinal > STARTED.ordinal
    }

    private var state: State = State.CREATED
        set(value) {
            field = value
            observer(this, field)
        }

    internal fun start() {
        state = State.STARTED
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