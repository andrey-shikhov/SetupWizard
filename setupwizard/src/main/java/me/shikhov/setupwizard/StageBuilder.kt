package me.shikhov.setupwizard

class StageBuilder internal constructor(private val wizard: WizardImpl) {

    var id: String = ""

    private var setup: () -> Unit = { }

    private var run: Stage.() -> Unit = { done() }

    private var teardown: () -> Unit = { }

    fun setUp(action: () -> Unit) {
        setup = action
    }

    fun procede(action: Stage.() -> Unit) {
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

    fun build(): Stage {
        return Stage(
            id,
            setup,
            run,
            teardown,
            wizard::onStageFailed,
            wizard::onStageDone)
    }
}