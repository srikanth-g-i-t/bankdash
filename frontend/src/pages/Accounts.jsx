import { useState, useEffect } from 'react'
import { accountApi } from '../api/api'
import styles from './Accounts.module.css'

const ACCOUNT_TYPES = ['CHECKING', 'SAVINGS', 'INVESTMENT', 'CREDIT']

export default function Accounts() {
  const [summary, setSummary]   = useState(null)
  const [loading, setLoading]   = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm]         = useState({ accountType: 'CHECKING', nickname: '', currency: 'USD' })
  const [creating, setCreating] = useState(false)
  const [error, setError]       = useState('')

  const load = () => {
    setLoading(true)
    accountApi.getSummary()
      .then(r => setSummary(r.data))
      .catch(console.error)
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const handleCreate = async e => {
    e.preventDefault()
    setCreating(true)
    setError('')
    try {
      await accountApi.create(form)
      setShowForm(false)
      setForm({ accountType: 'CHECKING', nickname: '', currency: 'USD' })
      load()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create account')
    } finally {
      setCreating(false)
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <div>
          <p className={styles.breadcrumb}>BankDash / Accounts</p>
          <h1 className={styles.title}>My Accounts</h1>
        </div>
        <button className={styles.addBtn} onClick={() => setShowForm(v => !v)}>
          {showForm ? '✕ Cancel' : '+ Open Account'}
        </button>
      </div>

      {/* Summary bar */}
      {summary && (
        <div className={styles.summaryBar}>
          <div className={styles.summaryItem}>
            <span className={styles.summaryLabel}>TOTAL BALANCE</span>
            <span className={styles.summaryValue}>
              ${parseFloat(summary.totalBalance || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}
            </span>
          </div>
          <div className={styles.summaryItem}>
            <span className={styles.summaryLabel}>ACCOUNTS</span>
            <span className={styles.summaryValue}>{summary.totalAccounts}</span>
          </div>
        </div>
      )}

      {/* Create account form */}
      {showForm && (
        <div className={styles.formCard}>
          <p className={styles.formTitle}>OPEN NEW ACCOUNT</p>
          {error && <div className={styles.errorBox}>{error}</div>}
          <form onSubmit={handleCreate} className={styles.form}>
            <div className={styles.formRow}>
              <div className={styles.field}>
                <label className={styles.label}>ACCOUNT TYPE</label>
                <select
                  value={form.accountType}
                  onChange={e => setForm(f => ({ ...f, accountType: e.target.value }))}
                  className={styles.select}
                >
                  {ACCOUNT_TYPES.map(t => <option key={t}>{t}</option>)}
                </select>
              </div>
              <div className={styles.field}>
                <label className={styles.label}>NICKNAME (OPTIONAL)</label>
                <input
                  value={form.nickname}
                  onChange={e => setForm(f => ({ ...f, nickname: e.target.value }))}
                  placeholder="e.g. Emergency Fund"
                  className={styles.input}
                />
              </div>
              <div className={styles.field}>
                <label className={styles.label}>CURRENCY</label>
                <select
                  value={form.currency}
                  onChange={e => setForm(f => ({ ...f, currency: e.target.value }))}
                  className={styles.select}
                >
                  {['USD', 'EUR', 'GBP'].map(c => <option key={c}>{c}</option>)}
                </select>
              </div>
            </div>
            <button type="submit" disabled={creating} className={styles.submitBtn}>
              {creating ? 'Opening…' : 'Open Account →'}
            </button>
          </form>
        </div>
      )}

      {/* Accounts grid */}
      {loading ? (
        <p className={styles.loading}>Loading accounts…</p>
      ) : (
        <div className={styles.grid}>
          {(summary?.accounts || []).map(acc => (
            <AccountCard key={acc.id} acc={acc} />
          ))}
          {!summary?.accounts?.length && (
            <p className={styles.empty}>No accounts yet. Open one above.</p>
          )}
        </div>
      )}
    </div>
  )
}

function AccountCard({ acc }) {
  const typeColors = {
    CHECKING:   '#DA9B28',
    SAVINGS:    '#4CAF7D',
    INVESTMENT: '#7B61FF',
    CREDIT:     '#E05252',
  }
  const color = typeColors[acc.accountType] || '#DA9B28'

  return (
    <div className={styles.card} style={{ borderTopColor: color }}>
      <div className={styles.cardTop}>
        <span className={styles.cardType} style={{ color }}>{acc.accountType}</span>
        <span className={`${styles.cardStatus} ${acc.status === 'ACTIVE' ? styles.active : ''}`}>
          {acc.status}
        </span>
      </div>
      {acc.nickname && <p className={styles.nickname}>{acc.nickname}</p>}
      <p className={styles.accountNumber}>{acc.accountNumber}</p>
      <div className={styles.balanceSection}>
        <span className={styles.balanceLabel}>AVAILABLE BALANCE</span>
        <span className={styles.balance}>
          ${parseFloat(acc.balance).toLocaleString('en-US', { minimumFractionDigits: 2 })}
          <small> {acc.currency}</small>
        </span>
      </div>
      <p className={styles.opened}>Opened {acc.createdAt ? new Date(acc.createdAt).toLocaleDateString() : '—'}</p>
    </div>
  )
}
