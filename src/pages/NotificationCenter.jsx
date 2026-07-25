import { useEffect, useState } from 'react';
import { Bell, Check, CheckCheck, Trash2 } from 'lucide-react';
import Layout from '../components/Layout';
import { notificationApi } from '../api/notificationApi';

export default function NotificationCenter() {
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      const { data } = await notificationApi.getMyNotifications();
      setNotifications(data.data.notifications || []);
      setUnreadCount(data.data.unreadCount || 0);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load notifications');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const markRead = async (id) => {
    try { await notificationApi.markAsRead(id); load(); } catch (e) { /* ignore */ }
  };
  const markAllRead = async () => {
    try { await notificationApi.markAllAsRead(); load(); } catch (e) { /* ignore */ }
  };
  const remove = async (id) => {
    try { await notificationApi.deleteNotification(id); load(); } catch (e) { /* ignore */ }
  };

  return (
    <Layout title="Notification Center" subtitle="All your alerts — gaps, training deadlines, sessions, and milestones">
      {error && <div className="alert alert-error">{error}</div>}

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <div style={{ fontSize: 14, color: '#64748b' }}>
          {unreadCount} unread · {notifications.length} total
        </div>
        {unreadCount > 0 && (
          <button onClick={markAllRead} className="btn-primary" style={{ width: 'auto', padding: '8px 16px', display: 'inline-flex', alignItems: 'center', gap: 6 }}>
            <CheckCheck size={15} /> Mark all read
          </button>
        )}
      </div>

      {loading ? (
        <div className="loading-text">Loading notifications...</div>
      ) : notifications.length === 0 ? (
        <div className="info-card" style={{ color: '#64748b', display: 'flex', alignItems: 'center', gap: 8 }}>
          <Bell size={16} /> You're all caught up — no notifications.
        </div>
      ) : (
        notifications.map((n) => (
          <div key={n.id} className="info-card" style={{ marginBottom: 10, borderLeft: n.read ? '3px solid #e2e8f0' : '3px solid #0d9488' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 12 }}>
              <div>
                <div style={{ fontWeight: n.read ? 500 : 700, fontSize: 14 }}>{n.title}</div>
                <div style={{ fontSize: 13, color: '#334155', marginTop: 4 }}>{n.message}</div>
                <div style={{ fontSize: 11, color: '#94a3b8', marginTop: 6 }}>
                  {n.type ? n.type.replaceAll('_', ' ') : ''}{n.createdAt ? ` · ${new Date(n.createdAt).toLocaleString()}` : ''}
                </div>
              </div>
              <div style={{ display: 'flex', gap: 8, flexShrink: 0 }}>
                {!n.read && (
                  <button onClick={() => markRead(n.id)} title="Mark read" style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#0d9488' }}>
                    <Check size={16} />
                  </button>
                )}
                <button onClick={() => remove(n.id)} title="Delete" style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#e11d48' }}>
                  <Trash2 size={16} />
                </button>
              </div>
            </div>
          </div>
        ))
      )}
    </Layout>
  );
}
