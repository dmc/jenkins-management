import jenkins.*
import jenkins.model.*
import hudson.*
import hudson.model.*
import org.apache.commons.lang3.time.DurationFormatUtils
import java.util.concurrent.TimeUnit

def errorMessages = []
def String createMessage(build) {
  """<a href="${build.getAbsoluteUrl()}">${build.toString()}</a> takes ${DurationFormatUtils.formatDuration(execTime ,"mm:ss")}"""
}

pipeline {
  parameters {
    string(name: 'WEBHOOK_URL', defaultValue: 'https://', description: 'teams webhook url')
    string(name: 'AGENT', defaultValue: 'master', description: 'agent which executes groovy')
    string(name: 'HOURS', defaultValue: '10', description: 'time limit to alarm')
  }
  agent {
    label "${params.AGENT}"
  }
  stages {
    stage('watch jobs') {
      steps {
        script {
          long limit = TimeUnit.HOURS.toMillis(Integer.parseInt("${params.HOURS}"))
          def jobs = Jenkins.instance.getAllItems(Job.class)
          def builds = jobs.collect {
            it.builds.findAll {
              it.isBuilding()
            }
          }.flatten()

          for (build in builds) {
            def execTime = System.currentTimeMillis() - build.getTimestamp().getTimeInMillis()
            if (limit < execTime) {
              currentBuild.result = "UNSTABLE"
              println("""${build.toString()} takes ${DurationFormatUtils.formatDuration(execTime ," mm:ss")}""")
              errorMessages.add(createMessage(build))
            }
          }
        }
      }
    }
  }
  post {
    always {
      script {
        if (currentBuild.result != null && currentBuild.result != "SUCCESS") {
          office365ConnectorSend webhookUrl: "${params.WEBHOOK_URL}",
            status: "${currentBuild.result}",
            factDefinitions: [
              [name: "Result", template: "<a href='${BUILD_URL}'>${JOB_NAME} #${BUILD_ID}</a>:${currentBuild.result}"],
              [name: "Detail", template: "${errorMessages.join('<br>')}"]]
        }
      }
    }
  }
}
