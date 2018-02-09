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
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*


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

                        Instant.ofEpochMilli()

                        val sdf = SimpleDateFormat("HH:mm:ss")
                        val lastBuildTime = sdf.format(Date(it.details().lastBuild.details().timestamp))

                        text = "${it.name}  $lastBuildTime"

                        style {
                            backgroundColor = when (lastBuildStatus) {
                                BuildStatus.NEVER_RUN -> multi(Color.GREY)
                                BuildStatus.FAILURE -> multi(Color.RED)
                                BuildStatus.SUCCESS -> multi(Color.GREEN)
                            }
                        }
                    }
                }

                jobsProperty.set(FXCollections.observableArrayList(getJobs()))
            }
        }

        val fiveSecondsWonder = Timeline(KeyFrame(Duration.seconds(5.0), EventHandler<ActionEvent> {
            println("Updating Jobs")

            setNewTitle(windowTitle + " (Updating...)")

            runAsync {
                getJobs()
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
                URI("http://localhost:32769"),
                "admin",
                "admin"
        )
    }

    private fun getJobs(): List<Job> {
        return jenkins.jobs.map { it.value }.sortedByDescending { it.details().lastBuild.details().timestamp }
    }
}
