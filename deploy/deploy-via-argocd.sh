#! /usr/bin/env bash

this_dir=$(cd $(dirname ${BASH_SOURCE[0]}) && pwd)
root_dir=$(cd ${this_dir}/../.. && pwd)
if [[ -e "${root_dir}/.env" ]]; then source ${root_dir}/.env; fi
if [[ -e "${this_dir}/.env" ]]; then source ${this_dir}/.env; fi
source ${this_dir}/lib/kubernetes.sh

argocd_namespace=openshift-gitops
app_namespace=spring-apiserver
ensure_argocd ${argocd_namespace}
ensure_namespace ${app_namespace}

kubectl apply -n ${argocd_namespace} -f ${this_dir}/argocd-application.yaml
