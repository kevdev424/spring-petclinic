@description('Specifies the location for resources.')
param location string = 'eastus'

targetScope = 'subscription'

@description('Name of the Budget. It should be unique within a resource group.')
param budgetName string = 'DemoBudget'

@description('The total amount of cost or usage to track with the budget')
param amount int = 1000

@description('The time covered by a budget. Tracking of the amount will be reset based on the time grain.')
@allowed([
  'Monthly'
  'Quarterly'
  'Annually'
])
param timeGrain string = 'Annually'

@description('The start date must be first of the month in YYYY-MM-DD format. Future start date should not be more than three months. Past start date should be selected within the timegrain preiod.')
param startDate string = '2023-02-03'

@description('The end date for the budget in YYYY-MM-DD format. If not provided, we default this to 10 years from the start date.')
param endDate string = '2024-02-03'

@description('Threshold value associated with a notification. Notification is sent when the cost exceeded the threshold. It is always percent and has to be between 0.01 and 1000.')
param threshold int = 5

@description('The list of email addresses to send the budget notification to when the threshold is exceeded.')
param contactEmails array = ['shane.widanagama@td.com']

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

resource budget 'Microsoft.Consumption/budgets@2021-10-01' = {
  name: budgetName
  properties: {
    timePeriod: {
      startDate: startDate
      endDate: endDate
    }
    timeGrain: timeGrain
    amount: amount
    category: 'Cost'
    notifications: {
      NotificationForExceededBudget1: {
        enabled: true
        operator: 'GreaterThan'
        threshold: threshold
        contactEmails: contactEmails
      }
    }
  }
}
