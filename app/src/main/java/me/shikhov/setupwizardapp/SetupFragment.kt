package me.shikhov.setupwizardapp

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import me.shikhov.setupwizard.extend
import me.shikhov.setupwizard.wizard


class SetupFragment : Fragment(R.layout.fragment_setup) {

    private lateinit var logView: TextView

    private val wizard = wizard {
        stage() {
            setUp {
                logView.append("stage 1, setup\n")
            }
            simple {
                logView.append("stage 1, simple action\n")
            }
            tearDown {
                logView.append("stage 1, teardown\n")
            }
        }
        stage {
            setUp {
                logView.append("stage 2, setup\n")
            }
            procede {
                logView.append("stage 2, procede\n")
                logView.postDelayed(1000L) {
                    logView.append("stage 2, delayed end\n")
                    done()
                }
                logView.append("stage 2, procede setup end\n")
            }
            tearDown {
                logView.append("stage 2, teardown\n")
            }
        }

        wizardDone {
            logView.append("I done!\n")
        }
    }

    init {
        wizard extend {
            stage {
                setUp {
                    logView.append("ex stage 1, setup\n")
                }
                procede {
                    logView.append("ex stage 1, procede\n")
                    logView.postDelayed(1000L) {
                        logView.append("ex stage 1, delayed end\n")
                        done()
                    }
                    logView.append("ex stage 1, procede setup end\n")
                }
                tearDown {
                    logView.append("ex stage 1, teardown\n")
                }
            }
            stage {

            }

            wizardDone {
                logView.append("II done!\n")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        logView = view.findViewById(R.id.setup_log)

        view.findViewById<View>(R.id.setup_launch).setOnClickListener {
            wizard.start()
        }

        view.findViewById<View>(R.id.setup_cancel).setOnClickListener {
            wizard.stop()
        }

        logView.append("wizard: ${wizard.currentStage}/${wizard.stageCount}")
    }
}

