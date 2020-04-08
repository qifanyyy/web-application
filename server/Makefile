TOM_CAT_HOME=/home/ubuntu/tomcat

all:
	clean
	build
	deploy

clean:
	${TOM_CAT_HOME}/bin/shutdown.sh
	rm -rf ./target
	rm -rf "${TOM_CAT_HOME}/webapps/cs122b-server"

build: pom.xml
	git pull
	mvn package

deploy: target/cs122b-server.war
	rm -rf "${TOM_CAT_HOME}/webapps/cs122b-server"
	cp ./target/cs122b-server.war "${TOM_CAT_HOME}/webapps/cs122b-server.war"
	${TOM_CAT_HOME}/bin/startup.sh
