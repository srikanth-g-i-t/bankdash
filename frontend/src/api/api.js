import axios from 'axios'

const BASE_URL = import.meta.env.VITE_API_URL
  ? `${import.meta.env.VITE_API_URL}/api/v1`
  : '/api/v1'

const api = axios.create({ baseURL: BASE_URL })

// Attach JWT token to every request
api.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Auto-refresh on 401
api.interceptors.response.use(
  res => res,
  async err => {
    const original = err.config
    if (err.response?.status === 401 && !original._retry) {
      original._retry = true
      try {
        const refreshToken = localStorage.getItem('refreshToken')
        const { data } = await axios.post(`${BASE_URL}/auth/refresh`, { refreshToken })
        localStorage.setItem('accessToken', data.accessToken)
        original.headers.Authorization = `Bearer ${data.accessToken}`
        return api(original)
      } catch {
        localStorage.clear()
        window.location.href = '/login'
      }
    }
    return Promise.reject(err)
  }
)

export default api

// ── Auth API ──────────────────────────────────────────────────
export const authApi = {
  register: data  => api.post('/auth/register', data),
  login:    data  => api.post('/auth/login', data),
  logout:   ()    => api.post('/auth/logout'),
  me:       ()    => api.get('/auth/me'),
}

// ── Accounts API ─────────────────────────────────────────────
export const accountApi = {
  getSummary:  ()     => api.get('/accounts/summary'),
  getAll:      ()     => api.get('/accounts'),
  getById:     id     => api.get(`/accounts/${id}`),
  create:      data   => api.post('/accounts', data),
  close:       id     => api.delete(`/accounts/${id}`),
}

// ── Transactions API ──────────────────────────────────────────
export const transactionApi = {
  getAll:    (page = 0, size = 20) => api.get(`/transactions?page=${page}&size=${size}`),
  getById:   id                    => api.get(`/transactions/${id}`),
  transfer:  data                  => api.post('/transactions/transfer', data),
  deposit:   data                  => api.post('/transactions/deposit', data),
  withdraw:  data                  => api.post('/transactions/withdraw', data),
}
