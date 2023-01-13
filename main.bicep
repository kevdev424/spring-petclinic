resource appServicePlan 'Microsoft.Web/serverfarms@2021-02-01' = {
  name: 'devx-spring-petclinc-asp'
  location: 'td-petclinic-rg'
  sku: {
    name: 'F1'
  }
  properties: {
    reserved: true
  }
  kind: 'linux'
}

resource appService 'Microsoft.Web/sites@2021-02-01' = {
  name: 'devx-spring-petclinic'
  location: 'td-petclinic-rg'
  identity: {
    type: 'SystemAssigned'
  }
  properties: {
    serverFarmId: appServicePlan.id
    reserved: true
    siteConfig: {
      alwaysOn: false
      ftpsState: 'Disabled'
      linuxFxVersion: 'JAVA|17'
      http20Enabled: true
    }
    httpsOnly: true
  }
}
