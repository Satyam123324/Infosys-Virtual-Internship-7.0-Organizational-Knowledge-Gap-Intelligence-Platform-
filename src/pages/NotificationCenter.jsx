import { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import { Check, X, CheckCheck, Inbox } from 'lucide-react';
import { notificationApi } from '../api/notificationApi';
import { TYPE_ICON, TYPE_LABEL, FILTER_GROUPS, timeAgo } from '../utils/notificationTypeMeta';

export default function NotificationCenter() {
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeFilter, setActiveFilter] = useState('ALL');
  const [unreadOnly, setUnreadOnly] = useState(false);

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const { data } = await notificationApi.getMyNotifications();
      setNotifications(data.data.notifications);
      setUnreadCount(data.data.unreadCount);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load notifications');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleMarkRead = async (id) => {
    try {
      await notificationApi.markAsRead(id);
      load();
    } catch (err) { /* ignore */ }
  };

  const handleDismiss = async (id) => {
    try {
      await notificationApi.deleteNotification(id);
      load();
    } catch (err) { /* ignore */ }
  };

  const handleMarkAllRead = async () => {
    try {
      await notificationApi.markAllAsRead();
      load();
    } catch (err) { /* ignore */ }
  };

  const activeGroup = FILTER_GROUPS.find((g) => g.key === activeFilter);
  const filtered = notifications
    .filter((n) => !activeGroup.types || activeGroup.types.includes(n.type))
    .filter((n) => !unreadOnly || !n.read);

  return (
    <Layout
      title="Notification Center"
      subtitle="Skill gaps, certification renewals, training deadlines, mentorship reminders, and milestones — all in one place"
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12, marginBottom: 16 }}>
        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
          {FILTER_GROUPS.map((g) => {
            const count = g.types
              ? notifications.filter((n) => g.types.includes(n.type)).length
              : notifications.length;
            const isActive = activeFilter === g.key;
            return (
              <button
                key={g.key}
                onClick={() => setActiveFilter(g.key)}
                className="status-pill"
                style={{
                  border: '1px solid ' + (isActive ? '#0d9488' : '#e2e8f0'),
                  background: isActive ? '#0d9488' : 'white',
                  color: isActive ? 'white' : '#475569',
                  cursor: 'pointer', fontSize: 12.5,
                }}
              >
                {g.label} {count > 0 && <span style={{ opacity: 0.85 }}>({count})</span>}
              </button>
            );
          })}
        </div>

        <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
          <label style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 13, color: '#475569', cursor: 'pointer' }}>
            <input type="checkbox" checked={unreadOnly} onChange={(e) => setUnreadOnly(e.target.checked)} />
            Unread only
          </label>
          {unreadCount > 0 && (
            <button className="btn-sm" onClick={handleMarkAllRead} style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
              <CheckCheck size={14} /> Mark all read
            </button>
          )}
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="loading-text">Loading notifications...</div>
      ) : filtered.length === 0 ? (
        <div className="info-card" style={{ textAlign: 'center', padding: '40px 20px', color: '#94a3b8' }}>
          <Inbox size={28} style={{ marginBottom: 10, opacity: 0.6 }} />
          <div style={{ fontSize: 14 }}>
            {unreadOnly ? "No unread notifications in this category." : "Nothing here — you're all caught up."}
          </div>
        </div>
      ) : (
        filtered.map((n) => {
          const { Icon, color, bg } = TYPE_ICON[n.type] || TYPE_ICON.GENERAL;
          const isMilestone = n.type === 'MILESTONE_ACHIEVED';
          return (
            <div
              key={n.id}
              className="info-card"
              style={{
                marginBottom: 12, display: 'flex', gap: 14, alignItems: 'flex-start',
                borderLeft: isMilestone ? '3px solid #9333ea' : (n.read ? undefined : '3px solid #0d9488'),
                background: n.read ? 'white' : '#f8fafc',
              }}
            >
              <div style={{
                width: 40, height: 40, borderRadius: 10, background: bg, color,
                display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
              }}>
                <Icon size={19} />
              </div>

              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
                  <span style={{ fontSize: 14.5, fontWeight: n.read ? 500 : 700, color: '#0f172a' }}>{n.title}</span>
                  <span className="status-pill" style={{ background: bg, color, fontSize: 11 }}>
                    {TYPE_LABEL[n.type] || n.type}
                  </span>
                  {isMilestone && (
                    <span className="status-pill" style={{ background: '#faf5ff', color: '#9333ea', fontSize: 11 }}>
                      🏆 Achievement
                    </span>
                  )}
                </div>
                <div style={{ fontSize: 13.5, color: '#64748b', marginTop: 4, lineHeight: 1.5 }}>{n.message}</div>
                <div style={{ fontSize: 12, color: '#94a3b8', marginTop: 6 }}>{timeAgo(n.createdAt)}</div>
              </div>

              <div style={{ display: 'flex', gap: 6, flexShrink: 0 }}>
                {!n.read && (
                  <button onClick={() => handleMarkRead(n.id)} title="Mark as read" className="btn-sm" style={{ padding: '6px 8px' }}>
                    <Check size={14} />
                  </button>
                )}
                <button onClick={() => handleDismiss(n.id)} title="Dismiss" className="btn-sm" style={{ padding: '6px 8px' }}>
                  <X size={14} />
                </button>
              </div>
            </div>
          );
        })
      )}
    </Layout>
  );
}
