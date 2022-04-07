pipeline {
  parameters {
    string(name: 'WEBHOOK_URL', defaultValue: 'https://dmc140.webhook.office.com/webhookb2/c91c7d52-fd4f-4d91-9665-6da80a64bbe8@51b92261-76ef-4836-af43-b346894f9e4d/IncomingWebhook/144571c9dbc64840b5f019a9a2edb859/35b6241d-b538-4294-9c0e-33e1edc20ec1', description: 'teams webhook url')
  }
  agent {
    label "master"
  }
  stages {
    stage('clone') {
      steps {
        script {
          def nodes = "${params.NODES}".split(',')
          for (i in 0..3) {
            cleanWs()
            git poll: url: 'https://github.com/dmc/jenkins-management.git'
            sleep 10
          }
        }
      }
    }
  }  
  post {
    always {
      script {
        office365ConnectorSend webhookUrl: "${params.WEBHOOK_URL}",
          status: "${currentBuild.result}",
          factDefinitions: [[name: "Result", template: "<a href='${BUILD_URL}'>${JOB_NAME} #${BUILD_ID}</a>:${currentBuild.result}"]]
        
      }
    }
  }
}
