import jetbrains.buildServer.configs.kotlin.v10.toExtId
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

    var chain = sequential {

        buildType(Maven("Compile", "clean compile"))

        parallel {
            buildType(Maven("Fast Test", "test", "-Dmaven.test.failure.ignore=true -Dtest=*.unit.*Test"))
            buildType(Maven("Slow Test", "test", "-Dmaven.test.failure.ignore=true -Dtest=*.integration.*Test"))
        }
        buildType(Maven("Package", "package"))
    }

    chain.buildTypes().forEachIndexed { index, bt ->
        {
            if (index == chain.buildTypes().lastIndex) {
                bt.triggers {
                    vcs {

                    }
                }
            }
            buildType(bt)
        }
    }
}

class Maven(name: String, goals: String, runnerArgs: String = "") : BuildType({
    id(("TheNextG_${name}").toExtId())
    this.name = name

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        maven {
            this.goals = goals
            this.runnerArgs = runnerArgs;
        }
    }
})

