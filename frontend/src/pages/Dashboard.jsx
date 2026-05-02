import { useState, useEffect } from 'react'
import { accountApi, transactionApi } from '../api/api'
import { useAuth } from '../context/AuthContext'
import { AreaChart, Area, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts'
import styles from './Dashboard.module.css'

export default function Dashboard() {
  const { user }    = useAuth()
  const [summary, setSummary]   = useState(null)
  const [txPage,  setTxPage]    = useState(null)
  const [loading, setLoading]   = useState(true)

  useEffect(() => {
    Promise.all([
      accountApi.getSummary(),
      transactionApi.getAll(0, 5),
    ]).then(([s, t]) => {
      setSummary(s.data)
      setTxPage(t.data)
    }).catch(console.error)
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <PageLoader />

  const recentTx = txPage?.content || []
  // Build a simple spend trend (last 7 tx as chart data)
  const chartData = recentTx.slice().reverse().map((t, i) => ({
    name: `Tx ${i + 1}`,
    amount: parseFloat(t.amount),
  }))

  return (
    <div className={styles.page}>
      {/* Page header */}
      <div className={styles.pageHeader}>
        <div>
          <p className={styles.greeting}>Good day, {user?.firstName}</p>
          <h1 className={styles.pageTitle}>Financial Overview</h1>
        </div>
        <span className={styles.date}>{new Date().toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}</span>
      </div>

      {/* Top stats */}
      <div className={styles.statsGrid}>
        <StatCard
          label="Total Balance"
          value={`$${parseFloat(summary?.totalBalance || 0).toLocaleString('en-US', { minimumFractionDigits: 2 })}`}
          sub={`${summary?.totalAccounts || 0} active accounts`}
          accent
        />
        <StatCard label="Total Accounts" value={summary?.totalAccounts || 0} sub="Across all types" />
        <StatCard label="Recent Transactions" value={txPage?.totalElements || 0} sub="All time" />
        <StatCard label="Status" value="Active" sub="Account standing" green />
      </div>

      {/* Chart + accounts */}
      <div className={styles.midGrid}>
        {/* Spending trend */}
        <div className={styles.chartCard}>
          <div className={styles.cardHeader}>
            <span className={styles.cardLabel}>RECENT ACTIVITY TREND</span>
          </div>
          <div className={styles.chartWrap}>
            {chartData.length > 0 ? (
              <ResponsiveContainer width="100%" height={180}>
                <AreaChart data={chartData}>
                  <defs>
                    <linearGradient id="amberGrad" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%"  stopColor="#DA9B28" stopOpacity={0.25} />
                      <stop offset="95%" stopColor="#DA9B28" stopOpacity={0} />
                    </linearGradient>
                  </defs>
                  <XAxis dataKey="name" tick={{ fontFamily: 'IBM Plex Mono', fontSize: 10, fill: '#7A6E56' }} axisLine={false} tickLine={false} />
                  <YAxis tick={{ fontFamily: 'IBM Plex Mono', fontSize: 10, fill: '#7A6E56' }} axisLine={false} tickLine={false} />
                  <Tooltip
                    contentStyle={{ background: '#131209', border: '1px solid rgba(218,155,40,0.2)', fontFamily: 'IBM Plex Mono', fontSize: 12 }}
                    labelStyle={{ color: '#DA9B28' }}
                    itemStyle={{ color: '#F0E6CC' }}
                  />
                  <Area type="monotone" dataKey="amount" stroke="#DA9B28" strokeWidth={1.5} fill="url(#amberGrad)" />
                </AreaChart>
              </ResponsiveContainer>
            ) : (
              <p className={styles.empty}>No transaction data yet</p>
            )}
          </div>
        </div>

        {/* Accounts list */}
        <div className={styles.accountsCard}>
          <div className={styles.cardHeader}>
            <span className={styles.cardLabel}>YOUR ACCOUNTS</span>
            <a href="/accounts" className={styles.seeAll}>View all →</a>
          </div>
          {(summary?.accounts || []).slice(0, 4).map(acc => (
            <div key={acc.id} className={styles.accountRow}>
              <div>
                <p className={styles.accNum}>{acc.accountNumber}</p>
                <p className={styles.accType}>{acc.accountType}</p>
              </div>
              <span className={styles.accBalance}>
                ${parseFloat(acc.balance).toLocaleString('en-US', { minimumFractionDigits: 2 })}
              </span>
            </div>
          ))}
          {!summary?.accounts?.length && <p className={styles.empty}>No accounts found</p>}
        </div>
      </div>

      {/* Recent transactions */}
      <div className={styles.txCard}>
        <div className={styles.cardHeader}>
          <span className={styles.cardLabel}>RECENT TRANSACTIONS</span>
          <a href="/transactions" className={styles.seeAll}>View all →</a>
        </div>
        <div className={styles.txTable}>
          <div className={styles.txHead}>
            <span>Reference</span>
            <span>Type</span>
            <span>Description</span>
            <span>Amount</span>
            <span>Status</span>
            <span>Date</span>
          </div>
          {recentTx.length > 0 ? recentTx.map(tx => (
            <TxRow key={tx.id} tx={tx} />
          )) : (
            <p className={styles.empty} style={{ padding: '24px 20px' }}>No transactions yet</p>
          )}
        </div>
      </div>
    </div>
  )
}

function StatCard({ label, value, sub, accent, green }) {
  return (
    <div className={`${styles.statCard} ${accent ? styles.statAccent : ''}`}>
      <p className={styles.statLabel}>{label}</p>
      <p className={`${styles.statValue} ${green ? styles.statGreen : ''}`}>{value}</p>
      <p className={styles.statSub}>{sub}</p>
    </div>
  )
}

function TxRow({ tx }) {
  const isCredit = tx.type === 'DEPOSIT'
  return (
    <div className={styles.txRow}>
      <span className={styles.txRef}>{tx.referenceNumber?.slice(0, 16)}…</span>
      <span className={styles.txType}>{tx.type}</span>
      <span className={styles.txDesc}>{tx.description || '—'}</span>
      <span className={`${styles.txAmount} ${isCredit ? styles.credit : styles.debit}`}>
        {isCredit ? '+' : '-'}${parseFloat(tx.amount).toLocaleString('en-US', { minimumFractionDigits: 2 })}
      </span>
      <span className={`${styles.txStatus} ${tx.status === 'COMPLETED' ? styles.completed : ''}`}>{tx.status}</span>
      <span className={styles.txDate}>{tx.createdAt ? new Date(tx.createdAt).toLocaleDateString() : '—'}</span>
    </div>
  )
}

function PageLoader() {
  return <div style={{ padding: '40px', fontFamily: 'var(--ff-mono)', color: 'var(--amber)', fontSize: '0.8rem' }}>Loading dashboard…</div>
}
