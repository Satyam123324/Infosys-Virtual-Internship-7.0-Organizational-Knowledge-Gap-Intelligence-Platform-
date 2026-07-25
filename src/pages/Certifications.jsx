import { useEffect, useState } from 'react';
import { Award, Plus, Trash2, ExternalLink } from 'lucide-react';
import Layout from '../components/Layout';
import { employeeApi } from '../api/employeeApi';

const EMPTY = { name: '', issuingBody: '', issueDate: '', expiryDate: '', credentialUrl: '' };

export default function Certifications() {
  const [certs, setCerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [form, setForm] = useState(EMPTY);
  const [submitting, setSubmitting] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const { data } = await employeeApi.getMyCertifications();
      setCerts(data.data || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load certifications');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.name.trim()) return;
    setSubmitting(true);
    setError('');
    setSuccess('');
    try {
      await employeeApi.addCertification({
        name: form.name,
        issuingBody: form.issuingBody || null,
        issueDate: form.issueDate || null,
        expiryDate: form.expiryDate || null,
        credentialUrl: form.credentialUrl || null,
      });
      setSuccess('Certification added!');
      setForm(EMPTY);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to add certification');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this certification?')) return;
    try {
      await employeeApi.deleteCertification(id);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete certification');
    }
  };

  return (
    <Layout title="Certifications" subtitle="Track your professional certifications and renewal dates">
      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div className="info-card" style={{ marginBottom: 24 }}>
        <div className="section-title" style={{ marginTop: 0, display: 'flex', alignItems: 'center', gap: 6 }}>
          <Plus size={16} /> Add a Certification
        </div>
        <form onSubmit={handleSubmit}>
          <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 12 }}>
            <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 220 }}>
              <label>Name *</label>
              <input name="name" value={form.name} onChange={handleChange} placeholder="e.g. AWS Certified Solutions Architect" required />
            </div>
            <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 180 }}>
              <label>Issuing Body</label>
              <input name="issuingBody" value={form.issuingBody} onChange={handleChange} placeholder="e.g. Amazon Web Services" />
            </div>
          </div>
          <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 12 }}>
            <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 150 }}>
              <label>Issue Date</label>
              <input type="date" name="issueDate" value={form.issueDate} onChange={handleChange} />
            </div>
            <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 150 }}>
              <label>Expiry Date</label>
              <input type="date" name="expiryDate" value={form.expiryDate} onChange={handleChange} />
            </div>
            <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 200 }}>
              <label>Credential URL</label>
              <input name="credentialUrl" value={form.credentialUrl} onChange={handleChange} placeholder="https://..." />
            </div>
          </div>
          <button type="submit" className="btn-primary" style={{ width: 'auto', padding: '10px 24px' }} disabled={submitting}>
            {submitting ? 'Adding...' : 'Add Certification'}
          </button>
        </form>
      </div>

      <div className="section-title" style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
        <Award size={16} /> My Certifications ({certs.length})
      </div>
      {loading ? (
        <div className="loading-text">Loading...</div>
      ) : certs.length === 0 ? (
        <div className="info-card" style={{ color: '#64748b' }}>No certifications added yet.</div>
      ) : (
        certs.map((c) => (
          <div key={c.id} className="info-card" style={{ marginBottom: 10 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <div>
                <div style={{ fontWeight: 600, fontSize: 14 }}>
                  {c.name}
                  {c.expired && <span className="role-badge" style={{ background: '#fff1f2', color: '#e11d48', marginLeft: 8 }}>Expired</span>}
                </div>
                {c.issuingBody && <div style={{ fontSize: 13, color: '#64748b', marginTop: 2 }}>{c.issuingBody}</div>}
                <div style={{ fontSize: 12, color: '#94a3b8', marginTop: 4 }}>
                  {c.issueDate ? `Issued ${c.issueDate}` : ''}{c.expiryDate ? ` · Expires ${c.expiryDate}` : ''}
                </div>
                {c.credentialUrl && (
                  <a href={c.credentialUrl} target="_blank" rel="noreferrer" style={{ fontSize: 12, color: '#0d9488', display: 'inline-flex', alignItems: 'center', gap: 4, marginTop: 4 }}>
                    View credential <ExternalLink size={12} />
                  </a>
                )}
              </div>
              <button onClick={() => handleDelete(c.id)} title="Delete" style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#e11d48' }}>
                <Trash2 size={16} />
              </button>
            </div>
          </div>
        ))
      )}
    </Layout>
  );
}
