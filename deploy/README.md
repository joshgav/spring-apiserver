# Delivery configuration

There are a few ways to set things up for now:

- preferred: wire up Tekton Pipelines as Code and let it configure and sync the component as in the [../.tekton](../.tekton/) dir
- `./deploy-via-argocd.sh` to install an argocd Application
- `./deploy.sh` to deploy directly from this directory without using ArgoCD

For now the following configuration must be set up manually in the app's namespace (default: `spring-apiserver`).

In addition you must create a Repository resource in the spring-apiserver namespace.

## ClusterRoleBinding

- `pipeline` service account requires `applications.argoproj.io-v1alpha1-admin` cluster role

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: applications.argoproj.io-v1alpha1-admin
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: applications.argoproj.io-v1alpha1-admin
subjects:
- kind: ServiceAccount
  name: pipeline
  namespace: ${app_namespace}
```

## Pull Secrets

- `pipeline` service account requires access to secrets `quay-pull-secret` and `redhat-pull-secret`

```yaml
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: pipeline
  namespace: ${app_namespace}
secrets:
- name: quay-pull-secret
- name: redhat-pull-secret
imagePullSecrets:
- name: quay-pull-secret
- name: redhat-pull-secret
---
kind: Secret
apiVersion: v1
metadata:
  name: quay-pull-secret
  namespace: spring-apiserver
data:
  .dockerconfigjson: >-
    yourbase64encodedconfigjsonhere
type: kubernetes.io/dockerconfigjson
---
kind: Secret
apiVersion: v1
metadata:
  name: redhat-pull-secret
  namespace: spring-apiserver
data:
  .dockerconfigjson: >-
    yourbase64encodedconfigjsonhere
type: kubernetes.io/dockerconfigjson
```

## for argocd-task-sync-and-wait

- the `argocd-task-async-and-wait` Tekton task requires these values as env vars
- TODO: is there a better way to do this?

```yaml
---
kind: Secret
apiVersion: v1
metadata:
  name: argocd-env-secret
  namespace: ${app_namespace}
data:
  ARGOCD_AUTH_TOKEN: >-
    yourbase64encodedargocdauthtokenhere
type: Opaque
---
kind: ConfigMap
apiVersion: v1
metadata:
  name: argocd-env-configmap
  namespace: ${app_namespace}
data:
  # this is the default for OpenShift Pipelines
  ARGOCD_SERVER: openshift-gitops-server.openshift-gitops.svc
```yaml
