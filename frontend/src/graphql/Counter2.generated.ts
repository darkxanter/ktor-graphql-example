import type * as Types from '../types';

import gql from 'graphql-tag';
import * as Urql from '@urql/vue';
export type Omit<T, K extends keyof T> = Pick<T, Exclude<keyof T, K>>;
export type Counter2SubscriptionVariables = Types.Exact<{ [key: string]: never; }>;


export type Counter2Subscription = { __typename?: 'Subscription', counter2: number };


export const Counter2Document = gql`
    subscription Counter2 {
  counter2
}
    `;

export function useCounter2Subscription<R = Counter2Subscription>(options: Omit<Urql.UseSubscriptionArgs<never, Counter2SubscriptionVariables>, 'query'> = {}, handler?: Urql.SubscriptionHandlerArg<Counter2Subscription, R>) {
  return Urql.useSubscription<Counter2Subscription, R, Counter2SubscriptionVariables>({ query: Counter2Document, ...options }, handler);
};