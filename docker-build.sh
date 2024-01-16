#!/bin/bash
# This script builds the project and creates a Docker image.
mvn clean package -DskipTests=true
# docker login info
USER=test
PWD=test123
image_name=test-pub

# project tag
#ozhera-log
logAgentTag=log-agent-release-0.0.1
logAgentServerTag=tag=log-agent-server-release-0.0.1
logManagerTag=log-manager-release-0.0.1
logStreamTag=log-stream-release-0.0.1
#ozhera-app
appServerTag=app-server-release-0.0.1
#ozhera-demo-client
demoClientTag=demo-client-server-release-0.0.1
#ozhera-demo-server
demoServerTag=demo-server-release-0.0.1
#ozhera-monitor
monitorTag=monitor-release-0.0.1
#ozhera-operator
operatorTag=operator-release-0.0.1
#ozhera-prometheus-agent
prometheusAgentTag=prometheus-agent-release-0.0.1
#ozhera-webhook
webhookTag=webhook-release-0.0.1
#trace-etl
traceEtlTag=trace-etl-release-0.0.1

docker login --username=$USER --password=$PWD

directories=($(find . -type f -name "Dockerfile" -exec dirname {} \;))

# loop through each found dockerfile
for dir in "${directories[@]}"; do

    echo "current Directory:" $dir
    # extract filename without path
    dir_name=$(basename "$dir")
    echo "dir_name:" $dir_name

     # If the directory is named "classes", skip and continue with the next
    if [ "$dir_name" = "classes" ]; then
        continue
    fi

    # If the directory is named "resources", skip and continue to the next
    if [ "$dir_name" = "resources" ]; then
        continue
    fi

    # set specific tags
    tag=""
    case "$dir_name" in
            "log-agent")
                tag=$logAgentTag
                ;;
            "log-agent-server")
                tag=$logAgentServerTag
                ;;
            "log-manager")
                tag=$logManagerTag
                ;;
            "log-stream")
                tag=$logStreamTag
                ;;
            "app-server")
                  tag=$appServerTag
                  ;;
            "ozhera-demo-client-server")
                tag=$demoClientTag
                ;;
            "ozhera-demo-server-server")
                tag=$demoServerTag
                ;;
            "ozhera-monitor")
                tag=$monitorTag
                ;;
            "ozhera-operator")
                tag=$operatorTag
                ;;
            "ozhera-prometheus-agent")
                tag=$prometheusAgentTag
                ;;
            "ozhera-webhook")
                tag=$webhookTag
                ;;
            "trace-etl")
                tag=$traceEtlTag
                ;;
            *)
            tag="latest"
            ;;
    esac
    image=$USER/$image_name:$tag
    # print directory paths and labels for settings
    echo "Directory: $dir, image: $image"
    pushd $dir > /dev/null
    # execute docker build
    docker build -t $image .

    echo "Image path:" $image

    docker push $image
    popd > /dev/null
done
