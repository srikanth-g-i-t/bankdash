import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import styles from './Auth.module.css'

export default function Register() {
  const { register } = useAuth()
  const navigate = useNavigate()

  const [form, setForm] = useState({
    firstName: '', lastName: '', email: '',
    username: '', password: '', phoneNumber: '',
  })
  const [error, setError]     = useState('')
  const [loading, setLoading] = useState(false)

  const handleChange = e => setForm(f => ({ ...f, [e.target.name]: e.target.value }))

  const handleSubmit = async e => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await register(form)
      navigate('/dashboard')
    } catch (err) {
      const msg = err.response?.data?.message || 'Registration failed. Please try again.'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <div className={styles.header}>
          <div className={styles.logoMark}>BD</div>
          <h1 className={styles.title}>Create account</h1>
          <p className={styles.sub}>Join BankDash — secure banking platform</p>
        </div>

        {error && <div className={styles.errorBox}>{error}</div>}

        <form onSubmit={handleSubmit} className={styles.form}>
          <div className={styles.row}>
            <div className={styles.field}>
              <label className={styles.label}>FIRST NAME</label>
              <input name="firstName" value={form.firstName} onChange={handleChange}
                placeholder="John" required className={styles.input} />
            </div>
            <div className={styles.field}>
              <label className={styles.label}>LAST NAME</label>
              <input name="lastName" value={form.lastName} onChange={handleChange}
                placeholder="Doe" required className={styles.input} />
            </div>
          </div>

          <div className={styles.field}>
            <label className={styles.label}>EMAIL</label>
            <input type="email" name="email" value={form.email} onChange={handleChange}
              placeholder="you@example.com" required className={styles.input} />
          </div>

          <div className={styles.field}>
            <label className={styles.label}>USERNAME</label>
            <input name="username" value={form.username} onChange={handleChange}
              placeholder="johndoe" required minLength={3} className={styles.input} />
          </div>

          <div className={styles.field}>
            <label className={styles.label}>PASSWORD</label>
            <input type="password" name="password" value={form.password} onChange={handleChange}
              placeholder="Min. 8 characters" required minLength={8} className={styles.input} />
          </div>

          <div className={styles.field}>
            <label className={styles.label}>PHONE (OPTIONAL)</label>
            <input name="phoneNumber" value={form.phoneNumber} onChange={handleChange}
              placeholder="+1 000 000 0000" className={styles.input} />
          </div>

          <button type="submit" disabled={loading} className={styles.btn}>
            {loading ? 'Creating account...' : 'Create Account →'}
          </button>
        </form>

        <p className={styles.footer}>
          Already have an account?{' '}
          <Link to="/login" className={styles.link}>Sign in</Link>
        </p>
      </div>
    </div>
  )
}
