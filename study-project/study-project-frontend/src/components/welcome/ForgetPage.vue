<script setup>
import {reactive} from "vue";
import {EditPen, Lock, Message} from "@element-plus/icons-vue";
import {ref} from "vue";
import {post} from "@/net";
import {ElMessage} from "element-plus";
import router from "@/router";

const form = reactive({
  email: '',
  code: '',
  password: '',
  password_repeat: '',
})
const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}
const rules = {
  email: [
    { required: true, message: '请输入邮件地址', trigger: 'blur' },
    {type: 'email', message: '请输入合法的电子邮件地址', trigger: ['blur', 'change']}
  ], code: [
    { required: true, message: '请输入获取的验证码', trigger: 'blur' },
  ], password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 16, message: '密码的长度必须在6-16个字符之间', trigger: ['blur', 'change'] }
  ], password_repeat: [
    { validator: validatePassword, trigger: ['blur', 'change'] },
  ]
}

const active = ref(0)
const formRef = ref()
const isEmailValid = ref(false)
const coldTime = ref(0)
const onValidate = (prop, isValid) => {
  if(prop === 'email')
    isEmailValid.value = isValid
}
const validateEmail = () => {
  coldTime.value = 60
  post('/api/auth/valid-reset-email', {
    email: form.email
  }, (message) => {
    ElMessage.success(message)
    setInterval(() => coldTime.value--, 1000)
  }, (message) => {
    ElMessage.warning(message)
    coldTime.value = 0
  })
}
const startReset = () => {
  formRef.value.validate((isValid) => {
    if(isValid) {
      post('/api/auth/start-reset', {
        email: form.email,
        code: form.code
      }, () => {
        active.value++
      })
    } else {
      ElMessage.warning('请填写电子邮件地址和验证码')
    }
  })


}
const doReset = () => {
  formRef.value.validate((isValid) => {
    if(isValid) {
      post('/api/auth/do-reset', {
        password: form.password
      }, (message) => {
        ElMessage.success(message)
        router.push('/')
      })
    } else {
      ElMessage.warning('请填写新的密码')
    }
  })
}
</script>

<template>
  <div>
    <div style="margin: 30px 20px">
      <el-steps :active="active" finish-status="success" align-center>
        <el-step title="验证电子邮件"></el-step>
        <el-step title="重新设置密码"></el-step>
      </el-steps>
    </div>
    <transition name="el-fade-in-linear" mode="out-in">
      <div style="text-align: center;margin: 0 20px; height: 100%" v-if="active === 0">
        <div style="margin-top: 250px">
          <div style="font-size: 25px; font-weight: bold">重置密码</div>
          <div style="font-size: 15px; color: grey">请输入需要重置密码的电子邮件地址</div>
        </div>
        <div style="margin-top: 50px">
          <el-form :model="form" :rules="rules" @validate="onValidate" ref="formRef">
            <el-form-item prop="email">
              <el-input   v-model="form.email" type="email" placeholder="电子邮件地址">
                <template #prefix>
                  <el-icon><Message /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item prop="code">
              <el-row :gutter="16">
                <el-col :span="18">
                  <el-input v-model="form.code" type="email" :maxlength="6" placeholder="请输入电子邮件验证码">
                    <template #prefix>
                      <el-icon><EditPen /></el-icon>
                    </template>
                  </el-input>
                </el-col>
                <el-col :span="6">
                  <el-button type="success" @click="validateEmail" :disabled="!isEmailValid || coldTime>0">
                    {{coldTime > 0 ? '请稍后 ' + coldTime + ' 秒' : '获取验证码'}}
                  </el-button>
                </el-col>
              </el-row>
            </el-form-item>
          </el-form>
        </div>
        <div style="margin-top: 50px">
          <el-button @click="startReset()" style="width: 270px ; color: dodgerblue" type="danger" plain>开始重置密码</el-button>
        </div>
        <div style="margin-top: 20px">
          <span style="font-size: 14px;line-height: 15px;color: grey">我后悔了 </span>
          <el-link type="primary" style="translate: 0 -2.5px" @click="router.push('/')">返回登录</el-link>
        </div>
      </div>
    </transition>
    <transition name="el-fade-in-linear" mode="out-in">
      <div style="text-align: center;margin: 0 20px; height: 100%" v-if="active === 1">
        <div style="margin-top: 250px">
          <div style="font-size: 25px; font-weight: bold">重置密码</div>
          <div style="font-size: 15px; color: grey">请填写你的新密码，务必牢记，防止丢失</div>
        </div>
        <div style="margin-top: 50px">
          <el-form :model="form" :rules="rules" @validate="onValidate" ref="formRef">
            <el-form-item prop="password">
              <el-input  v-model="form.password" type="password" :maxlength="16" placeholder="新密码">
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="password_repeat">
              <el-input   v-model="form.password_repeat" type="password" :maxlength="16" placeholder="重复新密码">
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-form>
        </div>
        <div style="margin-top: 50px">
          <el-button @click="doReset()" style="width: 270px " type="danger" plain>立即重置密码</el-button>
        </div>
      </div>
    </transition>
  </div>


</template>

<style scoped>

</style>