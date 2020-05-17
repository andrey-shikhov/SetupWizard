package me.shikhov.setupwizard

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

internal class LifecycleWizard(private val lifecycleOwner: LifecycleOwner,
                               restartPolicy: RestartPolicy): WizardImpl(UsageType.REUSABLE, restartPolicy),
                                                            LifecycleEventObserver {

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
                    stop()
            }
            Lifecycle.Event.ON_DESTROY -> {
                source.lifecycle.removeObserver(this)
                dispose()
            }
            else -> { }
        }
    }

    override fun plusAssign(stages: List<Stage>) {
        val isInit = stageCount == 0

        super.plusAssign(stages)

        if(isInit) {
            lifecycleOwner.lifecycle.addObserver(this)
        }
    }
}