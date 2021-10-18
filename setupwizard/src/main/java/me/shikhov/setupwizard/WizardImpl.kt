package me.shikhov.setupwizard

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData

internal open class WizardImpl(usageType: UsageType,
                               restartPolicy: RestartPolicy ) : Wizard(usageType, restartPolicy) {

    override val onChange = MutableLiveData<Stage>()

    override val onProgressChanged = MutableLiveData(0)

    val onDone = mutableListOf<() -> Unit>()

    val onFailure = mutableListOf<() -> Unit>()

    val onStop = mutableListOf<() -> Unit>()

    val onDispose = mutableListOf<() -> Unit>()

    final override var currentStageIndex: Int = -1
        private set

    override val stageCount: Int
        get() = stages.size

    override var state: State = State.CREATED
        protected set(value) {
            field = value

            when(field) {
                State.CREATED,
                State.RUNNING,
                State.PAUSED -> { }

                State.DONE -> {
                    onDone.forEach { it() }

                    handler.post {
                        state = State.STOPPED
                    }
                }

                State.CANCELED,
                State.FAILED -> {
                    onFailure.forEach { it() }

                    handler.post {
                        state = State.STOPPED
                    }
                }

                State.STOPPED -> {
                    onStop.forEach { it() }

                    if(usageType == UsageType.DISPOSABLE) {
                        handler.post {
                            state = State.DISPOSED
                        }
                    }
                }

                State.DISPOSED -> onDispose.forEach { it() }
            }
        }

    private val stages = mutableListOf<Stage>()

    private val handler = Handler(Looper.getMainLooper())

    override fun start() {
        if(state == State.CANCELED && restartPolicy == RestartPolicy.RESTART) {
            currentStageIndex = -1
            state = State.CREATED
        }

        require(state.isLaunchable) { state }
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
        if(state == State.RUNNING) {
            if(currentStageIndex in stages.indices) {
                val s = stages[currentStageIndex]
                if(s.state == Stage.State.RUNNING) {
                    s.cancel() // will trigger onStateChanged with canceled state
                }
                else {
                    state = State.CANCELED
                }
            } else {
                state = State.CANCELED
            }
        }
    }

    override fun dispose() {
        require(state != State.DISPOSED)

        if(state == State.RUNNING)
            stop()
        else
            state = State.DISPOSED
    }

    override fun onStageStateChanged(stage: Stage, state: Stage.State) {
        when(state) {
            Stage.State.CREATED,
            Stage.State.RUNNING  -> { }
            Stage.State.CANCELED -> onStageCanceled(stage)
            Stage.State.DONE     -> onStageDone(stage)
            Stage.State.FAILED   -> onStageFailed(stage)
        }

        onChange.postValue(stage)
    }

    override fun onStageProgressChanged(stage: Stage, progress: Int) {

    }

    private fun onStageDone(stage: Stage) {
        stage.tearDown()

        if(state == State.RUNNING)
            handler.post(::runNext)
    }

    private fun onStageCanceled(stage: Stage) {
        stage.tearDown()
        state = State.CANCELED
        currentStageIndex = if(restartPolicy == RestartPolicy.RESTART) -1 else currentStageIndex - 1
    }

    private fun onStageFailed(stage: Stage) {
        stage.tearDown()
        state = State.FAILED
        currentStageIndex = if(restartPolicy == RestartPolicy.RESTART) -1 else currentStageIndex - 1
    }

    internal open operator fun plusAssign(stages: List<Stage>) {
        require(state == State.CREATED) { state }
        this.stages += stages
    }
}