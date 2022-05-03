<script setup lang="ts">
import { ref } from 'vue'
import { useCounterSubscription } from '../graphql/Counter.generated'
import { useHelloQuery } from '../graphql/HelloQuery.generated'
import { useCounter2Subscription } from '../graphql/Counter2.generated'

defineProps<{ msg: string }>()

const count = ref(0)


interface Counter {
  counter: number
}

const counter1Paused = ref(true)
const counter2Paused = ref(true)

const { data: hello, executeQuery } = useHelloQuery()

const { data: counter1 } = useCounterSubscription({
  pause: counter1Paused,
})

const { data: counter2 } = useCounter2Subscription({
  pause: counter2Paused,
})

const counter1Start = () => counter1Paused.value = false
const counter1Stop = () => counter1Paused.value = true

const counter2Start = () => counter2Paused.value = false
const counter2Stop = () => counter2Paused.value = true


</script>

<template>
  <div>
    Hello {{ hello?.hello }}
  </div>
  <button @click="executeQuery">Refetch</button>
  <div style="display: flex; justify-content: center">
    <div style="width: 200px">
      <div style="text-align: left">Counter1: {{ counter1?.counter }}</div>
      <div style="text-align: left">Counter2: {{ counter2?.counter2 }}</div>
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
