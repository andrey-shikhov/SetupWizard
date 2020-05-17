package me.shikhov.setupwizardapp

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import me.shikhov.setupwizard.Wizard
import me.shikhov.setupwizard.extend
import me.shikhov.setupwizard.wizard
import me.shikhov.wlog.Log

private const val TAG = "wizard-fragment"

class SetupFragment : Fragment(R.layout.fragment_setup) {

    private lateinit var logView: TextView

    private val wizard = wizard(this,
                                restartPolicy = Wizard.RestartPolicy.CONTINUE) {
            step {
                logView.append("step stage proceed\n")
            }
            stage("named") {
                setUp {
                    logView.append("stage 2, setup\n")
                }
                proceed {
                    logView.append("stage 2, simple action\n")
                    done()
                }
                tearDown {
                    logView.append("stage 2, teardown\n")
                }
            }
            stage {
                setUp {
                    logView.append("stage 3, setup\n")
                }
                proceed {
                    logView.append("stage 3, procede\n")
                    logView.postDelayed(1000L) {
                        logView.append("stage 3, delayed end\n")
                        done()
                    }
                    logView.append("stage 3, procede setup end\n")
                }
                tearDown {
                    logView.append("stage 3, teardown\n")
                }
            }

            stage {
                proceed {
                    logView.append("stage 4, proceed\n")
                    if(System.currentTimeMillis() % 2 == 0L) done() else cancel()
                }
            }

            wizardDone {
                logView.append("Wizard done!\n")
            }

            wizardDispose {
                Log[TAG].a("** wizard dispose! **").r()
            }

            wizardFailure {
                logView.append("Wizard failed\n")
            }

            wizardStop {
                logView.append("Wizard stop I\n")
            }
        }

    init {
        wizard extend {
            stage {
                setUp {
                    logView.append("ex stage 1, setup\n")
                }
                proceed {
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
                logView.append("Wizard done II\n")
            }
            wizardStop {
                logView.append("Wizard stop II\n")
                logView.append("--------------\n")
            }
        }

        wizard.onChange.observe(this, Observer  { stage ->
            android.util.Log.i("wizard", "#${wizard.currentStageIndex}: $stage")
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        logView = view.findViewById(R.id.setup_log)

        view.findViewById<View>(R.id.setup_launch).setOnClickListener {
            logView.append("* start: $wizard\n")
            wizard.start()
        }

        view.findViewById<View>(R.id.setup_cancel).setOnClickListener {
            logView.append("* stop: $wizard\n")
            wizard.stop()
        }

        view.findViewById<View>(R.id.setup_destroy).setOnClickListener {
            logView.append("* destroy: $wizard\n")
            parentFragmentManager.commit {
                remove(this@SetupFragment)
            }
        }

        logView.append("$wizard\n")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log[TAG].a("onAttach").r()
    }

    override fun onResume() {
        super.onResume()
        Log[TAG].a("onResume").r()
    }

    override fun onPause() {
        super.onPause()
        Log[TAG].a("onPause").r()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log[TAG].a("onDestroy").r()
    }
}

