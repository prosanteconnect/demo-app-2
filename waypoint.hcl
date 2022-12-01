project = "${workspace.name}/share_context"

labels = {
  "domaine" = "psc"
}

runner {
  enabled = true
  profile = "sharecontext-${workspace.name}"
  data_source "git" {
    url = "https://github.com/prosanteconnect/demo-app-2.git"
    ref = "${workspace.name}"
  }
  poll {
    enabled = false
  }
}

app "psc/demo-app-2" {
  build {
    use "docker" {
      dockerfile = "${path.app}/Dockerfile"
    }

    registry {
      use "docker" {
        image = "${var.registry_username}/demo-app-2"
        tag = gitrefpretty()
        username = var.registry_username
        password = var.registry_password
        local = true
      }
    }
  }

  deploy {
    use "nomad-jobspec" {
      jobspec = templatefile("${path.app}/app2.nomad.tpl", {
        datacenter = var.datacenter
        nomad_namespace = var.nomad_namespace
        log_level = var.log_level
      })
    }
  }
}

variable "datacenter" {
  type = string
  default = ""
  env = ["NOMAD_DATACENTER"]
}

variable "nomad_namespace" {
  type = string
  default = ""
  env = ["NOMAD_NAMESPACE"]
}

variable "registry_username" {
  type    = string
  default = ""
  env     = ["REGISTRY_USERNAME"]
  sensitive = true
}

variable "registry_password" {
  type    = string
  default = ""
  env     = ["REGISTRY_PASSWORD"]
  sensitive = true
}

variable "log_level" {
  type = string
  default = "INFO"
}