default: verify

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

install:
	mvn install

deploy:
	mvn clean deploy -P oss -DautoReleaseAfterClose=true
