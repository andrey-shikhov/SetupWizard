package me.shikhov.setupwizard

import android.os.Handler
import androidx.lifecycle.*

fun wizard(lifecycleOwner: LifecycleOwner? = null,
           usageType: Wizard.UsageType = Wizard.UsageType.DISPOSABLE,
           init: WizardBuilder.() -> Unit): Wizard {
    val w =  if(lifecycleOwner == null) WizardImpl(usageType) else LifecycleWizard(lifecycleOwner, usageType)
    return WizardBuilder(w).apply(init).build()
}

infix fun Wizard.extend(init: WizardBuilder.() -> Unit) {
    val wb = WizardBuilder(this as WizardImpl)
    init(wb)
    wb.build()
}

sealed class Wizard {

    enum class UsageType {
        DISPOSABLE,
        REUSABLE
    }

    enum class State {
        CREATED,
        RUNNING,
        PAUSED,
        DONE,
        CANCELED,
        FAILED,
        DISPOSED
    }

    abstract val usageType: UsageType

    abstract val state: State

    abstract val stageCount: Int

    abstract val currentStageIndex: Int

    abstract val onChange: LiveData<Stage>

    abstract fun start()

    abstract fun stop()

    abstract fun dispose()

    internal abstract fun onStageStateChanged(stage: Stage, state: Stage.State)
}

internal open class WizardImpl(override val usageType: UsageType) : Wizard() {

    override val onChange = MutableLiveData<Stage>()

    var onDoneCallback: () -> Unit = { }

    var onFailureCallback: () -> Unit = { }

    var onDisposeCallback: () -> Unit = { }

    final override var currentStageIndex: Int = -1
        private set

    override val stageCount: Int
        get() = stages.size

    override var state: State = State.CREATED
        protected set(value) {
            field = value

            when(field) {
                State.DONE     -> {
                    onDoneCallback()

                    if(usageType == UsageType.DISPOSABLE) {
                        handler.post {
                            state = State.DISPOSED
                        }
                    }
                }

                State.CANCELED,
                State.FAILED   -> {
                    onFailureCallback()

                    if(usageType == UsageType.DISPOSABLE) {
                        handler.post {
                            state = State.DISPOSED
                        }
                    }
                }

                State.DISPOSED -> onDisposeCallback()

                State.CREATED,
                State.RUNNING,
                State.PAUSED -> { }
            }
        }

    private val stages = mutableListOf<Stage>()

    private val handler = Handler()

    override fun start() {
        require(state == State.CREATED || state == State.PAUSED) { state }
        state = State.RUNNING

        handler.post(::runNext)
    }

    private fun runNext() {
        val nextIndex = currentStageIndex + 1

        if(nextIndex == stages.size) {
            state = State.DONE
            return
        }

        currentStageIndex = nextIndex
        val stage = stages[nextIndex]
        stage.start()
    }

    override fun stop() {
        if(currentStageIndex in stages.indices) {
            val s = stages[currentStageIndex]
            s.cancel() // will trigger onStateChanged with canceled state
        } else {
            state = State.CANCELED
        }
    }

    override fun dispose() {
        require(state != State.DISPOSED)
        state = State.DISPOSED
    }

    override fun onStageStateChanged(stage: Stage, state: Stage.State) {
        when(state) {
            Stage.State.CREATED,
            Stage.State.STARTED  -> { }
            Stage.State.CANCELED -> onStageCanceled(stage)
            Stage.State.DONE     -> onStageDone(stage)
            Stage.State.FAILED   -> onStageFailed(stage)
        }

        onChange.postValue(stage)
    }

    private fun onStageDone(stage: Stage) {
        stage.tearDown()

        if(state == State.RUNNING)
            handler.post(::runNext)
    }

    private fun onStageCanceled(stage: Stage) {
        stage.tearDown()
        state = State.CANCELED
    }

    private fun onStageFailed(stage: Stage) {
        stage.tearDown()
        currentStageIndex = -1
        state = State.FAILED
    }

    internal operator fun plusAssign(stages: List<Stage>) {
        require(state == State.CREATED)
        check(currentStageIndex < 0)
        this.stages += stages
    }
}

internal class LifecycleWizard(lifecycleOwner: LifecycleOwner, usageType: UsageType): WizardImpl(usageType), LifecycleEventObserver {
    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                if(state == State.CREATED ||
                   state == State.PAUSED ||
                   state == State.FAILED ||
                   state == State.CANCELED)
                    start()
            }
            Lifecycle.Event.ON_PAUSE -> {
                if(state == State.RUNNING) {
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