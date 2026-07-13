import { useEffect, useRef, useState } from 'react';
import { Bell, AlertTriangle, AlertOctagon, ShieldAlert, Check, X } from 'lucide-react';
import { notificationApi } from '../api/notificationApi';

const TYPE_ICON = {
  CRITICAL_GAP: { Icon: AlertOctagon, color: '#e11d48', bg: '#fff1f2' },
  MODERATE_GAP: { Icon: AlertTriangle, color: '#f59e0b', bg: '#fffbeb' },
  CERTIFICATION_EXPIRING: { Icon: ShieldAlert, color: '#f59e0b', bg: '#fffbeb' },
  CERTIFICATION_EXPIRED: { Icon: ShieldAlert, color: '#e11d48', bg: '#fff1f2' },
  ASSESSMENT_REMINDER: { Icon: Bell, color: '#2563eb', bg: '#eff6ff' },
  GENERAL: { Icon: Bell, color: '#64748b', bg: '#f1f5f9' },
};

function timeAgo(dateStr) {
  const diffMs = Date.now() - new Date(dateStr).getTime();
  const mins = Math.floor(diffMs / 60000);
  if (mins < 1) return 'just now';
  if (mins < 60) return `${mins}m ago`;
  const hours = Math.floor(mins / 60);
  if (hours < 24) return `${hours}h ago`;
  return `${Math.floor(hours / 24)}d ago`;
}

export default function NotificationBell() {
  const [open, setOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const ref = useRef(null);

  const load = async () => {
    try {
      const { data } = await notificationApi.getMyNotifications();
      setNotifications(data.data.notifications);
      setUnreadCount(data.data.unreadCount);
    } catch (err) {
      // fail quietly — bell just shows nothing
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    const interval = setInterval(load, 60000); // refresh every minute
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleMarkRead = async (id, e) => {
    e.stopPropagation();
    try {
      await notificationApi.markAsRead(id);
      load();
    } catch (err) { /* ignore */ }
  };

  const handleDismiss = async (id, e) => {
    e.stopPropagation();
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

  return (
    <div ref={ref} style={{ position: 'relative' }}>
      <button
        onClick={() => setOpen(!open)}
        style={{
          position: 'relative', background: 'none', border: 'none', cursor: 'pointer',
          padding: 8, borderRadius: 8, display: 'flex', color: '#475569',
        }}
      >
        <Bell size={19} />
        {unreadCount > 0 && (
          <span style={{
            position: 'absolute', top: 3, right: 3, background: '#e11d48', color: 'white',
            fontSize: 10, fontWeight: 700, borderRadius: 999, minWidth: 16, height: 16,
            display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '0 3px',
          }}>
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        )}
      </button>

      {open && (
        <div style={{
          position: 'absolute', right: 0, top: 40, width: 360, maxHeight: 440, overflowY: 'auto',
          background: 'white', border: '1px solid #e2e8f0', borderRadius: 12,
          boxShadow: '0 12px 32px rgba(0,0,0,0.12)', zIndex: 50,
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px 16px', borderBottom: '1px solid #f1f5f9' }}>
            <span style={{ fontWeight: 600, fontSize: 14 }}>Notifications</span>
            {unreadCount > 0 && (
              <button onClick={handleMarkAllRead} style={{ background: 'none', border: 'none', color: '#0d9488', fontSize: 12, cursor: 'pointer' }}>
                Mark all read
              </button>
            )}
          </div>

          {loading ? (
            <div style={{ padding: 24, textAlign: 'center', color: '#94a3b8', fontSize: 13 }}>Loading...</div>
          ) : notifications.length === 0 ? (
            <div style={{ padding: 24, textAlign: 'center', color: '#94a3b8', fontSize: 13 }}>
              You're all caught up — no alerts right now.
            </div>
          ) : (
            notifications.map((n) => {
              const { Icon, color, bg } = TYPE_ICON[n.type] || TYPE_ICON.GENERAL;
              return (
                <div
                  key={n.id}
                  style={{
                    display: 'flex', gap: 10, padding: '12px 16px', borderBottom: '1px solid #f8fafc',
                    background: n.read ? 'white' : '#f8fafc', cursor: 'default',
                  }}
                >
                  <div style={{
                    width: 30, height: 30, borderRadius: 8, background: bg, color,
                    display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
                  }}>
                    <Icon size={15} />
                  </div>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ fontSize: 13, fontWeight: n.read ? 500 : 600, color: '#0f172a' }}>{n.title}</div>
                    <div style={{ fontSize: 12, color: '#64748b', marginTop: 2, lineHeight: 1.4 }}>{n.message}</div>
                    <div style={{ fontSize: 11, color: '#94a3b8', marginTop: 4 }}>{timeAgo(n.createdAt)}</div>
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 4, flexShrink: 0 }}>
                    {!n.read && (
                      <button onClick={(e) => handleMarkRead(n.id, e)} title="Mark as read" style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#0d9488', padding: 2 }}>
                        <Check size={13} />
                      </button>
                    )}
                    <button onClick={(e) => handleDismiss(n.id, e)} title="Dismiss" style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#94a3b8', padding: 2 }}>
                      <X size={13} />
                    </button>
                  </div>
                </div>
              );
            })
          )}
        </div>
      )}
    </div>
  );
}
