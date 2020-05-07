all: clean build deploy

clean:
	sh /home/ubuntu/tomcat/bin/shutdown.sh
	rm -rf ./target
	rm -rf "/home/ubuntu/tomcat/webapps/ROOT"
	rm -f "/home/ubuntu/tomcat/webapps/ROOT.war"

build:
	git reset --hard HEAD
	git pull
	python minify.py --deploy
	mvn package

deploy: target/cs122b-server.war
	rm -rf "/home/ubuntu/tomcat/webapps/ROOT"
	cp ./target/cs122b-server.war "/home/ubuntu/tomcat/webapps/ROOT.war"
	sh /home/ubuntu/tomcat/bin/startup.sh
