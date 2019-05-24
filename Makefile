clean:
	mvn clean

fmt:
	mvn process-sources -P format

test:
	mvn test -Dmaven.test.skip=false

build:
	mvn package

verify:
	mvn verify -P oss

~/.m2/settings.xml:
	curl -fSL https://dl.bintray.com/subchen/download/maven/subchen-maven-deploy.tar.gz.enc -O
	openssl aes-256-cbc -k "$(MAVEN_DEPLOY_SECRET_PASSWD)" -in subchen-maven-deploy.tar.gz.enc -out subchen-maven-deploy.tar.gz -d
	tar -zxvf subchen-maven-deploy.tar.gz -C ~/
	rm -f subchen-maven-deploy.tar.gz*

release: ~/.m2/settings.xml
	mvn clean deploy -P oss -DautoReleaseAfterClose=true
