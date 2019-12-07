package me.shikhov.setupwizard

class WizardBuilder internal constructor(private val wizard: WizardImpl) {

    private var onDoneCallback: (() -> Unit)? = null

    private var onFailureCallback: (() -> Unit)? = null

    private val stages = mutableListOf<Stage>()

    fun stage(id: String = "", init: StageBuilder.() -> Unit) {
        val sb = StageBuilder(wizard)
        sb.id = id
        init(sb)
        stages += sb.build()
    }

    fun wizardDone(onDone: () -> Unit) {
        onDoneCallback = onDone
    }

    fun wizardFailure(onFailure: () -> Unit) {
        onFailureCallback = onFailure
    }

    internal fun build(): Wizard {
        wizard += stages

        onDoneCallback?.let {
            wizard.onDoneCallback = it
        }

        return wizard
    }
}