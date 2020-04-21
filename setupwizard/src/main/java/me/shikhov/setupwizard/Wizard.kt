package me.shikhov.setupwizard

import android.os.Handler
import androidx.lifecycle.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Builds a wizard object and initialize it by using [init] block.
 * As for now, 2 implementations of [Wizard] available:
 * 1. [WizardImpl] as base implementation
 * 2. [LifecycleWizard] which extends WizardImpl with automatic start/stop behaviour
 * according to state of [lifecycleOwner]
 * @param lifecycleOwner typically fragment or activity as scope for the wizard object.
 *                       if null creates [WizardImpl] object, [LifecycleWizard] otherwise
 * @param usageType specifies usage of wizard, can it run multiple times, or it is single pass flow
 * @param autoStart used only when [lifecycleOwner] is null to launch the wizard flow after creation if needed.
 * @param init actual wizard configuration
 */
fun wizard(lifecycleOwner: LifecycleOwner? = null,
           usageType: Wizard.UsageType = Wizard.UsageType.DISPOSABLE,
           autoStart: Boolean = usageType == Wizard.UsageType.DISPOSABLE,
           init: WizardBuilder.() -> Unit): Wizard {

    val wizard = lifecycleOwner?.run {
        LifecycleWizard(lifecycleOwner, usageType)
    } ?: WizardImpl(usageType)

    return WizardBuilder(wizard).apply(init).build().also {
        if(autoStart && usageType == Wizard.UsageType.DISPOSABLE) it.start()
    }
}

/**
 * Allows adding stages to the existing wizard,
 * usually in derived fragments when subclass need to add several stages.
 * REQUIRED: wizard must be in [Wizard.State.CREATED] state, because it will cause undefined behaviour
 */
infix fun Wizard.extend(init: WizardBuilder.() -> Unit) {
    val wb = WizardBuilder(this as WizardImpl)
    init(wb)
    wb.build()
}

sealed class Wizard(val usageType: UsageType) {

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

    abstract val state: State

    abstract val stageCount: Int

    abstract val currentStageIndex: Int

    abstract val onChange: LiveData<Stage>

    abstract fun start()

    abstract fun stop()

    abstract fun dispose()

    internal abstract fun onStageStateChanged(stage: Stage, state: Stage.State)
}

internal open class WizardImpl(usageType: UsageType) : Wizard(usageType) {

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