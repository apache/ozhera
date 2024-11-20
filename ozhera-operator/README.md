<!--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

-->
# OzHeraOperator Overview
OzHeraOperator is used to deploy the entire Apache OzHera(incubating) system with one click.

# Environment Requirements
+ k8s cluster
+ Better to have LB enabled. If in local test environment, NodePort will be used by default.
+ Must have k8s privileges to create ns, crd, service, deployment, pv.

# Operation Steps
Please switch to ozhera-operator/ozhera-operator-server/src/main/resources/operator directory
## Create Account and Namespace
kubectl apply -f ozhera_operator_auth.yaml

## Create ozhera CRD
kubectl apply -f ozhera_operator_crd.yaml

## Deploy Operator
kubectl apply -f ozhera_operator_deployment.yaml
Please note: the operator image. If you need to build the image from the source code yourself, please modify the image address in the yaml file.

## Operator UI Operation to Complete OzHera Deployment
Find the access address of ozhera, the specific service name is in the ozhera_operator_deployment.yaml
kubectl get service -n=ozhera-namespace

After finding it, access the operator UI address in the browser and deploy according to the UI operation.

# Appendix
+ Some k8s operations (for convenient testing)
  + Initialize custom resources
  + kubectl apply -f mone_bootstrap_crd.yaml
  + View created custom resources
  + kubectl get monebootstraps
  + Delete a specific custom resource
  + kubectl delete monebootstraps example-podset
  + Delete a deployment
  + kubectl delete deployment nginx-deployment
  + View all deployments
  + kubectl get deployments
+ In this test case, it is actually installing 2 deployments and two services.
+ Test after installation: curl http://localhost:31080/
+ Pull mysql 5.7 image
+ docker pull mysql:5.7  (for arm64 m1: docker pull mariadb:latest)
+ Execute command in node
+ kubectl exec mysql-deployment-7b65855b66-9qhp7 - mysql -uroot -pMone!123456 -e "select 1"
+ Execute command in sidecar
+ kubectl exec -it redis-deployment-74667fdc89-727kp -c toolbox
