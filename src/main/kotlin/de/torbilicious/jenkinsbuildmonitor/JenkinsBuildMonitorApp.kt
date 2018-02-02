package de.torbilicious.jenkinsbuildmonitor

import javafx.application.Application
import javafx.stage.Stage
import tornadofx.App

class JenkinsBuildMonitorApp : App(BuildView::class) {
    override fun start(stage: Stage) {
        super.start(stage)
        stage.isResizable = false

        stage.width = 550.0
        stage.height = 600.0
    }
}

fun main(args: Array<String>) {
    Application.launch(JenkinsBuildMonitorApp::class.java, * args)
}
