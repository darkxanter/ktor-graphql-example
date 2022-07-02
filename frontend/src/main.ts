import { createApp, h } from 'vue'
import {
  cacheExchange,
  createClient,
  dedupExchange,
  defaultExchanges, fetchExchange,
  provideClient,
  subscriptionExchange,
} from '@urql/vue'
import { createClient as createWSClient } from 'graphql-ws'
import App from './App.vue'


const wsClient = createWSClient({
  url: `ws://${window.location.host}/subscriptions`,

  shouldRetry: () => true,
})


console.log({ defaultExchanges })

const urqlClient = createClient({
  url: '/graphql',
  requestPolicy: 'cache-and-network',
  exchanges: [
    dedupExchange,
    cacheExchange,
    subscriptionExchange({
      forwardSubscription: (operation) => ({
        subscribe: (sink) => ({
          unsubscribe: wsClient.subscribe(operation, sink),
        }),
      }),
      enableAllOperations: false,
    }),
    fetchExchange,
  ],
})

createApp({
  setup() {
    // provideApolloClient(apolloClient)
    provideClient(urqlClient)
  },
  render: () => h(App),
}).mount('#app')
