#! /usr/bin/env bash

## Helper functions to be imported in scripts.

# is_openshift returns 0 if cluster appears to be OpenShift
function is_openshift() {
    set +e
    kubectl get namespaces openshift-operators &> /dev/null
    if [[ $? != 0 ]]; then
        return 1
    fi
    return 0
}

function await_resource_ready {
    local resource_name=${1}

    ready=1
    while [[ ${ready} != 0 ]]; do
        echo "INFO: awaiting readiness of operator resource ${resource_name}"
        kubectl api-resources | grep ${resource_name} &> /dev/null
        ready=$?
        if [[ ${ready} != 0 ]]; then sleep 2; else echo "INFO: operator resource ready"; fi
    done
}

function ensure_resource_exists {
    local resource_name=${1}

    await_resource_ready "${resource_name}"
}

function render_yaml {
    local directory=${1:-'.'}

    echo "INFO: rendering env vars in manifests"
    for file in $(find ${directory} -type f -iname '*.tpl'); do 
        echo "DEBUG: rendering ${file} to ${file%%'.tpl'}"
        cat "${file}" | envsubst > "${file%%'.tpl'}"
    done
}

function apply_kustomize_dir {
    local directory=${1:-'.'}

    echo "INFO: rendering and applying kustomize dir ${directory}"
    if [[ -e "${directory}/.env" ]]; then
        echo "INFO: sourcing .env found in kustomize dir"
        set -o allexport
        source ${directory}/.env
        set +o allexport
    fi
    render_yaml "${directory}"
    kustomize build ${directory} | kubectl apply -f -
}

function ensure_namespace {
    local namespace=${1}
    local change_context=${2:-''}

    kubectl get namespace ${namespace} &> /dev/null
    result=$?
    if [[ ${result} != 0 ]]; then
        echo "INFO: namespace ${namespace} not found, attempting to create"
        kubectl create namespace --save-config ${namespace}
        result=$?
    fi
    if [[ ${result} == 0 && -n "${change_context}" ]]; then
        echo "INFO: switching context to namespace ${namespace}"
        kubectl config set-context --current --namespace ${namespace}
        result=$?
    fi
    return ${result}
}

# ensure_helm_repo ensures the repo name exists
# if it doesn't exist it adds it with specified URL
function ensure_helm_repo {
    local name=${1}
    local url=${2}

    repos=($(helm repo list -o json | jq -r '.[].name'))
    echo "${repos[@]}" | grep -wq "${name}"
    result=$?
    if [[ ${result} == 0 ]]; then
        echo "INFO: repo named \"${name}\" already exists"
        helm repo update
        return
    fi
    helm repo add --force-update "${name}" "${url}"
}

function ensure_argocd {
    local argocd_namespace=${1:-openshift-gitops}
    local argocd_deployment_name=${2:-openshift-gitops-server}

    kubectl get deployments -n ${argocd_namespace} ${argocd_deployment_name} &> /dev/null
    if [[ $? != 0 ]]; then
        echo "ERROR: ArgoCD is not installed, cannot deploy Argo app"
        return 2
    fi
}