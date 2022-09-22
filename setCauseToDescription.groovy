def setCauseToDescription(){
    @NonCPS
    cause = currentBuild.rawBuild.getCause(hudson.model.Cause.UserIdCause)
    userId = cause?.userId
    if (userId) {
        currentBuild.setDescription("executed by ${userId}")
    }
}

pipeline {
    agent any

    stages {
        stage('setCauseToDescription') {
            steps {
                script {
                   setCauseToDescription()
                }
            }
        }
    }
}

