import { useEffect, useRef, useState } from 'react';
import { Camera, Plus, Trash2, Upload, Download } from 'lucide-react';
import Layout from '../components/Layout';
import { employeeApi } from '../api/employeeApi';
import { useAuth } from '../context/AuthContext';

const BACKEND_BASE_URL = 'http://localhost:8080';

export default function MyProfile() {
  const { user, refreshProfile } = useAuth();
  const [profile, setProfile] = useState(null);
  const [certifications, setCertifications] = useState([]);
  const [workExperience, setWorkExperience] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [uploadingPhoto, setUploadingPhoto] = useState(false);
  const [uploadingResume, setUploadingResume] = useState(false);
  const photoInputRef = useRef(null);
  const resumeInputRef = useRef(null);

  const [showCertForm, setShowCertForm] = useState(false);
  const [certForm, setCertForm] = useState({ name: '', issuingBody: '', issueDate: '', expiryDate: '', credentialUrl: '' });
  const [savingCert, setSavingCert] = useState(false);

  const [showExpForm, setShowExpForm] = useState(false);
  const [expForm, setExpForm] = useState({ companyOrProject: '', roleTitle: '', startDate: '', endDate: '', description: '' });
  const [savingExp, setSavingExp] = useState(false);

  const loadAll = async () => {
    setLoading(true);
    try {
      const [profileRes, certsRes, expRes] = await Promise.all([
        employeeApi.getMyProfile(),
        employeeApi.getMyCertifications(),
        employeeApi.getMyWorkExperience(),
      ]);
      setProfile(profileRes.data.data);
      setCertifications(certsRes.data.data);
      setWorkExperience(expRes.data.data);
    } catch (err) {
      setError('Failed to load profile data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAll();
  }, []);

  const handlePhotoSelect = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    setUploadingPhoto(true);
    setError('');
    try {
      await employeeApi.uploadProfilePhoto(file);
      await loadAll();
      await refreshProfile();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to upload photo');
    } finally {
      setUploadingPhoto(false);
      e.target.value = '';
    }
  };

  const handleResumeSelect = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    setUploadingResume(true);
    setError('');
    try {
      await employeeApi.uploadResume(file);
      await loadAll();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to upload resume');
    } finally {
      setUploadingResume(false);
      e.target.value = '';
    }
  };

  const submitCertification = async (e) => {
    e.preventDefault();
    setSavingCert(true);
    try {
      await employeeApi.addCertification(certForm);
      setCertForm({ name: '', issuingBody: '', issueDate: '', expiryDate: '', credentialUrl: '' });
      setShowCertForm(false);
      loadAll();
    } catch (err) {
      alert('Failed to add certification: ' + (err.response?.data?.message || 'Unknown error'));
    } finally {
      setSavingCert(false);
    }
  };

  const deleteCertification = async (id) => {
    if (!confirm('Remove this certification?')) return;
    try {
      await employeeApi.deleteCertification(id);
      loadAll();
    } catch (err) {
      alert('Failed to delete certification');
    }
  };

  const submitExperience = async (e) => {
    e.preventDefault();
    setSavingExp(true);
    try {
      await employeeApi.addWorkExperience(expForm);
      setExpForm({ companyOrProject: '', roleTitle: '', startDate: '', endDate: '', description: '' });
      setShowExpForm(false);
      loadAll();
    } catch (err) {
      alert('Failed to add work experience: ' + (err.response?.data?.message || 'Unknown error'));
    } finally {
      setSavingExp(false);
    }
  };

  const deleteExperience = async (id) => {
    if (!confirm('Remove this work experience entry?')) return;
    try {
      await employeeApi.deleteWorkExperience(id);
      loadAll();
    } catch (err) {
      alert('Failed to delete work experience');
    }
  };

  const photoUrl = profile?.profileImageUrl
    ? (profile.profileImageUrl.startsWith('http') ? profile.profileImageUrl : BACKEND_BASE_URL + profile.profileImageUrl)
    : null;

  const resumeUrl = profile?.resumeUrl ? BACKEND_BASE_URL + profile.resumeUrl : null;

  return (
    <Layout title="My Profile" subtitle="Manage your photo, resume, certifications, and work experience">
      {error && <div className="alert alert-error">{error}</div>}

      {loading ? (
        <div className="loading-text">Loading profile...</div>
      ) : (
        <>
          <div className="info-card" style={{ marginBottom: 20, display: 'flex', gap: 32, alignItems: 'center', flexWrap: 'wrap' }}>
            <div style={{ textAlign: 'center' }}>
              <div
                onClick={() => photoInputRef.current?.click()}
                style={{
                  width: 88, height: 88, borderRadius: '50%', cursor: 'pointer', position: 'relative',
                  background: photoUrl ? `url(${photoUrl}) center/cover` : '#f0fdfa',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  border: '2px solid #e2e8f0', overflow: 'hidden',
                }}
              >
                {!photoUrl && (
                  <span style={{ fontSize: 24, fontWeight: 700, color: '#0d9488' }}>
                    {user?.fullName?.split(' ').map((n) => n[0]).slice(0, 2).join('').toUpperCase()}
                  </span>
                )}
                <div style={{
                  position: 'absolute', bottom: 0, left: 0, right: 0, background: 'rgba(15,23,42,0.6)',
                  color: 'white', fontSize: 10, padding: '3px 0', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 3,
                }}>
                  <Camera size={11} /> {uploadingPhoto ? '...' : 'Edit'}
                </div>
              </div>
              <input ref={photoInputRef} type="file" accept="image/jpeg,image/png,image/webp" hidden onChange={handlePhotoSelect} />
              <div style={{ fontSize: 11, color: '#94a3b8', marginTop: 6 }}>JPEG/PNG/WebP, max 5MB</div>
            </div>

            <div style={{ flex: 1, minWidth: 240 }}>
              <div className="label" style={{ marginBottom: 8 }}>Resume</div>
              {resumeUrl ? (
                <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                  <a href={resumeUrl} target="_blank" rel="noreferrer" style={{
                    display: 'flex', alignItems: 'center', gap: 6, fontSize: 13, color: '#0d9488', textDecoration: 'none',
                    background: '#f0fdfa', padding: '8px 14px', borderRadius: 8,
                  }}>
                    <Download size={14} /> View Current Resume
                  </a>
                  <button className="btn-sm" onClick={() => resumeInputRef.current?.click()} disabled={uploadingResume}>
                    {uploadingResume ? 'Uploading...' : 'Replace'}
                  </button>
                </div>
              ) : (
                <button className="btn-sm" onClick={() => resumeInputRef.current?.click()} disabled={uploadingResume} style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                  <Upload size={14} /> {uploadingResume ? 'Uploading...' : 'Upload Resume'}
                </button>
              )}
              <input ref={resumeInputRef} type="file" accept=".pdf,.doc,.docx" hidden onChange={handleResumeSelect} />
              <div style={{ fontSize: 11, color: '#94a3b8', marginTop: 6 }}>PDF/DOC/DOCX, max 10MB</div>
            </div>
          </div>

          <div className="section-title" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span>Certifications ({certifications.length})</span>
            <button className="btn-sm" onClick={() => setShowCertForm(!showCertForm)} style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
              <Plus size={14} /> Add Certification
            </button>
          </div>

          {showCertForm && (
            <form onSubmit={submitCertification} className="info-card" style={{ marginBottom: 14 }}>
              <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 12 }}>
                <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 180 }}>
                  <label>Certification Name</label>
                  <input value={certForm.name} onChange={(e) => setCertForm({ ...certForm, name: e.target.value })} required />
                </div>
                <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 180 }}>
                  <label>Issuing Body</label>
                  <input value={certForm.issuingBody} onChange={(e) => setCertForm({ ...certForm, issuingBody: e.target.value })} />
                </div>
              </div>
              <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 12 }}>
                <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 150 }}>
                  <label>Issue Date</label>
                  <input type="date" value={certForm.issueDate} onChange={(e) => setCertForm({ ...certForm, issueDate: e.target.value })} />
                </div>
                <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 150 }}>
                  <label>Expiry Date</label>
                  <input type="date" value={certForm.expiryDate} onChange={(e) => setCertForm({ ...certForm, expiryDate: e.target.value })} />
                </div>
                <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 180 }}>
                  <label>Credential URL</label>
                  <input value={certForm.credentialUrl} onChange={(e) => setCertForm({ ...certForm, credentialUrl: e.target.value })} placeholder="https://..." />
                </div>
              </div>
              <button type="submit" className="btn-primary" style={{ width: 'auto', padding: '9px 20px' }} disabled={savingCert}>
                {savingCert ? 'Saving...' : 'Save Certification'}
              </button>
            </form>
          )}

          {certifications.length === 0 ? (
            <div className="info-card" style={{ color: '#64748b', marginBottom: 20 }}>No certifications added yet.</div>
          ) : (
            certifications.map((c) => (
              <div key={c.id} className="info-card" style={{ marginBottom: 10, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <div style={{ fontWeight: 600, fontSize: 14 }}>{c.name}</div>
                  <div style={{ fontSize: 12, color: '#64748b' }}>
                    {c.issuingBody || '—'} {c.issueDate && `· Issued ${c.issueDate}`} {c.expired && <span style={{ color: '#e11d48' }}>· Expired</span>}
                  </div>
                </div>
                <button className="btn-sm" onClick={() => deleteCertification(c.id)} style={{ color: '#e11d48' }}>
                  <Trash2 size={13} />
                </button>
              </div>
            ))
          )}

          <div className="section-title" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span>Work Experience ({workExperience.length})</span>
            <button className="btn-sm" onClick={() => setShowExpForm(!showExpForm)} style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
              <Plus size={14} /> Add Experience
            </button>
          </div>

          {showExpForm && (
            <form onSubmit={submitExperience} className="info-card" style={{ marginBottom: 14 }}>
              <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 12 }}>
                <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 180 }}>
                  <label>Company / Project</label>
                  <input value={expForm.companyOrProject} onChange={(e) => setExpForm({ ...expForm, companyOrProject: e.target.value })} required />
                </div>
                <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 180 }}>
                  <label>Role Title</label>
                  <input value={expForm.roleTitle} onChange={(e) => setExpForm({ ...expForm, roleTitle: e.target.value })} />
                </div>
              </div>
              <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 12 }}>
                <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 150 }}>
                  <label>Start Date</label>
                  <input type="date" value={expForm.startDate} onChange={(e) => setExpForm({ ...expForm, startDate: e.target.value })} />
                </div>
                <div className="form-group" style={{ marginBottom: 0, flex: 1, minWidth: 150 }}>
                  <label>End Date</label>
                  <input type="date" value={expForm.endDate} onChange={(e) => setExpForm({ ...expForm, endDate: e.target.value })} />
                </div>
              </div>
              <div className="form-group">
                <label>Description</label>
                <input value={expForm.description} onChange={(e) => setExpForm({ ...expForm, description: e.target.value })} placeholder="What did you work on?" />
              </div>
              <button type="submit" className="btn-primary" style={{ width: 'auto', padding: '9px 20px' }} disabled={savingExp}>
                {savingExp ? 'Saving...' : 'Save Experience'}
              </button>
            </form>
          )}

          {workExperience.length === 0 ? (
            <div className="info-card" style={{ color: '#64748b' }}>No work experience logged yet.</div>
          ) : (
            workExperience.map((w) => (
              <div key={w.id} className="info-card" style={{ marginBottom: 10, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <div style={{ fontWeight: 600, fontSize: 14 }}>{w.roleTitle || w.companyOrProject}</div>
                  <div style={{ fontSize: 12, color: '#64748b' }}>
                    {w.companyOrProject} {w.startDate && `· ${w.startDate} — ${w.endDate || 'Present'}`}
                  </div>
                  {w.description && <div style={{ fontSize: 12.5, color: '#334155', marginTop: 4 }}>{w.description}</div>}
                </div>
                <button className="btn-sm" onClick={() => deleteExperience(w.id)} style={{ color: '#e11d48' }}>
                  <Trash2 size={13} />
                </button>
              </div>
            ))
          )}
        </>
      )}
    </Layout>
  );
}
