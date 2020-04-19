package me.shikhov.setupwizard


@WizardMarker
class ParallelStageBuilder internal constructor(private val wizard: WizardImpl,
                                                private val stageId: String) {

    private val groupStage = GroupStage()

    private var counter = 0

    fun stage(id: String = "", init: StageBuilder.() -> Unit) {
        val childId = if(id.isNotEmpty())
                            "$stageId.$id"
                        else
                            "$stageId.${counter}".apply { counter++ }

        groupStage.stages += StageBuilder(childId)
                                .apply(init)
                                .build(groupStage::onStateChanged)
    }

    fun build(): Stage {
        return Stage(stageId,
            wizard::onStageStateChanged,
            { },
            groupStage::run,
            { },
            { _,_ -> })
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

    fun run(stage: Stage) {
        groupStage = stage

        stages.forEach {
            it.start()
        }
    }

    fun error(stage: Stage, throwable: Throwable) {

    }
}