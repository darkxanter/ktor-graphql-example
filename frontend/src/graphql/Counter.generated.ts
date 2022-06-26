import type * as Types from '../types';

import gql from 'graphql-tag';
import * as Urql from '@urql/vue';
export type Omit<T, K extends keyof T> = Pick<T, Exclude<keyof T, K>>;
export type CounterSubscriptionVariables = Types.Exact<{ [key: string]: never; }>;


export type CounterSubscription = { __typename?: 'Subscription', counter: number };


export const CounterDocument = gql`
    subscription Counter {
  counter
}
    `;

export function useCounterSubscription<R = CounterSubscription>(options: Omit<Urql.UseSubscriptionArgs<never, CounterSubscriptionVariables>, 'query'> = {}, handler?: Urql.SubscriptionHandlerArg<CounterSubscription, R>) {
  return Urql.useSubscription<CounterSubscription, R, CounterSubscriptionVariables>({ query: CounterDocument, ...options }, handler);
};