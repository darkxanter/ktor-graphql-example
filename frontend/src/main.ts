import { createApp, h } from 'vue'
import { createClient, defaultExchanges, provideClient, subscriptionExchange } from '@urql/vue'
import { SubscriptionClient } from 'subscriptions-transport-ws'
import { createClient as createWSClient } from 'graphql-ws';
// import { provideApolloClient } from '@vue/apollo-composable'
import App from './App.vue'
// import { ApolloClient, HttpLink, InMemoryCache, split } from '@apollo/client/core'
// import { WebSocketLink } from '@apollo/client/link/ws'
// import { getMainDefinition } from '@apollo/client/utilities'

// const cache = new InMemoryCache()
//
// const httpLink = new HttpLink({
//   uri: "/graphql"
// });
//
// const wsLink = new WebSocketLink({
//   uri: `ws://${window.location.host}/subscriptions`,
//   options: {
//     reconnect: true
//   }
// });
//
// // using the ability to split links, you can send data to each link
// // depending on what kind of operation is being sent
// const link = split(
//     // split based on operation type
//     ({ query }) => {
//       const definition = getMainDefinition(query);
//       return (
//           definition.kind === "OperationDefinition" &&
//           definition.operation === "subscription"
//       );
//     },
//     wsLink,
//     httpLink
// );
//
// const apolloClient = new ApolloClient({
//   cache,
//   link: link,
// })


const wsClient = createWSClient({
  url: `ws://${window.location.host}/subscriptions`,
  shouldRetry: () => true,
});


// const subscriptionClient = new SubscriptionClient(`ws://${window.location.host}/subscriptions`, {
//   reconnect: true,
// })

const urqlClient = createClient({
  url: '/graphql',
  exchanges: [
    subscriptionExchange({
      forwardSubscription: (operation) => ({
        subscribe: (sink) => ({
          unsubscribe: wsClient.subscribe(operation, sink),
        }),
      }),
      enableAllOperations:  true,
    }),
    ...defaultExchanges,
  ],
})

createApp({
  setup() {
    // provideApolloClient(apolloClient)
    provideClient(urqlClient)
  },
  render: () => h(App),
}).mount('#app')
