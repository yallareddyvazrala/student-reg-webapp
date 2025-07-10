node {
   
   def mavenHome = tool name: 'Maven-3.9.10', type: 'maven'
   

    options {
        buildDiscarder (numToKeepStr: '5')
    }
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
 
}