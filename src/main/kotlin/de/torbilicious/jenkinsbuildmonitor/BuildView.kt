package de.torbilicious.jenkinsbuildmonitor

import com.offbytwo.jenkins.JenkinsServer
import com.offbytwo.jenkins.model.Job
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.util.Duration
import tornadofx.*
import java.net.URI


private const val windowTitle = "Jenkins Build Monitor"

class BuildView : View(windowTitle) {

    override val root = BorderPane()

    private var jenkins = initJenkinsServer()

    private val jobsProperty = SimpleListProperty<Job>()


    private lateinit var jobsBox: VBox

    init {
        with(root) {
            prefHeight = 600.0
            prefWidth = 550.0

            paddingAll = 10

            center {

                listview<Job>(jobsProperty) {
                    cellFormat {
                        val hasNeverRun = it.details().allBuilds.size == 0
                        val lastBuildStatus = if (hasNeverRun) {
                            BuildStatus.NEVER_RUN
                        } else {
                            if (it.details().hasLastFailedBuildRun()) {
                                BuildStatus.FAILURE
                            } else {
                                BuildStatus.SUCCESS
                            }
                        }

                        text = "${it.name} -> $lastBuildStatus"

                        style {
                            backgroundColor = when (lastBuildStatus) {
                                BuildStatus.NEVER_RUN -> multi(Color.GREY)
                                BuildStatus.FAILURE -> multi(Color.RED)
                                BuildStatus.SUCCESS -> multi(Color.GREEN)
                            }
                        }
                    }
                }

                val allJobs = jenkins.jobs.map { it.value }
                jobsProperty.set(FXCollections.observableArrayList(allJobs))
            }
        }

        val fiveSecondsWonder = Timeline(KeyFrame(Duration.seconds(5.0), EventHandler<ActionEvent> {
            println("Updating Jobs")

            setNewTitle(windowTitle + " (Updating...)")

            runAsync {
                jenkins.jobs.map { it.value }
            } ui {
                jobsProperty.set(FXCollections.observableArrayList(it))
                setNewTitle(windowTitle)
            }
        }))

        fiveSecondsWonder.cycleCount = Timeline.INDEFINITE
        fiveSecondsWonder.play()
    }

    private fun setNewTitle(title: String) {
        this.title = title
    }

    private fun initJenkinsServer(): JenkinsServer {
        return JenkinsServer(
                URI("http://localhost:32774"),
                "admin",
                "admin"
        )
    }
}
