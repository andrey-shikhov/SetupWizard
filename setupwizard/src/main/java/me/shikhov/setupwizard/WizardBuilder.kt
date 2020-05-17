package me.shikhov.setupwizard

@DslMarker
annotation class WizardMarker

@WizardMarker
class WizardBuilder internal constructor(private val wizard: WizardImpl) {

    private var onDoneCallback: (() -> Unit)? = null

    private var onFailureCallback: (() -> Unit)? = null

    private var onStopCallback: (() -> Unit)? = null

    private var onDisposeCallback: (() -> Unit)? = null

    private val stages = mutableListOf<Stage>()

    fun step(id: String = "", block: () -> Unit) {
        stages += Stage(id, wizard::onStageStateChanged, { }, {
            block()
            done()
        }, { }, {_,_ ->})
    }

    fun stage(id: String = "", init: StageBuilder.() -> Unit) {
        stages += StageBuilder(id).apply(init).build(wizard::onStageStateChanged)
    }

    fun parallel(id: String = "", init: ParallelStageBuilder.() -> Unit) {
        stages += ParallelStageBuilder(wizard, id).apply(init).build()
    }

    fun wizardDone(onDone: () -> Unit) {
        onDoneCallback = onDone
    }

    fun wizardFailure(onFailure: () -> Unit) {
        onFailureCallback = onFailure
    }

    fun wizardStop(onStop: () -> Unit) {
        onStopCallback = onStop
    }

    fun wizardDispose(onDispose: () -> Unit) {
        onDisposeCallback = onDispose
    }

    internal fun build(): Wizard {
        wizard += stages

        onDoneCallback?.let {
            wizard.onDone += it
        }

        onFailureCallback?.let {
            wizard.onFailure += it
        }

        onDisposeCallback?.let {
            wizard.onDispose += it
        }

        onStopCallback?.let {
            wizard.onStop += it
        }

        return wizard
    }
}