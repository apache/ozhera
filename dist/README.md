## 1. Build the project
You can use the following command to build the project from the source code
mvn clean install -Papache-release -Dmaven.test.skip=true

## 2. Resource usage instructions
The binary tarball provides executable jar files according to the directory hierarchy, and comes with the corresponding Dockerfile
You can use the corresponding jar package and the corresponding Dockerfile to build the image.

## 3. Build the image
The corresponding executable jar package and the corresponding Dockerfile are provided in each directory. You need to switch to the directory corresponding to the Dockerfile first, and execute the command:
build . --build-arg SRC_PATH=. --build-arg APP_VERSION=2.2.5-incubating -t ${DOCKER_USERNAME}/${REPOSITORY_NAME}:${TAG}

Note: Because the environment variables SRC_PATH and APP_VERSION are used in Dockerfile, the corresponding parameter values ​​need to be specified when building the image
SRC_PATH: The path where the executable jar package is located. If you have switched to the directory corresponding to the Dockerfile, the executable jar package and the Dockerfile are in the same directory, so you can specify the current directory.
APP_VERSION: This version is 2.2.5-incubating

Example:
cd ozhera-app/app-server
docker build . --build-arg SRC_PATH=./target --build-arg APP_VERSION=2.2.5-incubating -t herahub/opensource-pub:app-server-2.2.5-incubating-beta-v1

## 4. How to deploy the project

During the startup process, OzHera's services rely on many basic services, including: mysql, Nacos, elasticsearch, Redis, RocketMQ, grafana, prometheus, alertManager, node-exporter, Cadvisor, etc.;
In addition, the configuration items of each module and the dependencies between projects make it a very complicated process to manually start each application. Therefore, OzHera
provides an operator that is convenient for users to use. You can follow the instructions in the following link to make your deployment process simple and easy.

For the deployment process, you need to refer to the following documentation
https://ozhera.apache.org/zh/docs/deployment.html
https://ozhera.apache.org/en/docs/deployment.html

Of course, before using the operator, you need to build the image of the corresponding service module under OzHera for the operator to use during the deployment process.

## 5. How to build executable jar files from source code
All executable jar files can be obtained through source code building;
You can execute the following command in the root directory of the project (ozhera) or the directory where the submodule is located to obtain the corresponding executable jar package:
mvn clean install -Papache-release -Dmaven.test.skip=true
Note: The parameter -Papache-release must be added here

### Build environment requirements
You need to rely on the environment to build the project locally:
maven3 and jdk21