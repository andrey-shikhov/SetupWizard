package me.shikhov.setupwizard

import android.os.Handler
import androidx.lifecycle.*

fun wizard(lifecycleOwner: LifecycleOwner? = null, init: WizardBuilder.() -> Unit): Wizard {
    val w =  if(lifecycleOwner == null) WizardImpl() else LifecycleWizard(lifecycleOwner)
    return WizardBuilder(w).apply(init).build()
}

infix fun Wizard.extend(init: WizardBuilder.() -> Unit) {
    val wb = WizardBuilder(this as WizardImpl)
    init(wb)
    wb.build()
}

sealed class Wizard {

    enum class State {
        CREATED,
        STARTED,
        PAUSED,
        DONE
    }

    abstract val state: State

    abstract val stageCount: Int

    abstract val currentStageIndex: Int

    abstract val onChange: LiveData<Stage>

    abstract fun start()

    abstract fun stop()

    internal abstract fun onStageStateChanged(stage: Stage, state: Stage.State)
}

internal open class WizardImpl : Wizard() {

    override val onChange = MutableLiveData<Stage>()

    var onDoneCallback: () -> Unit = { }

    var onFailureCallback: () -> Unit = { }

    final override var currentStageIndex: Int = -1
        private set

    override val stageCount: Int
        get() = stages.size

    override var state: State = State.CREATED
        protected set

    private val stages = mutableListOf<Stage>()

    private val handler = Handler()

    override fun start() {
        require(state == State.CREATED || state == State.PAUSED) { state }
        state = State.STARTED

        handler.post(::runNext)
    }

    private fun runNext() {
        val nextIndex = currentStageIndex + 1

        if(nextIndex == stages.size) {
            state = State.DONE
            onDoneCallback()
            return
        }

        currentStageIndex = nextIndex
        val stage = stages[nextIndex]
        stage.start()
    }

    override fun stop() {
        if(currentStageIndex in stages.indices) {
            val s = stages[currentStageIndex]
            s.cancel()
        }
    }

    override fun onStageStateChanged(stage: Stage, state: Stage.State) {
        when(state) {
            Stage.State.CREATED -> TODO()
            Stage.State.STARTED -> TODO()
            Stage.State.CANCELED -> TODO()
            Stage.State.DONE -> onStageDone(stage)
        }

        onChange.postValue(stage)
    }

    private fun onStageDone(stage: Stage) {
        stage.tearDown()

        if(state == State.STARTED)
            handler.post(::runNext)
    }

    fun onStageFailed(stage: Stage) {
        stage.tearDown()
        currentStageIndex = -1
    }

    internal operator fun plusAssign(stages: List<Stage>) {
        check(currentStageIndex < 0)
        this.stages += stages
    }
}

internal class LifecycleWizard(lifecycleOwner: LifecycleOwner): WizardImpl(), LifecycleEventObserver {
    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                if(state == State.CREATED || state == State.PAUSED)
                    start()
            }
            Lifecycle.Event.ON_PAUSE -> {
                if(state != State.DONE) {
                    state = State.PAUSED
                    stop()
                }
            }
            Lifecycle.Event.ON_DESTROY -> {
                source.lifecycle.removeObserver(this)
            }
            else -> { }
        }
    }
}