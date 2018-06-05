mvn clean package -P assemble -Dmaven.javadoc.skip=true -Dmaven.test.skip=true

cd target
java -jar client-app-0.0.1-SNAPSHOT-spring-boot.jar
