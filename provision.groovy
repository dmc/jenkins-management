import hudson.model.Label
import hudson.model.Node
import hudson.model.labels.LabelAtom
import hudson.plugins.ec2.AmazonEC2Cloud
import hudson.plugins.ec2.EC2AbstractSlave
import hudson.plugins.ec2.SlaveTemplate
import jenkins.model.Jenkins

// provision EC2 instances for agent label
label = "cppcheck"
agent = new LabelAtom(label)

Jenkins.instance.clouds.findAll { 
    it?.canProvision(agent) 
}.each { 
    template = it.getTemplate(agent)
    List<Node> nodes = template.provision(1, EnumSet.of(SlaveTemplate.ProvisionOptions.FORCE_CREATE))
    nodes.each { node ->
        Jenkins.instance.addNode(node)
    }
    println "Provisioned ${label} as new agent."
} ?: println('No agents provisioned.')

null
