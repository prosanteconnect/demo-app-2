job "psc-context-sharing-demo-app-2" {
  datacenters = ["${datacenter}"]
  type = "service"
  namespace = "${nomad_namespace}"

  vault {
    policies = ["psc-ecosystem"]
    change_mode = "restart"
  }

  affinity {
    attribute = "$\u007Bnode.class\u007D"
    value = "standard"
  }

  group "demo_app_2" {
    count = "1"
    restart {
      attempts = 3
      delay = "60s"
      interval = "1h"
      mode = "fail"
    }

    network {
      port "http" {
        to = 8080
      }
    }

    task "demo_app_2" {
      driver = "docker"

      config {
        image = "${artifact.image}:${artifact.tag}"
        ports = ["http"]
      }

      # env variables
      template {
        destination = "local/file.env"
        env = true
        data = <<EOH
PUBLIC_HOSTNAME={{ with secret "psc-ecosystem/${nomad_namespace}" }}{{ .Data.data.demo_app_2_public_hostname }}{{ end }}
JAVA_TOOL_OPTIONS="-Xms256m -Xmx1g -XX:+UseG1GC -Dspring.config.location=/secrets/application.properties -Dlogging.level.fr.ans.psc=${log_level}"
EOH
      }

      # application.properties
      template {
        destination = "secrets/application.properties"
        change_mode = "restart"
        data = <<EOF
psc.context.sharing.api.url=http://{{ range service "${nomad_namespace}-psc-context-sharing"}}{{ .Address }}:{{ .Port }}{{ end }}/psc-context-sharing/api/share

{{ with secret "psc-ecosystem/${nomad_namespace}" }}
spring.security.oauth2.client.registration.prosanteconnect.clientId={{ .Data.data.psc_client_id}}
spring.security.oauth2.client.registration.prosanteconnect.clientSecret={{ .Data.data.psc_client_secret}}
spring.security.oauth2.client.registration.prosanteconnect.provider=prosanteconnect
spring.security.oauth2.client.registration.prosanteconnect.authorization-grant-type=authorization_code
spring.security.oauth2.client.registration.prosanteconnect.client-name=prosanteconnect
spring.security.oauth2.client.registration.prosanteconnect.redirect-uri=https://{{ .Data.data.demo_app_2_public_hostname }}/login/oauth2/code/prosanteconnect
spring.security.oauth2.client.registration.prosanteconnect.scope=scope_all
spring.security.oauth2.client.registration.prosanteconnect.client-authentication-method=client_secret_post

spring.security.oauth2.client.provider.prosanteconnect.authorization-uri=https://wallet.bas.psc.esante.gouv.fr/auth
spring.security.oauth2.client.provider.prosanteconnect.token-uri=https://auth.bas.psc.esante.gouv.fr/auth/realms/esante-wallet/protocol/openid-connect/token
spring.security.oauth2.client.provider.prosanteconnect.user-info-uri=https://auth.bas.psc.esante.gouv.fr/auth/realms/esante-wallet/protocol/openid-connect/userinfo
spring.security.oauth2.client.provider.prosanteconnect.user-name-attribute=preferred_username
spring.security.oauth2.client.provider.prosanteconnect.jwk-set-uri=https://auth.bas.psc.esante.gouv.fr/auth/realms/esante-wallet/protocol/openid-connect/certs
{{ end }}
EOF
      }

      resources {
        cpu = 500
        memory = 1152
      }

      service {
        name = "$\u007BNOMAD_NAMESPACE\u007D-$\u007BNOMAD_JOB_NAME\u007D"
        tags = ["urlprefix-$\u007BPUBLIC_HOSTNAME\u007D"]
        port = "http"
        check {
          type = "http"
          path = "/insecure/check"
          port = "http"
          interval = "30s"
          timeout = "2s"
          failures_before_critical = "3"
        }
      }
    }
  }
}
