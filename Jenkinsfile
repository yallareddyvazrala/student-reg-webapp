node {
    def remoteHost = "172.31.21.97"
    def remoteUser = "ec2-user"
    def remotePath = "/opt/tomcat/webapps/"
    def warFile = "target/student-reg-webapp.war"
    def recipients = "dev-team@example.com"
    def mavenToolName = "Maven-3.9.10"
    def mavenHome = tool name: mavenToolName, type: 'maven'
    try {

        stage("Git Clone") {
            git branch: 'development', changelog: false, credentialsId: 'GitHubCred', url: 'https://github.com/Rushi-Technologies/student-reg-webapp.git'
        }

        stage("Build WAR") {
            sh "${mavenHome}/bin/mvn clean package -DskipTests"
        }

        stage("Copy WAR to Remote Server") {
            sshagent(['Tomcat_Server']) {
                sh """
                    echo "Copying ${warFile} to ${remoteUser}@${remoteHost}:${remotePath}"
                    scp -o StrictHostKeyChecking=no ${warFile} ${remoteUser}@${remoteHost}:${remotePath}
                """
            }
        }

        stage("Restart Tomcat") {
            sshagent(['Tomcat_Server']) {
                sh """
                    echo "Waiting for WAR copy to complete..."
                    sleep 5
                    echo "Stopping Tomcat on ${remoteUser}@${remoteHost}"
                    ssh -o StrictHostKeyChecking=no ${remoteUser}@${remoteHost} 'sudo systemctl stop tomcat'
                    echo "Waiting for Tomcat to shut down gracefully..."
                    sleep 5
                    echo "Starting Tomcat on ${remoteUser}@${remoteHost}"
                    ssh -o StrictHostKeyChecking=no ${remoteUser}@${remoteHost} 'sudo systemctl start tomcat'
                """
            }
        }

    } catch (err) {
        echo "An error occurred: ${err}"
        currentBuild.result = "FAILURE"
        throw err
    } finally {
        def buildStatus = currentBuild.result ?: 'SUCCESS'
        sendEmail(buildStatus, recipients)
        notifyBuild(buildStatus)
    }
}

def sendEmail(buildStatus, recipients) {
        def body = ""
        def subject = "Jenkins Build ${buildStatus}: ${env.JOB_NAME} #${env.BUILD_NUMBER}"

        if (buildStatus == "SUCCESS") {
            body = """
                <h3 style='color:green;'>Build Successful</h3>
                <p><b>Job:</b> ${env.JOB_NAME}</p>
                <p><b>Build Number:</b> #${env.BUILD_NUMBER}</p>
                <p><a href='${env.BUILD_URL}'>Click here to view logs</a></p>
            """
        } else {
            body = """
                <h3 style='color:red;'>Build Failed</h3>
                <p><b>Job:</b> ${env.JOB_NAME}</p>
                <p><b>Build Number:</b> #${env.BUILD_NUMBER}</p>
                <p><a href='${env.BUILD_URL}'>Click here to view logs</a></p>
            """
        }

        emailext(
            subject: subject,
            body: body,
            to: recipients,
            mimeType: 'text/html'
        )
        echo "Email sent to ${recipients} with subject: ${subject}"
}

def slackNotification(String buildStatus) {
    echo "Build Status: ${buildStatus}"
    def colorCode = (buildStatus == 'SUCCESS') ? '#00FF00' : '#FF0000'
    def summary = "*${env.JOB_NAME}* Build #${env.BUILD_NUMBER} - *${buildStatus}*\n${env.BUILD_URL}"
    slackSend(color: colorCode, message: summary)
}
