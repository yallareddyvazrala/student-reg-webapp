pipeline {
    agent any

    environment {
        remoteHost = "172.31.21.97"
        remoteUser = "ec2-user"
        remotePath = "/opt/tomcat/webapps/"
        warFile = "target/student-reg-webapp.war"
        recipients = "balajireddy.urs@gmail.com"
    }
    
   triggers {
     githubPush()
   }


    tools {
        maven "Maven-3.9.10"
    }
    options {
        buildDiscarder(logRotator(numToKeepStr: '5'))
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
    }
    stages {
       /* stage('Git Clone') {
            steps {
                 //checkout scm // This will use the SCM configured in the Jenkins job And Webhooks will only with this option.
                // Webhooks will not trigger with below option.
                //git branch: 'development', changelog: false, credentialsId: 'GitHubCred', url: 'https://github.com/Rushi-Technologies/student-reg-webapp.git'
            }
        } */ 

        stage('Build and Test') {
            steps {
                sh "mvn clean verify"
            }
        }
        stage('SonarQube Analysis') {
            steps {
                sh "mvn sonar:sonar"
            }
        }
         stage('Deploy to Nexus') {
            steps {
                sh "mvn deploy"
            }
        }

        stage('Stop Tomcat') {
            steps {
                sshagent(['Tomcat_Server']) {
                    sh """
                        echo "Stopping Tomcat on ${env.remoteUser}@${env.remoteHost}"
                        ssh -o StrictHostKeyChecking=no ${env.remoteUser}@${env.remoteHost} 'sudo systemctl stop tomcat'
                        echo "Waiting for Tomcat to shut down gracefully..."
                        sleep 5
                    """
                }
            }
        }

        stage('Copy WAR to Remote Server') {
            steps {
                sshagent(['Tomcat_Server']) {
                    sh """
                        echo "Copying ${env.warFile} to ${env.remoteUser}@${env.remoteHost}:${env.remotePath}"
                        scp -o StrictHostKeyChecking=no ${env.warFile} ${env.remoteUser}@${env.remoteHost}:${env.remotePath}
                    """
                }
            }
        }

        stage('Start Tomcat') {
            steps {
                sshagent(['Tomcat_Server']) {
                    sh """
                        echo "Starting Tomcat on ${env.remoteUser}@${env.remoteHost}"
                        ssh -o StrictHostKeyChecking=no ${env.remoteUser}@${env.remoteHost} 'sudo systemctl start tomcat'
                    """
                }
            }
        }
    }

    post {
        success {
            script {
                sendEmail("SUCCESS", env.recipients)
                slackNotification("SUCCESS")
            }
        }

        failure {
            script {
                sendEmail("FAILURE", env.recipients)
                slackNotification("FAILURE")
            }
        }
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
