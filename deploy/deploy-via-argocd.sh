#! /usr/bin/env bash

this_dir=$(cd $(dirname ${BASH_SOURCE[0]}) && pwd)
root_dir=$(cd ${this_dir}/../.. && pwd)
if [[ -e "${root_dir}/.env" ]]; then source ${root_dir}/.env; fi
if [[ -e "${this_dir}/.env" ]]; then source ${this_dir}/.env; fi
source ${this_dir}/lib/kubernetes.sh

argocd_namespace=openshift-gitops
argocd_deployment=openshift-gitops-server
kustomize_path=base

kubectl get deployments -n ${argocd_namespace} ${argocd_deployment} &> /dev/null
if [[ $? == 0 ]]; then
    echo "INFO: found ArgoCD, deploying application"
    export argocd_namespace kustomize_path
    cat ${this_dir}/argocd-application.yaml | envsubst | kubectl apply -f -
else
    echo "ERROR: ArgoCD is not installed, cannot deploy Argo app"
    exit 2
fi
