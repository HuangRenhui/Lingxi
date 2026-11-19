import { createStore } from 'vuex'
import { getProfile, login as loginApi, logout as logoutApi, register as registerApi } from '@/api/auth'
import { getToken, getUser, removeToken, removeUser, setToken, setUser } from '@/utils/auth'

const user = {
  namespaced: true,
  state: () => ({
    token: getToken() || '',
    profile: getUser()
  }),
  getters: {
    isLoggedIn: state => Boolean(state.token)
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token || ''
    },
    SET_PROFILE(state, profile) {
      state.profile = profile || null
    },
    CLEAR(state) {
      state.token = ''
      state.profile = null
    }
  },
  actions: {
    async login({ commit }, form) {
      const data = await loginApi(form)
      setToken(data.token)
      setUser(data.user)
      commit('SET_TOKEN', data.token)
      commit('SET_PROFILE', data.user)
      return data
    },
    async register({ commit }, form) {
      const data = await registerApi(form)
      setToken(data.token)
      setUser(data.user)
      commit('SET_TOKEN', data.token)
      commit('SET_PROFILE', data.user)
      return data
    },
    async fetchProfile({ commit, state }) {
      if (!state.token) return null
      const data = await getProfile()
      setUser(data)
      commit('SET_PROFILE', data)
      return data
    },
    logout({ commit }) {
      const token = getToken()
      // 先通知服务端注销（加入黑名单），失败不影响本地清理
      if (token) logoutApi(token).catch(() => {})
      removeToken()
      removeUser()
      commit('CLEAR')
    }
  }
}

export default createStore({
  modules: {
    user
  }
})
