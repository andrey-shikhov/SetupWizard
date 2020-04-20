package me.shikhov.setupwizard

@DslMarker
annotation class WizardMarker

@WizardMarker
class WizardBuilder internal constructor(private val wizard: WizardImpl) {

    private var onDoneCallback: (() -> Unit)? = null

    private var onFailureCallback: (() -> Unit)? = null

    private var onDisposeCallback: (() -> Unit)? = null

    private val stages = mutableListOf<Stage>()

    fun stage(id: String = "", init: StageBuilder.() -> Unit) {
        val sb = StageBuilder(id).apply(init)
        stages += sb.build(wizard::onStageStateChanged)
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

    fun wizardDispose(onDispose: () -> Unit) {
        onDisposeCallback = onDispose
    }

    internal fun build(): Wizard {
        wizard += stages

        onDoneCallback?.let {
            wizard.onDoneCallback = it
        }

        onFailureCallback?.let {
            wizard.onFailureCallback = it
        }

        onDisposeCallback?.let {
            wizard.onDisposeCallback = it
        }

        return wizard
    }
}