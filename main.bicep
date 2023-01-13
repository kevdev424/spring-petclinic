@description('Specifies the location for resources.')
param location string = 'eastus'

resource appServicePlan 'Microsoft.Web/serverfarms@2021-02-01' = {
  name: 'devx-spring-petclinc-asp'
  location: location
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
  location: location
  identity: {
    type: 'SystemAssigned'
  }
  properties: {
    serverFarmId: appServicePlan.id
    reserved: true
    siteConfig: {
      alwaysOn: false
      ftpsState: 'Disabled'
      linuxFxVersion: 'JAVA|17-java17'
      http20Enabled: true
    }
    httpsOnly: true
  }
}
