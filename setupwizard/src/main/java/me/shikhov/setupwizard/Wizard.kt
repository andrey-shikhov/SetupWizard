package me.shikhov.setupwizard

import android.os.Handler

fun wizard(init: WizardBuilder.() -> Unit): Wizard {
    val w = WizardImpl()
    val wb = WizardBuilder(w)
    init(wb)
    wb.build()
    return w
}

infix fun Wizard.extend(init: WizardBuilder.() -> Unit) {
    val wb = WizardBuilder(this as WizardImpl)
    init(wb)
    wb.build()
}

sealed class Wizard {

    abstract val stageCount: Int

    abstract val currentStage: Int

    abstract fun start()

    abstract fun stop()
}

internal class WizardImpl : Wizard() {

    internal var onDoneCallback: () -> Unit = { }

    internal var onFailureCallback: () -> Unit = { }

    private val stages = mutableListOf<Stage>()

    private var currentStageIndex: Int = -1

    private val handler = Handler()

    override val stageCount: Int
        get() = stages.size

    override val currentStage: Int
        get() = currentStageIndex

    override fun start() {
        require(currentStageIndex < 0)

        handler.post(::runNext)
    }

    private fun runNext() {
        val nextIndex = currentStageIndex + 1

        if(nextIndex == stages.size) {
            onDoneCallback()
            return
        }

        currentStageIndex = nextIndex
        val stage = stages[nextIndex]
        stage.setUp()
        stage.start()
    }

    override fun stop() {
        if(currentStageIndex in stages.indices) {
            val s = stages[currentStageIndex]
            s.cancel()
        }
    }

    internal fun onStageDone(stage: Stage) {
        stage.tearDown()
        handler.post(::runNext)
    }

    internal fun onStageFailed(stage: Stage) {
        stage.tearDown()
        currentStageIndex = -1

    }

    internal operator fun plusAssign(stages: List<Stage>) {
        check(currentStageIndex < 0)
        this.stages += stages
    }
}