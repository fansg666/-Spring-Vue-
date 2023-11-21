<script setup>
import  image from '@/assets/2be267d0f703918f501da513143d26975beec465.jpg'
import {Lock, User} from '@element-plus/icons-vue'
import {get} from "@/net"
import {ElMessage} from "element-plus";
import {useStore} from "@/stores";
import router from "@/router";
const store = useStore()
if(store.auth.user == null){
  get('/api/user/me',(message)=> {
    store.auth.user = message
    router.push('/index')
  }, ()=>{
    store.auth.user = null
  })
}

</script>

<template>
  <router-view/>
</template>
<style scoped>
</style>