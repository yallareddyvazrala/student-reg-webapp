FROM tomcat:9.0-jdk11
COPY target/student-reg-webapp.war /usr/local/tomcat/webapps/student-reg-webapp.war