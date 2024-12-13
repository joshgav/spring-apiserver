#! /usr/bin/env bash

this_dir=$(cd $(dirname ${BASH_SOURCE[0]}) && pwd)
root_dir=$(cd ${this_dir}/.. && pwd)
if [[ -e "${root_dir}/.env" ]]; then source ${root_dir}/.env; fi
if [[ -e "${this_dir}/.env" ]]; then source ${this_dir}/.env; fi
source ${this_dir}/lib/kubernetes.sh

app_namespace=${1:-spring-apiserver}
git_repo_name=${app_namespace}
ensure_namespace ${app_namespace} true

oc get secret quay-pull-secret &> /dev/null
if [[ $? == 0 ]]; then oc delete secret quay-pull-secret; fi
oc create secret docker-registry quay-pull-secret --namespace=${app_namespace} \
    --docker-server="${QUAY_HOSTNAME}" \
    --docker-username="${QUAY_USERNAME}" \
    --docker-password="${QUAY_PASSWORD}"

for sa in "default" "pipeline"; do
    oc patch --type strategic serviceaccount ${sa} --namespace ${app_namespace} \
        --patch '{"secrets": [{"name": "quay-pull-secret"}], "imagePullSecrets": [{"name": "quay-pull-secret"}]}'
done

oc get secret argocd-env-secret &> /dev/null
if [[ $? == 0 ]]; then oc delete secret argocd-env-secret; fi
oc create secret generic argocd-env-secret --namespace ${app_namespace} \
    --from-literal="ARGOCD_AUTH_TOKEN=${ARGOCD_AUTH_TOKEN}"

oc get configmap argocd-env-configmap &> /dev/null
if [[ $? == 0 ]]; then oc delete configmap argocd-env-configmap; fi
oc create configmap argocd-env-configmap --namespace ${app_namespace} \
    --from-literal="ARGOCD_SERVER=openshift-gitops-server.openshift-gitops.svc"

oc adm policy add-cluster-role-to-user --serviceaccount pipeline 'applications.argoproj.io-v1alpha1-admin'

oc apply -f - <<EOF
    apiVersion: pipelinesascode.tekton.dev/v1alpha1
    kind: Repository
    metadata:
        name: github-${git_repo_name}
        namespace: ${app_namespace}
    spec:
        url: 'https://github.com/joshgav/${git_repo_name}'
EOF
