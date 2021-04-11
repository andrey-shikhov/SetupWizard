package me.shikhov.setupwizard

import androidx.annotation.IntRange
import androidx.lifecycle.*

/**
 * Builds a wizard object and initialize it by using [init] block.
 * @param usageType specifies usage of wizard, can it run multiple times, or it is single pass flow
 * @param autoStart flag to launch the wizard flow right after creation(called [Wizard.start]).
 * @param restartPolicy flag to determine from which point wizard should start after cancellation/failure
 * @param init actual wizard configuration
 */
fun wizard(usageType: Wizard.UsageType = Wizard.UsageType.DISPOSABLE,
           autoStart: Boolean = usageType == Wizard.UsageType.DISPOSABLE,
           restartPolicy: Wizard.RestartPolicy = Wizard.RestartPolicy.RESTART,
           init: WizardBuilder.() -> Unit): Wizard {

    val wizard = WizardImpl(usageType, restartPolicy)

    return WizardBuilder(wizard).apply(init).build().also {
        if(autoStart) it.start()
    }
}

/**
 * Builds a wizard object and initialize it by using [init] block.
 * Wizard will be bound to lifecycle,
 * - called [Wizard.start] when raised [Lifecycle.Event.ON_RESUME]
 * - called [Wizard.stop] when raised [Lifecycle.Event.ON_PAUSE]
 * - called [Wizard.dispose] when raised [Lifecycle.Event.ON_DESTROY]
 * @param lifecycleOwner typically, fragment or activity as a scope for the wizard object.
 * @param restartPolicy flag to select behavior when [Wizard.start] called after it was paused(
 * for example fragment was paused due to application was sent to the background and after restored)
 * @param init actual init block for wizard builder
 */
fun wizard(lifecycleOwner: LifecycleOwner,
           restartPolicy: Wizard.RestartPolicy = Wizard.RestartPolicy.RESTART,
           init: WizardBuilder.() -> Unit): Wizard {

    val wizard = LifecycleWizard(lifecycleOwner, restartPolicy)

    return WizardBuilder(wizard).apply(init).build()
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

abstract class Wizard internal constructor(internal val usageType: UsageType,
                                           internal val restartPolicy: RestartPolicy) {

    enum class UsageType {
        DISPOSABLE,
        REUSABLE
    }

    enum class RestartPolicy {
        /**
         * Restart from the first stage
         */
        RESTART,

        /**
         * Restart from the last failed stage
         */
        CONTINUE
    }

    /**
     * @param isLaunchable indicates that Wizard can be launched by [Wizard.start] from this state.
     */
    enum class State(internal val isLaunchable: Boolean) {
        /**
         * Wizard is ready to start
         */
        CREATED(true),
        /**
         *
         */
        RUNNING(false),
        PAUSED(true),
        DONE(false),
        CANCELED(false),
        FAILED(false),
        STOPPED(true),
        DISPOSED(false)
    }

    abstract val state: State

    abstract val currentStageIndex: Int

    abstract val stageCount: Int

    abstract val onChange: LiveData<Stage>

    abstract val onProgressChanged: LiveData<Int>

    abstract fun start()

    abstract fun stop()

    abstract fun dispose()

    internal abstract fun onStageStateChanged(stage: Stage, state: Stage.State)

    internal abstract fun onStageProgressChanged(stage: Stage, @IntRange(from = 0, to = 10_000) progress: Int)

    override fun toString(): String {
        return "Wizard($state): stages: $stageCount cur=${if(currentStageIndex >= 0) currentStageIndex.toString() else ""}"
    }
}