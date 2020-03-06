package me.shikhov.setupwizard


@WizardMarker
class StageBuilder internal constructor(private val stageId: String) {

    private var setup: () -> Unit = { }

    private var run: Stage.() -> Unit = { done() }

    private var teardown: () -> Unit = { }

    fun setUp(action: () -> Unit) {
        setup = action
    }

    fun proceed(action: Stage.() -> Unit) {
        run = action
    }

    fun simple(action: () -> Unit) {
        run = {
            action()
            done()
        }
    }

    fun tearDown(action: () -> Unit) {
        teardown = action
    }

    internal fun build(wizard: WizardImpl): Stage {
        return Stage(
            stageId,
            wizard,
            setup,
            run,
            teardown)
    }
}