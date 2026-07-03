import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import { hasPermission, hasAnyPermission, hasRole, hasAnyRole } from './directives/permission'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

app.directive('has-permission', hasPermission)
app.directive('has-any-permission', hasAnyPermission)
app.directive('has-role', hasRole)
app.directive('has-any-role', hasAnyRole)

app.mount('#app')
