TOM_CAT_HOME="/home/ubuntu/tomcat"

build: pom.xml
	git pull
	rm -rf ./target
	mvn package

deploy: target/cs122b-server.war
	rm -rf "${TOM_CAT_HOME}/webapps/cs122b-server"
	cp ./target/cs122b-server.war "${TOM_CAT_HOME}/webapps/cs122b-server.war"
