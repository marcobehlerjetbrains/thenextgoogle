import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.freeDiskSpace
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2020.1"

project {

    buildType(Compile)
    buildType(FastTest)
    buildType(SlowTest)
    buildType(Package)

    sequential {
        buildType(Compile)
        parallel {
            buildType(FastTest)
            buildType(SlowTest)
        }
        buildType(Package)
    }
}

object Compile : BuildType({
    name = "Compile"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {

        val myGoals = "clean compile"

        maven {
            goals = myGoals
            runnerArgs = "-Dmaven.test.failure.ignore=true"
        }
    }

    features {
        freeDiskSpace {
            requiredSpace = "5gb"
            failBuild = true
        }
    }
})

object FastTest : BuildType({
    name = "FastTest"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {

        maven {
            goals = "test"
            runnerArgs = "-Dmaven.test.failure.ignore=true -Dtest=*.unit.*Test"
        }
    }

})

object SlowTest : BuildType({
    name = "SlowTest"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {

        maven {
            goals = "test"
            runnerArgs = "-Dmaven.test.failure.ignore=true -Dtest=*.integration.*Test"
        }
    }

})


object Package : BuildType({
    name = "Package"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {

        val myGoals = "package"

        maven {
            goals = myGoals
            runnerArgs = "-Dmaven.test.failure.ignore=true"
        }
    }

    triggers {
        vcs {
        }
    }
})

