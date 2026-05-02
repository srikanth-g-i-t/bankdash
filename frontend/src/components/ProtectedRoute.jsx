import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function ProtectedRoute() {
  const { user, loading } = useAuth()
  if (loading) return <div style={{ color: 'var(--amber)', padding: '40px', fontFamily: 'var(--ff-mono)' }}>Loading...</div>
  return user ? <Outlet /> : <Navigate to="/login" replace />
}
