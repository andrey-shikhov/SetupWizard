package me.shikhov.setupwizard


@WizardMarker
class ParallelStageBuilder internal constructor(private val wizard: WizardImpl,
                                                private val stageId: String,
                                                private val readableName: String) {

    private val groupStage = GroupStage()

    private var counter = 0

    fun stage(id: String = "",
              readableName: String = "",
              init: StageBuilder.() -> Unit) {
        val childId = if(id.isNotEmpty())
                            "$stageId.$id"
                        else
                            "$stageId.${counter}".apply { counter++ }

        groupStage.stages += StageBuilder(childId,
                                          readableName,
                                          groupStage::onStateProgressChanged)
                                .apply(init)
                                .build(groupStage::onStateChanged)
    }

    fun build(): Stage {
        return Stage(stageId,
            readableName,
            wizard::onStageStateChanged,
            { },
            groupStage::run,
            { },
            { _,_ -> },
            wizard::onStageProgressChanged)
    }
}

internal class GroupStage {

    val stages = ArrayList<Stage>()

    private lateinit var groupStage: Stage

    private val stagesStates = mutableMapOf<String, Stage.State>()

    fun onStateChanged(stage: Stage, state: Stage.State) {
        stagesStates[stage.id] = state

        if(stagesStates.size == stages.size && state.isFinished) {
            if(stagesStates.values.all { it.isFinished }) {
                groupStage.done()
            }
        }
    }

    fun onStateProgressChanged(stage: Stage, progressLevel: Int) {
        val progress = stages.map { it.progressLevel }.average().toInt()
        groupStage.reportProgress(progress)
    }

    fun run(stage: Stage) {
        groupStage = stage

        stages.forEach {
            it.start()
        }
    }

    fun error(stage: Stage, throwable: Throwable) {

    }
}