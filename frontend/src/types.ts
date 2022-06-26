export type Maybe<T> = T | null;
export type InputMaybe<T> = Maybe<T>;
export type Exact<T extends { [key: string]: unknown }> = { [K in keyof T]: T[K] };
export type MakeOptional<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]?: Maybe<T[SubKey]> };
export type MakeMaybe<T, K extends keyof T> = Omit<T, K> & { [SubKey in K]: Maybe<T[SubKey]> };
/** All built-in and custom scalars, mapped to their actual values */
export type Scalars = {
  ID: string;
  String: string;
  Boolean: boolean;
  Int: number;
  Float: number;
};

export type AuthPayload = {
  __typename?: 'AuthPayload';
  token?: Maybe<Scalars['String']>;
  user?: Maybe<User>;
};

export type Mutation = {
  __typename?: 'Mutation';
  login: AuthPayload;
};


export type MutationLoginArgs = {
  aliasUUID?: InputMaybe<Scalars['String']>;
  email: Scalars['String'];
  password: Scalars['String'];
};

export type Query = {
  __typename?: 'Query';
  hello: Scalars['String'];
};

export type Subscription = {
  __typename?: 'Subscription';
  /** Returns a random number every second */
  counter: Scalars['Int'];
  /** Returns a random number every second */
  counter2: Scalars['Int'];
  /** Returns a random number every second, errors if even */
  counterWithError: Scalars['Int'];
  /** Returns stream of errors */
  flowOfErrors?: Maybe<Scalars['String']>;
  /** Returns a single value */
  singleValueSubscription: Scalars['Int'];
  /** Returns one value then an error */
  singleValueThenError: Scalars['Int'];
};


export type SubscriptionCounterArgs = {
  limit?: InputMaybe<Scalars['Int']>;
};


export type SubscriptionCounter2Args = {
  limit?: InputMaybe<Scalars['Int']>;
};

export type User = {
  __typename?: 'User';
  email: Scalars['String'];
  firstName?: Maybe<Scalars['String']>;
  intThatNeverComes: Scalars['Int'];
  isAdmin: Scalars['Boolean'];
  lastName?: Maybe<Scalars['String']>;
  universityId?: Maybe<Scalars['Int']>;
};
