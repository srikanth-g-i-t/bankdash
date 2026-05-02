import { useState, useEffect } from 'react'
import { transactionApi } from '../api/api'
import styles from './Transactions.module.css'

const TX_TYPES = ['TRANSFER', 'DEPOSIT', 'WITHDRAWAL']

export default function Transactions() {
  const [txPage, setTxPage]     = useState(null)
  const [loading, setLoading]   = useState(true)
  const [page, setPage]         = useState(0)
  const [activeTab, setActiveTab] = useState(null) // 'TRANSFER' | 'DEPOSIT' | 'WITHDRAWAL'
  const [form, setForm]         = useState({})
  const [submitting, setSubmit] = useState(false)
  const [error, setError]       = useState('')
  const [success, setSuccess]   = useState('')

  const load = (p = 0) => {
    setLoading(true)
    transactionApi.getAll(p, 15)
      .then(r => setTxPage(r.data))
      .catch(console.error)
      .finally(() => setLoading(false))
  }

  useEffect(() => { load(page) }, [page])

  const handleTab = (tab) => {
    setActiveTab(t => t === tab ? null : tab)
    setForm({})
    setError('')
    setSuccess('')
  }

  const handleSubmit = async e => {
    e.preventDefault()
    setSubmit(true)
    setError('')
    setSuccess('')
    try {
      if (activeTab === 'TRANSFER')   await transactionApi.transfer(form)
      if (activeTab === 'DEPOSIT')    await transactionApi.deposit(form)
      if (activeTab === 'WITHDRAWAL') await transactionApi.withdraw(form)
      setSuccess(`${activeTab} completed successfully!`)
      setForm({})
      load(0)
    } catch (err) {
      setError(err.response?.data?.message || 'Transaction failed. Please try again.')
    } finally {
      setSubmit(false)
    }
  }

  const fc = (k, v) => setForm(f => ({ ...f, [k]: v }))

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <div>
          <p className={styles.breadcrumb}>BankDash / Transactions</p>
          <h1 className={styles.title}>Transactions</h1>
        </div>
        <div className={styles.actions}>
          {TX_TYPES.map(t => (
            <button
              key={t}
              className={`${styles.actionBtn} ${activeTab === t ? styles.actionActive : ''}`}
              onClick={() => handleTab(t)}
            >
              {t === 'TRANSFER' ? '⇄ Transfer' : t === 'DEPOSIT' ? '↓ Deposit' : '↑ Withdraw'}
            </button>
          ))}
        </div>
      </div>

      {/* Transaction form panel */}
      {activeTab && (
        <div className={styles.formPanel}>
          <p className={styles.formTitle}>NEW {activeTab}</p>
          {error   && <div className={styles.errorBox}>{error}</div>}
          {success && <div className={styles.successBox}>{success}</div>}
          <form onSubmit={handleSubmit} className={styles.form}>
            {activeTab === 'TRANSFER' && (
              <>
                <Field label="FROM ACCOUNT NUMBER" value={form.fromAccountNumber || ''} onChange={v => fc('fromAccountNumber', v)} placeholder="BD00000000000001" required />
                <Field label="TO ACCOUNT NUMBER"   value={form.toAccountNumber   || ''} onChange={v => fc('toAccountNumber',   v)} placeholder="BD00000000000002" required />
              </>
            )}
            {(activeTab === 'DEPOSIT' || activeTab === 'WITHDRAWAL') && (
              <Field label="ACCOUNT NUMBER" value={form.accountNumber || ''} onChange={v => fc('accountNumber', v)} placeholder="BD00000000000001" required />
            )}
            <Field label="AMOUNT (USD)" value={form.amount || ''} onChange={v => fc('amount', v)} placeholder="0.00" type="number" step="0.01" min="0.01" required />
            <Field label="DESCRIPTION (OPTIONAL)" value={form.description || ''} onChange={v => fc('description', v)} placeholder="e.g. Monthly savings transfer" />
            <button type="submit" disabled={submitting} className={styles.submitBtn}>
              {submitting ? 'Processing…' : `Confirm ${activeTab} →`}
            </button>
          </form>
        </div>
      )}

      {/* Transactions table */}
      <div className={styles.tableCard}>
        <div className={styles.tableHeader}>
          <span className={styles.tableLabel}>TRANSACTION HISTORY</span>
          {txPage && (
            <span className={styles.tableCount}>{txPage.totalElements} total</span>
          )}
        </div>
        <div className={styles.table}>
          <div className={styles.thead}>
            <span>Reference</span>
            <span>Type</span>
            <span>From</span>
            <span>To</span>
            <span>Amount</span>
            <span>Status</span>
            <span>Date</span>
          </div>
          {loading ? (
            <p className={styles.loading}>Loading…</p>
          ) : txPage?.content?.length ? (
            txPage.content.map(tx => <TxRow key={tx.id} tx={tx} />)
          ) : (
            <p className={styles.empty}>No transactions yet.</p>
          )}
        </div>

        {/* Pagination */}
        {txPage && txPage.totalPages > 1 && (
          <div className={styles.pagination}>
            <button disabled={page === 0} onClick={() => setPage(p => p - 1)} className={styles.pageBtn}>← Prev</button>
            <span className={styles.pageInfo}>Page {page + 1} of {txPage.totalPages}</span>
            <button disabled={page >= txPage.totalPages - 1} onClick={() => setPage(p => p + 1)} className={styles.pageBtn}>Next →</button>
          </div>
        )}
      </div>
    </div>
  )
}

function Field({ label, value, onChange, placeholder, type = 'text', step, min, required }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
      <label style={{ fontFamily: 'var(--ff-mono)', fontSize: '0.58rem', letterSpacing: '0.18em', color: 'var(--muted)', textTransform: 'uppercase' }}>{label}</label>
      <input
        type={type} value={value} onChange={e => onChange(e.target.value)}
        placeholder={placeholder} step={step} min={min} required={required}
        style={{ background: 'var(--bg)', border: '1px solid var(--border)', color: 'var(--cream)', padding: '10px 12px', fontFamily: 'var(--ff-mono)', fontSize: '0.82rem', outline: 'none', width: '100%' }}
        onFocus={e => e.target.style.borderColor = 'var(--amber)'}
        onBlur={e  => e.target.style.borderColor = 'rgba(218,155,40,0.18)'}
      />
    </div>
  )
}

function TxRow({ tx }) {
  const isCredit = tx.type === 'DEPOSIT'
  return (
    <div className={styles.trow}>
      <span className={styles.ref}>{tx.referenceNumber?.slice(0, 14)}…</span>
      <span className={styles.type}>{tx.type}</span>
      <span className={styles.acc}>{tx.fromAccountNumber?.slice(0, 10)}…</span>
      <span className={styles.acc}>{tx.toAccountNumber?.slice(0, 10) || '—'}</span>
      <span className={`${styles.amount} ${isCredit ? styles.credit : styles.debit}`}>
        {isCredit ? '+' : '-'}${parseFloat(tx.amount).toLocaleString('en-US', { minimumFractionDigits: 2 })}
      </span>
      <span className={`${styles.status} ${tx.status === 'COMPLETED' ? styles.done : ''}`}>{tx.status}</span>
      <span className={styles.date}>{tx.createdAt ? new Date(tx.createdAt).toLocaleDateString() : '—'}</span>
    </div>
  )
}
