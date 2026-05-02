import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import styles from './Sidebar.module.css'

const NAV = [
  { to: '/dashboard',    label: 'Dashboard',     icon: '▦' },
  { to: '/accounts',     label: 'Accounts',       icon: '◈' },
  { to: '/transactions', label: 'Transactions',   icon: '⇄' },
]

export default function Sidebar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  return (
    <aside className={styles.sidebar}>
      {/* Logo */}
      <div className={styles.logo}>
        <span className={styles.logoMark}>BD</span>
        <span className={styles.logoName}>BankDash</span>
      </div>

      {/* User card */}
      <div className={styles.userCard}>
        <div className={styles.avatar}>
          {user?.firstName?.[0]}{user?.lastName?.[0]}
        </div>
        <div className={styles.userInfo}>
          <span className={styles.userName}>{user?.firstName} {user?.lastName}</span>
          <span className={styles.userRole}>Account Holder</span>
        </div>
      </div>

      <div className={styles.divider} />

      {/* Navigation */}
      <nav className={styles.nav}>
        <p className={styles.navLabel}>NAVIGATION</p>
        {NAV.map(({ to, label, icon }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              `${styles.navItem} ${isActive ? styles.active : ''}`
            }
          >
            <span className={styles.navIcon}>{icon}</span>
            {label}
          </NavLink>
        ))}
      </nav>

      <div className={styles.spacer} />

      {/* Bottom */}
      <div className={styles.bottom}>
        <div className={styles.statusDot}>
          <span className={styles.dot} />
          <span>All systems operational</span>
        </div>
        <button className={styles.logoutBtn} onClick={handleLogout}>
          ⏏ Logout
        </button>
      </div>
    </aside>
  )
}
