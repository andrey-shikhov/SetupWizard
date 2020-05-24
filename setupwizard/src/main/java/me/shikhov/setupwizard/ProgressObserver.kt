package me.shikhov.setupwizard


class ProgressObserver(private val wizard: Wizard) {

    init {

        wizard.onChange.observeForever {
            onStateChanged(it)
        }
    }

    fun onStateChanged(stage: Stage) {

    }

}