node {
    try {  
        def maven_home = tool name: 'Maven-3.9.12', type: 'maven'
        def tomcatServerIP="172.31.11.157"
        def tomcatServerSSHUsername="ec2-user"
        stage("Git Clone") {
            git branch: 'development', credentialsId: 'GitHub_Credentails', url: 'https://github.com/Rushi-Technologies/student-reg-webapp.git'
        }
        
        stage("Maven Pacakge"){
            sh "echo ${maven_home}"
            sh "${maven_home}/bin/mvn clean package"
        }
        
        stage("SonarScan"){
            withCredentials([string(credentialsId: 'SonarQubeToken', variable: 'SonarToken')]) {
                sh "${maven_home}/bin/mvn clean verify sonar:sonar -Dsonar.token=${SonarToken}"
            } 
        }
        
        stage("Upload War File To Nexus"){
            sh "${maven_home}/bin/mvn clean deploy"
        }
        
        stage("Deploy War file to tomcat"){
            sshagent(['Tomcat_SSH_Cred']) {
                sh """
                    ssh -o StrictHostKeyChecking=no ${tomcatServerSSHUsername}@${tomcatServerIP} sudo systemctl stop tomcat
                    sleep 20

                    ssh -o StrictHostKeyChecking=no ${tomcatServerSSHUsername}@${tomcatServerIP} rm /opt/tomcat/webapps/student-reg-webapp.war
                
                    scp -o StrictHostKeyChecking=no target/student-reg-webapp.war ${tomcatServerSSHUsername}@${tomcatServerIP}:/opt/tomcat/webapps/student-reg-webapp.war
                
                    ssh -o StrictHostKeyChecking=no ${tomcatServerSSHUsername}@${tomcatServerIP} sudo systemctl start tomcat
                    """
                
            }
            
        }
        
    } catch (Exception e) {
       currentBuild.result = 'FAILURE'
       
    } finally {
        def buildStatus = currentBuild.result ?: 'SUCCESS'
        def colorcode = 'good'
        
        if (buildStatus == "FAILURE"){
            colorcode = 'danger'
        }
        
        slackSend channel: 'lic-app-team', color: "${colorcode}"  , message: "Jenkins Job ${env.JOB_NAME} - ${env.BUILD_NUMBER} - ${buildStatus}. Please check the Jenkins console output for details. ${env.BUILD_URL}"

        emailext body:  """
                    <html>
                    <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                        <h2 style="color: #2d87f0;">Jenkins Build Notification</h2>
                        <p><strong>Build Result:</strong> ${buildStatus}</p>
                        <p><strong>Build Number:</strong> ${env.BUILD_NUMBER}</p>
                        <p><strong>Project:</strong> ${env.JOB_NAME}</p>
                        <p><strong>Build URL:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                        <p>For more details, please visit the Jenkins job page.</p>
                    </body>
                    </html>
                    """, 
        subject: "${env.JOB_NAME} - ${env.BUILD_NUMBER} - ${buildStatus}", 
        to: 'balajireddy.urs@gmail.com,rushitechnologiesbanglore@gmail.com',
        mimeType: 'text/html'
    }
}    