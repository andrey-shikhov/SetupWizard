# SetupWizard
![Build with](https://img.shields.io/badge/kotlin-1.4.31-blue)  
![Android](https://img.shields.io/badge/minSdk-15-informational)

![Bintray(deprecated)](https://img.shields.io/bintray/v/andrey-shikhov/SetupWizard/me.shikhov.setupwizard?style=for-the-badge)  
![MavenCentral]()

Library to ease multi step setup with rollbacks, written in kotlin 

gradle dependency declaration:

    dependencies {
          // old, deprecated, at jcenter
          implementation 'me.shikhov.setupwizard:setupwizard:0.9.0'
          // new published one, at mavenCentral
          implementation 'me.shikhov:setupwizard:1.0.0'            
    }

Required dependencies: 
- kotlin stdlib
- androidX lifecycle, liveData

# Basics
Wizard is basically a state machine which allows executing client specific code sequentially,
with handy dsl which allows subscription on the execution result.
 
![Wizard states graph](/diagrams/wizard_states.png?raw=true "Wizard states graph")

# Usage
1) Simple disposable wizard(created for one time usage) with autostart


        wizard {
            step {                
                // simple action                
            }
            stage("namedStage") {
                setUp {
                    // called before main action in proceed block
                }
                
                proceed {
                    // here must be scheduled call of done() or cancel()
                    // scheduled - means it can be called later after operation completes                 
                    if(true) 
                        done()                                                
                }
                
                tearDown {
                    // called after 'proceed' part ended(done or canceled, no matter) 
                    // Note: Will not called if setUp fails
                }
            }
        }        

2) Adding stages to created wizard(it must not be running at the moment)


        wizard extend {
            // same methods as during creation original wizard
            stage {               
                ...
            }
        }    
    
3) Running wizard automatically with lifecycle of Activity or Fragment.


        class SomeFragment: Fragment {
            
           // This wizard will start automatically when fragment resumes.
           // Also this wizard will be auto canceled if fragment will be destroyed before wizard finishes 
           // all the stages.
           // You can use viewLifecycleOwner property instead of this if wizard binded to view
           val wizard = wizard(this) {
                stage {
                    ...                
                }
           }         
        }
    
4) Do something when all stages completed:


        wizard {
            stage {
                ...
            }            
            
            wizardDone {
                // called when all the stages are completed successfully.
            }
            
            wizardFailure {
                // called when one of the stages fails or is canceled
            }
            
            wizardStop {
                // called after wizardDone or wizardFailure
            }
            
            wizardDispose {
                // called when lifecycle owner is destroyed or if manually called dispose() method of wizard 
            }
        }        

# ChangeLog
- 1.0.0 rewritten publishing, changed destination from jcenter to mavenCentral
- 0.10.0 added RestartPolicy, simplified root builder functions, wizardStop event, wizard events now can have multiple listeners, simple stage to be replaced with step block 
- 0.9.0 autoStart for disposable wizards, delayed subscription on Lifecycle events in the LifecycleWizard to avoid wizard launch before builder end  
- 0.8.0 wizardFailure, wizardDispose callbacks, disposable & reusable wizards, experimental parallel stage builder
- 0.7.0 added @DslMarker to restrict builders methods availability, updated dependencies
- 0.6.0 added Lifecycle aware wizard variant
- 0.5.0 base release
