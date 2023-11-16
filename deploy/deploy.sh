#! /usr/bin/env bash

this_dir=$(cd $(dirname ${BASH_SOURCE[0]}) && pwd)
root_dir=$(cd ${this_dir}/.. && pwd)
if [[ -e "${root_dir}/.env" ]]; then source ${root_dir}/.env; fi
if [[ -e "${this_dir}/.env" ]]; then source ${this_dir}/.env; fi
source ${this_dir}/lib/kubernetes.sh

namespace=spring-apiserver
ensure_namespace ${namespace} true

if is_openshift; then
    echo "INFO: building for openshift"
    kustomize build ${this_dir}/openshift | kubectl apply -n ${namespace} -f -
else
    echo "INFO: building for kubernetes"
    kustomize build ${this_dir}/kubernetes | kubectl apply -n ${namespace} -f -
fi
