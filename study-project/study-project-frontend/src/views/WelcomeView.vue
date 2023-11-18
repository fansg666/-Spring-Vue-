<script setup>

import image from "@/assets/2be267d0f703918f501da513143d26975beec465.jpg";
</script>

<template>
  <div style="width: 100vw;height: 100vh;overflow: hidden;display: flex">
    <div style="flex: 1">
      <el-image style="width: 100%;height: 100%" fit="cover"
                :src= 'image'/>
    </div>
    <div class="welcome-title">
      <div style="font-size: 30px;font-weight: bold">欢迎来到我们的学习平台</div>
      <div style="margin-top: 10px">在这里你将可以与java之父王俊逸直接对话</div>
      <div style="margin-top: 5px">在这里你将可以同性交友，因为都是学java的男的，没有女生ovo</div>
    </div>
    <div style="width: 500px;background-color: white">
      <router-view/>
   </div>
  </div>
</template>

<style scoped>
.welcome-title{
  position: absolute;
  bottom: 30px;
  left: 30px;
  color: white;
  text-shadow: 0 0 10px black;
}
</style>