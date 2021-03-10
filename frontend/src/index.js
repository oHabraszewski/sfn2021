import "./public/scss/main.scss"

import * as Vue from "vue"
import Login from "./public/vue/Login.vue"
import VueCookies from "vue3-cookies"

const App = {
    components: {
        Login
    }
}
  
Vue.createApp(App).use(VueCookies).mount('#app')

