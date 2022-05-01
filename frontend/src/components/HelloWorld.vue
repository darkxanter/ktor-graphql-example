<script setup lang="ts">
import { ref } from 'vue'
import { useQuery, useSubscription } from '@vue/apollo-composable'
import gql from 'graphql-tag'

defineProps<{ msg: string }>()

const count = ref(0)


const { result } = useQuery(gql`
  query TestQuery {
    hello
  }
`)

interface Counter {
  counter: number
}

const counter1Enabled = ref(false)
const counter2Enabled = ref(false)

const { result: counter1 } = useSubscription<Counter>(gql`
  subscription Flow1 {
    counter
  }
`, null, () => ({
  enabled: counter1Enabled.value
}))

const { result: counter2 } = useSubscription<Counter>(gql`
  subscription Flow2 {
    counter
  }
`, null, () => ({ enabled: counter2Enabled.value }))


const counter1Start = () => counter1Enabled.value = true
const counter1Stop = () => counter1Enabled.value = false

const counter2Start = () => counter2Enabled.value = true
const counter2Stop = () => counter2Enabled.value = false


</script>

<template>
  <h1>{{ msg }}</h1>
  <div style="display: flex; justify-content: center">
    <div style="width: 200px">
      <div style="text-align: left">Counter1: {{ counter1?.counter }}</div>
      <div style="text-align: left">Counter2: {{ counter2?.counter }}</div>
    </div>
  </div>
  <div>
    <div>
      Counter1
      <button @click="counter1Start">Start</button>
      <button @click="counter1Stop">Stop</button>
    </div>
    <div>
      Counter2
      <button @click="counter2Start">Start</button>
      <button @click="counter2Stop">Stop</button>
    </div>
  </div>
</template>

<style scoped>
a {
  color: #42b983;
}

label {
  margin: 0 0.5em;
  font-weight: bold;
}

code {
  background-color: #eee;
  padding: 2px 4px;
  border-radius: 4px;
  color: #304455;
}
</style>
