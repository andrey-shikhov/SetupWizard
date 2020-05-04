![Bintray](https://img.shields.io/bintray/v/andrey-shikhov/SetupWizard/me.shikhov.setupwizard?style=for-the-badge)

![Android](https://img.shields.io/badge/minSdk-15-informational)
# SetupWizard 

Library to ease multi step setup with rollbacks, written in kotlin 

gradle dependency declaration:

    dependencies {
          implementation 'me.shikhov.setupwizard:setupwizard:0.9.0'
    }

Required dependencies: 
- kotlin stdlib
- androidX lifecycle, liveData

# Usage
1) Simple disposable wizard(created for one time usage) with autostart


        wizard {
            stage {
                simple {
                    // simple action
                }
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
            
            wizardDispose {
                // called after wizardDone or wizardFailure
            }
        }        

# ChangeLog
- 0.9.0 autoStart for disposable wizards, delayed subscription on Lifecycle events in the LifecycleWizard to avoid wizard launch before builder end  
- 0.8.0 wizardFailure, wizardDispose callbacks, disposable & reusable wizards, experimental parallel stage builder
- 0.7.0 added @DslMarker to restrict builders methods availability, updated dependencies
- 0.6.0 added Lifecycle aware wizard variant
- 0.5.0 base release
