import { createApp, h } from 'vue'
import { provideApolloClient } from '@vue/apollo-composable'
import App from './App.vue'
import { ApolloClient, HttpLink, InMemoryCache, split } from '@apollo/client/core'
import { WebSocketLink } from '@apollo/client/link/ws'
import { getMainDefinition } from '@apollo/client/utilities'

const cache = new InMemoryCache()

const httpLink = new HttpLink({
  uri: "/graphql"
});

const wsLink = new WebSocketLink({
  uri: `ws://${window.location.host}/subscriptions`,
  options: {
    reconnect: true
  }
});

// using the ability to split links, you can send data to each link
// depending on what kind of operation is being sent
const link = split(
    // split based on operation type
    ({ query }) => {
      const definition = getMainDefinition(query);
      return (
          definition.kind === "OperationDefinition" &&
          definition.operation === "subscription"
      );
    },
    wsLink,
    httpLink
);

const apolloClient = new ApolloClient({
  cache,
  link: link,
})

createApp({
  setup() {
    provideApolloClient(apolloClient)
  },
  render: () => h(App)
}).mount('#app')
