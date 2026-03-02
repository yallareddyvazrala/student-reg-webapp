@Library('JenkinsSharedLib') _
pipeline {
   
   agent any

   tools {
       maven 'Maven-3.9.12'
   }

   triggers{
      githubPush()
   }

  options {
      disableConcurrentBuilds()
      timeout(5)
      buildDiscarder logRotator(daysToKeepStr: '5', numToKeepStr: '10')
   }


    environment {
        SONARQUBE_TOKEN = credentials('SonarQubeToken')
        SONARQUBE_HOST_URL = 'http://172.31.6.94:9000'
    }

    stages {
      
        stage('Maven Build') {
            steps {
                sh "mvn clean package"
            }
        }
        stage('SonarQube Analysis') {
            steps {
               sh "mvn sonar:sonar -Dsonar.url=${SONARQUBE_HOST_URL} -Dsonar.token=${SONARQUBE_TOKEN}"
            }
        }
        stage('Upload War to Nexus') {
            steps {
                sh "mvn clean deploy"
            }
        }

    
        stage('Deploy to Dev') {

            when {
                expression { env.BRANCH_NAME == "development" }
            } 
            environment {
                 TOMCAT_SERVER_IP = "172.31.11.157"
            }
            steps {
               sshagent (credentials: ['Tomcat_SSH_Cred']) {
                    sh "ssh -o StrictHostKeyChecking=no ec2-user@${TOMCAT_SERVER_IP} sudo systemctl stop tomcat"
                    sh "ssh -o StrictHostKeyChecking=no ec2-user@${TOMCAT_SERVER_IP} rm -f /opt/tomcat/webapps/student-reg-webapp.war"
                    sh "sleep 10"
                    sh "scp -o StrictHostKeyChecking=no target/student-reg-webapp.war ec2-user@${TOMCAT_SERVER_IP}:/opt/tomcat/webapps/student-reg-webapp.war"
                    sh "ssh -o StrictHostKeyChecking=no ec2-user@${TOMCAT_SERVER_IP} sudo systemctl start tomcat"
                }
            }
       }

       stage('Deploy to Prod') {

            when {
                branch 'main'
            } 
           
            steps {
                // Commands to Deploy Prod
                sh "Deployoing Prod"
            }
       }
   }
  
   post {
    always {
       cleanWs()
    }
    success {

            sendEmailNotifications(currentBuild.currentResult,"balajireddy.urs@gmail.com")
            sendSlackNotifications("lic-app-team",currentBuild.currentResult)
      
    }
    failure {
            sendEmailNotifications(currentBuild.currentResult,"balajireddy.urs@gmail.com")
            sendSlackNotifications("lic-app-team",currentBuild.currentResult)
     
    }

   }
}