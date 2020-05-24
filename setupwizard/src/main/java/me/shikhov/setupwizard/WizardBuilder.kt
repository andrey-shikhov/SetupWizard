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

    /**
     * Variable to use for creating automatic ids for stages
     */
    private var stageIndex = 1

    private fun createId(baseId: String): String {
        var id = if(baseId.isEmpty()) stageIndex.toString() else baseId
        val startIndex = stageIndex

        while(stages.any { it.id == id }) {
            stageIndex++
            id = if(baseId.isEmpty()) {
                stageIndex.toString()
            } else {
                // same id processing, same_id -> same_id2 -> same_id3 -> same_id4
                "$baseId${stageIndex - startIndex + 1}"
            }
        }

        stageIndex++
        return id
    }

    fun step(id: String = "",
             readableName: String = "",
             block: () -> Unit) {
        stages += Stage(createId(id),
                        readableName,
                        wizard::onStageStateChanged,
                        setup = { },
                        run = {
                            block()
                            done()
                        },
                        teardown = { },
                        onError = {_,_ ->},
                        onProgressChanged = wizard::onStageProgressChanged)
    }

    fun stage(id: String = "",
              readableName: String = "",
              init: StageBuilder.() -> Unit) {
        stages += StageBuilder(createId(id),
                               readableName,
                               wizard::onStageProgressChanged)
                                .apply(init)
                                .build(wizard::onStageStateChanged)
    }

    fun parallel(id: String = "",
                 readableName: String = "",
                 init: ParallelStageBuilder.() -> Unit) {
        stages += ParallelStageBuilder(wizard,
                                       createId(id),
                                       readableName)
                                        .apply(init).build()
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