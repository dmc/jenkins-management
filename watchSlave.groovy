def errorMessages = []
def String createMessage(slave) {
  """<a href="${JENKINS_URL}${slave.searchUrl}">${slave.name}</a> is Offline"""
}

pipeline {
  parameters {
    string(name: 'WEBHOOK_URL', defaultValue: 'https://dmc140.webhook.office.com/webhookb2/c91c7d52-fd4f-4d91-9665-6da80a64bbe8@51b92261-76ef-4836-af43-b346894f9e4d/IncomingWebhook/144571c9dbc64840b5f019a9a2edb859/35b6241d-b538-4294-9c0e-33e1edc20ec1', description: 'teams webhook url')
    string(name: 'AGENT', defaultValue: 'master', description: 'agent which executes groovy')
    string(name: 'NODES', defaultValue: 'linux', description: 'slaves which will be checked online or not')
  }
  agent {
    label "${params.AGENT}"
  }
  stages {
    stage('watch slave') {
      steps {
        script {
          def nodes = "${params.NODES}".split(',')
          for (slave in hudson.model.Hudson.instance.slaves) {
            if (nodes.contains(slave.name)) {
              if (slave.getComputer().isOffline()) {
                currentBuild.result = "UNSTABLE"
                println("${slave.name} is offline..")
                errorMessages.add(createMessage(slave))
              }
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
              [name: "Detail", template: "${errorMessages.join('<br>')}"]
            ]
        }
      }
    }
  }
}
