![Bintray](https://img.shields.io/bintray/v/andrey-shikhov/SetupWizard/me.shikhov.setupwizard?style=for-the-badge)

![Android](https://img.shields.io/badge/minSdk-15-informational)
# SetupWizard 

Library to ease multi step setup with rollbacks, written in kotlin 

gradle dependency declaration:

dependencies {
  implementation 'me.shikhov.setupwizard:setupwizard:0.8.0'
}

Required dependencies: 
- kotlin stdlib

# Usage

    

# ChangeLog
- 0.8.0 wizardFailure, wizardDispose callbacks, disposable & reusable wizards, experimental parallel stage builder
- 0.7.0 added @DslMarker to restrict builders methods availability, updated dependencies
- 0.6.0 added Lifecycle aware wizard variant
- 0.5.0 base release
