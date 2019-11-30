package me.shikhov.setupwizard


class Stage(val id: String,
            private val setup: () -> Unit,
            private val run: Stage.() -> Unit,
            private val teardown: () -> Unit,
            private val onFailure: (Stage) -> Unit,
            private val onDone: (Stage) -> Unit) {

    internal fun setUp() = setup()

    internal fun tearDown() = teardown()

    internal fun start() = run()

    fun cancel() {
        onFailure(this)
    }

    fun done() {
        onDone(this)
    }
}