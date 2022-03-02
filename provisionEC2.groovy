import hudson.model.Label
import hudson.model.Node
import hudson.model.labels.LabelAtom
import hudson.plugins.ec2.AmazonEC2Cloud
import hudson.plugins.ec2.EC2AbstractSlave
import hudson.plugins.ec2.SlaveTemplate
import jenkins.model.Jenkins

@NonCPS
def provision(agentLabel) {
  result = false  
  agent = (Jenkins.instance.getLabel(agentLabel)) ?: (new LabelAtom(agentLabel))
  Jenkins.instance.clouds.findAll().each { cloud ->
    template = cloud.getTemplate(agent)
    if (template == null) {
      println("${agentLabel} is not provisioned")
    } else {
      node = doProvision(template)
      Jenkins.instance.addNode(node)
      println "Provisioned ${node} new agents."
      result = true
    }
  }
  result
}

@NonCPS
def doProvision(template) {
  template.provision(1, EnumSet.of(SlaveTemplate.ProvisionOptions.FORCE_CREATE))
}

pipeline {
  parameters {
    string(name: 'AGENT', defaultValue: 'master', description: 'agent which executes groovy')
    string(name: 'EC2_NODE_LABEL', defaultValue: 'maven', description: 'label of EC2 Instance slave')
  }
  agent {
    label "${params.AGENT}"
  }
  stages {
    stage('print env') {
      steps {
        script {
          sh('env|sort')
        }
      }
    }
    stage('provision ec2') {
      steps {
        script {
          provision("${params.EC2_NODE_LABEL}")
        }
      }
    }
  } 
}  
