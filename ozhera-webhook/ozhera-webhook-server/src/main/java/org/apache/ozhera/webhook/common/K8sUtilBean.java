/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ozhera.webhook.common;

import com.google.gson.Gson;
import com.xiaomi.youpin.docean.anno.Component;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ParameterNamespaceListVisitFromServerGetDeleteRecreateWaitApplicable;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author shanwb
 * @date 2023-02-24
 */
@Component
@Slf4j
public class K8sUtilBean {
    private Gson gson = new Gson();
    @javax.annotation.Resource
    private KubernetesClient kubernetesClient;


    public void applyYaml(String yaml, String namespace, String action) {
        log.warn("applyYaml action:{}, ply yaml:{}", action, yaml);
        try (InputStream inputStream = new ByteArrayInputStream(yaml.getBytes("UTF-8"))) {
            ParameterNamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata> metaDataes = kubernetesClient.load(inputStream);
            List<HasMetadata> hasMetadataList = metaDataes.get();

            for (HasMetadata hasMetadata : hasMetadataList) {
                if (hasMetadata instanceof io.fabric8.kubernetes.api.model.Service heraService) {
                    if ("delete".equals(action)) {
                        kubernetesClient.services().inNamespace(namespace).delete(heraService);
                    } else {
                        kubernetesClient.services().inNamespace(namespace).createOrReplace(heraService);
                    }
                    continue;
                }

                if (hasMetadata instanceof io.fabric8.kubernetes.api.model.PersistentVolume heraPv) {
                    if ("delete".equals(action)) {
                        kubernetesClient.persistentVolumes().delete(heraPv);
                    } else {
                        kubernetesClient.persistentVolumes().createOrReplace(heraPv);
                    }
                    continue;
                }

                if (hasMetadata instanceof io.fabric8.kubernetes.api.model.storage.StorageClass heraStorageClass) {
                    if ("delete".equals(action)) {
                        kubernetesClient.storage().storageClasses().delete(heraStorageClass);
                    } else {
                        kubernetesClient.storage().storageClasses().createOrReplace(heraStorageClass);
                    }
                    continue;
                }

                if (hasMetadata instanceof io.fabric8.kubernetes.api.model.PersistentVolumeClaim heraPvc) {
                    if ("delete".equals(action)) {
                        kubernetesClient.persistentVolumeClaims().inNamespace(namespace).delete(heraPvc);
                    } else {
                        kubernetesClient.persistentVolumeClaims().inNamespace(namespace).createOrReplace(heraPvc);
                    }
                    continue;
                }


                if (hasMetadata instanceof io.fabric8.kubernetes.api.model.rbac.ClusterRoleBinding heraClusterRoleBinding) {
                    if ("delete".equals(action)) {
                        kubernetesClient.rbac().clusterRoleBindings().delete(heraClusterRoleBinding);
                    } else {
                        kubernetesClient.rbac().clusterRoleBindings().createOrReplace(heraClusterRoleBinding);
                    }
                    continue;
                }

                if (hasMetadata instanceof io.fabric8.kubernetes.api.model.rbac.ClusterRole heraClusterRole) {
                    if ("delete".equals(action)) {
                        kubernetesClient.rbac().clusterRoles().delete(heraClusterRole);
                    } else {
                        kubernetesClient.rbac().clusterRoles().createOrReplace(heraClusterRole);
                    }
                    continue;
                }

                if (hasMetadata instanceof io.fabric8.kubernetes.api.model.ServiceAccount heraServiceAccount) {
                    if ("delete".equals(action)) {
                        kubernetesClient.serviceAccounts().delete(heraServiceAccount);
                    } else {
                        kubernetesClient.serviceAccounts().createOrReplace(heraServiceAccount);
                    }
                    continue;
                }

                if (hasMetadata instanceof io.fabric8.kubernetes.api.model.ConfigMap heraConfigMap) {
                    if ("delete".equals(action)) {
                        kubernetesClient.configMaps().inNamespace(namespace).delete(heraConfigMap);
                    } else {
                        kubernetesClient.configMaps().inNamespace(namespace).createOrReplace(heraConfigMap);
                    }
                    continue;
                }

                if (hasMetadata instanceof io.fabric8.kubernetes.api.model.apps.DaemonSet heraDaemonSet) {
                    if ("delete".equals(action)) {
                        kubernetesClient.apps().daemonSets().delete(heraDaemonSet);
                    } else {
                        kubernetesClient.apps().daemonSets().createOrReplace(heraDaemonSet);
                    }
                    continue;
                }

                if (hasMetadata instanceof io.fabric8.kubernetes.api.model.apps.StatefulSet heraStatefulSet) {
                    if ("delete".equals(action)) {
                        kubernetesClient.apps().statefulSets().delete(heraStatefulSet);
                    } else {
                        kubernetesClient.apps().statefulSets().createOrReplace(heraStatefulSet);
                    }
                    continue;
                }

                if (hasMetadata instanceof io.fabric8.kubernetes.api.model.certificates.v1.CertificateSigningRequest heraCertificateSigningRequest) {
                    if ("delete".equals(action)) {
                        kubernetesClient.certificates().v1().certificateSigningRequests().delete(heraCertificateSigningRequest);
                    } else {
                        kubernetesClient.certificates().v1().certificateSigningRequests().createOrReplace(heraCertificateSigningRequest);
                    }
                    continue;
                }

                if (hasMetadata instanceof io.fabric8.kubernetes.api.model.admissionregistration.v1.MutatingWebhookConfiguration heraMutatingWebhookConfiguration) {
                    if ("delete".equals(action)) {
                        kubernetesClient.admissionRegistration().v1().mutatingWebhookConfigurations().delete(heraMutatingWebhookConfiguration);
                    } else {
                        kubernetesClient.admissionRegistration().v1().mutatingWebhookConfigurations().createOrReplace(heraMutatingWebhookConfiguration);
                    }
                    continue;
                }

                log.warn("not support k8s kind:{}, yaml:{}", hasMetadata.getKind(), gson.toJson(hasMetadata.getMetadata()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
