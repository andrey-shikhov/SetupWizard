package me.shikhov.setupwizard


class Stage(val id: String,
            private val wizard: Wizard,
            private val setup: () -> Unit,
            private val run: Stage.() -> Unit,
            private val teardown: () -> Unit) {

    enum class State {
        CREATED,
        PREPARED,
        STARTED,
        CANCELED,
        DONE
    }

    var state: State = State.CREATED
        private set(value) {
            field = value
            wizard.onStageChanged(this)
        }

    fun done() {
        if(state == State.STARTED) {
            state = State.DONE
            wizard.onStageDone(this)
        }
    }

    fun cancel() {
        if(state == State.STARTED) {
            state = State.CANCELED
            wizard.onStageFailed(this)
        }
    }

    internal fun setUp() {
        setup()
        state = State.PREPARED
    }

    internal fun start() {
        state = State.STARTED
        run()
    }

    internal fun tearDown() = teardown()
}