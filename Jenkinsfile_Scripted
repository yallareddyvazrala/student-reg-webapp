node {
   
   def mavenHome = tool name: 'Maven-3.9.10', type: 'maven'
   
   try {
    
        stage("Git Clone") {
            git branch: 'development', credentialsId: 'GitHubCred', url: 'https://github.com/Rushi-Technologies/student-reg-webapp.git'
        }
        
        stage("Maven Verify And Sonar Scan") {
            sh "${mavenHome}/bin/mvn clean package"
            sh "${mavenHome}/bin/mvn clean verify sonar:sonar"
        }
        
        stage("Maven Deploy") {
            sh "${mavenHome}/bin/mvn clean deploy"
        }
        
        stage("Stop Tomcat Service") {
            sshagent(['Tomcat_Server']){
                sh """
                echo Stoping the tomcat process" 
                ssh -o StrictHostKeyChecking=no ec2-user@172.31.21.97 sudo systemtctl stop tomcat" 
                sleep 10
                """
            }
        }
        
        stage("Deploy War File To  Tomcat") {
                sshagent(['Tomcat_Server']) {
                    sh "scp -o StrictHostKeyChecking=no target/student-reg-webapp.war ec2-user@172.31.21.97:/opt/tomcat/webapps/student-reg-webapp.war"
                }
        }
        
        stage("Start Tomcat") {
            sshagent(['Tomcat_Server']) {
                sh """
                    echo Starting the tomcat process
                    ssh -o StrictHostKeyChecking=no ec2-user@172.31.21.97 sudo systemctl start tomcat 
                    """
            }
        }

  } catch (err) {
        echo "An error occurred: ${e.getMessage()}"
        currentBuild.result = 'FAILURE'
    } finally {
        def buildStatus = currentBuild.result ?: 'SUCCESS'
        def colorcode = buildStatus == 'SUCCESS' ? 'good' : 'danger'
        slackSend (channel: 'lic-appteam', color: "${colorcode}", message: "Build - ${buildStatus} : ${env.JOB_NAME} #${env.BUILD_NUMBER} - URL: ${env.BUILD_URL}")
        sendEmail(
           "${env.JOB_NAME} - ${env.BUILD_NUMBER} - Build ${buildStatus}",
           "Build ${buildStatus}. Please check the console output at ${env.BUILD_URL}",
           'balajireddy.urs@gmail.com' )
    }
 
}

def sendEmail(String subject, String body, String recipient) {
    emailext(
        subject: subject,
        body: body,
        to: recipient,
        mimeType: 'text/html'
    )
}