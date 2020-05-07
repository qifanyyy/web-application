TOM_CAT_HOME=/home/ubuntu/tomcat

all: clean build deploy

clean:
	${TOM_CAT_HOME}/bin/shutdown.sh
	rm -rf ./target
	rm -rf "${TOM_CAT_HOME}/webapps/ROOT"
	rm -f "${TOM_CAT_HOME}/webapps/ROOT.war"

build:
	git reset --hard HEAD
	git pull
	python minify.py --deploy
	mvn package

deploy: target/ROOT.war
	rm -rf "${TOM_CAT_HOME}/webapps/ROOT"
	cp ./target/ROOT.war "${TOM_CAT_HOME}/webapps/ROOT.war"
	${TOM_CAT_HOME}/bin/startup.sh