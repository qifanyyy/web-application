all: clean build deploy

clean:
	sh /home/ubuntu/tomcat/bin/shutdown.sh
	rm -rf ./target
	rm -f "/home/ubuntu/tomcat/webapps/ROOT.war"

build:
	git reset --hard HEAD
	git pull
	python minify.py --deploy
	mvn package

deploy: target/ROOT.war
	rm -rf "/home/ubuntu/tomcat/webapps/ROOT"
	cp ./target/ROOT.war "/home/ubuntu/tomcat/webapps/ROOT.war"
	sh /home/ubuntu/tomcat/bin/startup.sh
