package me.shikhov.setupwizard


@WizardMarker
class StageBuilder internal constructor(private val stageId: String,
                                        private val stageReadableName: String,
                                        private val stageProgressObserver: (Stage, Int) -> Unit) {

    private var setup: () -> Unit = { }

    private var teardown: () -> Unit = { }

    private var run: Stage.() -> Unit = { done() }

    private var onError: (Stage, Throwable) -> Unit = { _, _ -> }

    fun setUp(action: () -> Unit) {
        setup = action
    }

    fun proceed(action: Stage.() -> Unit) {
        run = action
    }

    @Deprecated(message = "use [WizardBuilder.step] instead")
    fun simple(action: () -> Unit) {
        run = {
            action()
            done()
        }
    }

    fun tearDown(action: () -> Unit) {
        teardown = action
    }

    internal fun build(onStageStateChanged: (Stage, Stage.State) -> Unit): Stage {
        return Stage(stageId,
                     stageReadableName,
                     onStageStateChanged,
                     setup,
                     run,
                     teardown,
                     onError,
                     stageProgressObserver)
    }
}