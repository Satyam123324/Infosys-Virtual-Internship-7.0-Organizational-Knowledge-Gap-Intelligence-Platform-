import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { AlertTriangle, ShieldAlert, X, ArrowRight } from 'lucide-react';
import { employeeApi } from '../api/employeeApi';
import { summarizeCertifications } from '../utils/certificationStatus';

export default function CertificationBanner() {
  const [summary, setSummary] = useState(null);
  const [dismissed, setDismissed] = useState(false);

  useEffect(() => {
    employeeApi.getMyCertifications()
      .then(({ data }) => setSummary(summarizeCertifications(data.data)))
      .catch(() => {});
  }, []);

  if (!summary || dismissed) return null;
  const { expired, expiringSoon } = summary;
  if (expired === 0 && expiringSoon === 0) return null;

  // Expired takes priority over merely-expiring-soon for the banner's tone.
  const isUrgent = expired > 0;
  const color = isUrgent ? '#e11d48' : '#f59e0b';
  const bg = isUrgent ? '#fff1f2' : '#fffbeb';
  const border = isUrgent ? '#fecdd3' : '#fde68a';
  const Icon = isUrgent ? ShieldAlert : AlertTriangle;

  const parts = [];
  if (expired > 0) parts.push(`${expired} certification${expired > 1 ? 's have' : ' has'} expired`);
  if (expiringSoon > 0) parts.push(`${expiringSoon} expiring within 30 days`);

  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 12, padding: '12px 16px',
      background: bg, border: `1px solid ${border}`, borderRadius: 10, marginBottom: 20,
    }}>
      <Icon size={20} color={color} style={{ flexShrink: 0 }} />
      <div style={{ flex: 1, fontSize: 13.5, color: '#1e293b' }}>
        <strong style={{ color }}>{isUrgent ? 'Action needed:' : 'Heads up:'}</strong> {parts.join(' and ')}. Renew before it affects your skill gap standing.
      </div>
      <Link
        to="/certifications"
        style={{ display: 'flex', alignItems: 'center', gap: 4, fontSize: 13, fontWeight: 600, color, textDecoration: 'none', flexShrink: 0 }}
      >
        Review <ArrowRight size={14} />
      </Link>
      <button
        onClick={() => setDismissed(true)}
        style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#94a3b8', flexShrink: 0 }}
        title="Dismiss for this session"
      >
        <X size={16} />
      </button>
    </div>
  );
}
