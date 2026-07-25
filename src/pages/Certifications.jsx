import { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import { Award, Plus, X, ExternalLink, Trash2 } from 'lucide-react';
import { employeeApi } from '../api/employeeApi';
import { getCertificationStatus } from '../utils/certificationStatus';

const EMPTY_FORM = { name: '', issuingBody: '', issueDate: '', expiryDate: '', credentialUrl: '' };

export default function Certifications() {
  const [certifications, setCertifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const load = async () => {
    setLoading(true);
    try {
      const { data } = await employeeApi.getMyCertifications();
      setCertifications(data.data);
    } catch (err) {
      setError('Failed to load certifications');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSaving(true);
    try {
      await employeeApi.addCertification({
        ...form,
        issueDate: form.issueDate || null,
        expiryDate: form.expiryDate || null,
      });
      setForm(EMPTY_FORM);
      setShowForm(false);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to add certification');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    try {
      await employeeApi.deleteCertification(id);
      load();
    } catch (err) { /* ignore */ }
  };

  // Soonest-expiring first, so what needs attention surfaces at the top.
  const sorted = [...certifications].sort((a, b) => {
    if (!a.expiryDate) return 1;
    if (!b.expiryDate) return -1;
    return new Date(a.expiryDate) - new Date(b.expiryDate);
  });

  return (
    <Layout title="My Certifications" subtitle="Track credentials and stay ahead of renewals">
      <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 16 }}>
        <button className="btn-sm" onClick={() => setShowForm((s) => !s)} style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <Plus size={14} /> Add Certification
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleSubmit} className="info-card" style={{ marginBottom: 20 }}>
          {error && <div className="alert alert-error" style={{ marginBottom: 10 }}>{error}</div>}
          <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
            <div className="form-group" style={{ flex: '1 1 220px', marginBottom: 0 }}>
              <label>Certification Name</label>
              <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
            </div>
            <div className="form-group" style={{ flex: '1 1 180px', marginBottom: 0 }}>
              <label>Issuing Body</label>
              <input value={form.issuingBody} onChange={(e) => setForm({ ...form, issuingBody: e.target.value })} placeholder="e.g. AWS" />
            </div>
          </div>
          <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginTop: 12 }}>
            <div className="form-group" style={{ flex: '1 1 160px', marginBottom: 0 }}>
              <label>Issue Date</label>
              <input type="date" value={form.issueDate} onChange={(e) => setForm({ ...form, issueDate: e.target.value })} />
            </div>
            <div className="form-group" style={{ flex: '1 1 160px', marginBottom: 0 }}>
              <label>Expiry Date (optional)</label>
              <input type="date" value={form.expiryDate} onChange={(e) => setForm({ ...form, expiryDate: e.target.value })} />
            </div>
            <div className="form-group" style={{ flex: '1 1 220px', marginBottom: 0 }}>
              <label>Credential URL (optional)</label>
              <input value={form.credentialUrl} onChange={(e) => setForm({ ...form, credentialUrl: e.target.value })} placeholder="https://..." />
            </div>
          </div>
          <div style={{ display: 'flex', gap: 8, marginTop: 14 }}>
            <button type="submit" className="btn-primary" style={{ width: 'auto', padding: '9px 18px' }} disabled={saving}>
              {saving ? 'Saving...' : 'Add Certification'}
            </button>
            <button type="button" className="btn-sm" onClick={() => setShowForm(false)}>Cancel</button>
          </div>
        </form>
      )}

      {error && !showForm && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="loading-text">Loading certifications...</div>
      ) : sorted.length === 0 ? (
        <div className="info-card" style={{ textAlign: 'center', padding: '32px 20px', color: '#94a3b8' }}>
          <Award size={26} style={{ marginBottom: 10, opacity: 0.6 }} />
          <div style={{ fontSize: 13.5 }}>No certifications on file yet — add one to start tracking renewals.</div>
        </div>
      ) : (
        sorted.map((cert) => {
          const status = getCertificationStatus(cert);
          return (
            <div
              key={cert.id}
              className="info-card"
              style={{
                marginBottom: 12, display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                gap: 12, flexWrap: 'wrap', borderLeft: `3px solid ${status.color}`,
              }}
            >
              <div style={{ minWidth: 0 }}>
                <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
                  <span style={{ fontWeight: 600, fontSize: 14.5 }}>{cert.name}</span>
                  <span className="status-pill" style={{ background: status.bg, color: status.color, fontSize: 11 }}>
                    {status.label}
                  </span>
                </div>
                <div style={{ fontSize: 12.5, color: '#64748b', marginTop: 3 }}>
                  {cert.issuingBody && `${cert.issuingBody} · `}
                  {cert.issueDate && `Issued ${new Date(cert.issueDate).toLocaleDateString()}`}
                  {cert.expiryDate && ` · Expires ${new Date(cert.expiryDate).toLocaleDateString()}`}
                  {!cert.expiryDate && !cert.issueDate && 'No dates on file'}
                </div>
                {cert.credentialUrl && (
                  <a href={cert.credentialUrl} target="_blank" rel="noopener noreferrer"
                     style={{ display: 'inline-flex', alignItems: 'center', gap: 4, fontSize: 12.5, color: '#2563eb', marginTop: 6, textDecoration: 'none' }}>
                    View credential <ExternalLink size={12} />
                  </a>
                )}
              </div>
              <button onClick={() => handleDelete(cert.id)} title="Remove" className="btn-sm" style={{ padding: '6px 8px', flexShrink: 0 }}>
                <Trash2 size={14} />
              </button>
            </div>
          );
        })
      )}
    </Layout>
  );
}
